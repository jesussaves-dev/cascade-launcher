package dev.tom.cascade.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dev.tom.cascade.data.Prefs
import dev.tom.cascade.model.AppEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.util.Collections
import javax.xml.parsers.DocumentBuilderFactory

data class IconPackInfo(val packageName: String, val label: String)

class IconPackRepository(
    context: Context,
    private val prefs: Prefs,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Published wholesale so composition can read them without locking.
    @Volatile private var mappings: Map<String, IconPackMapping?> = emptyMap()
    @Volatile private var iconsByPack: Map<String, List<PackIcon>> = emptyMap()
    private val loadedMappings = HashMap<String, IconPackMapping?>()
    private val loadedIcons = HashMap<String, List<PackIcon>>()
    private val packUpdateTimes = HashMap<String, Long>()
    private val packContexts = HashMap<String, Context>()
    private val misses = LinkedHashSet<String>()
    private val loadingIcons = Collections.synchronizedSet(HashSet<String>())
    private val loadLock = Any()
    private val cache = LruCache<String, ImageBitmap>(128)

    private val _packs = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val packs: StateFlow<List<IconPackInfo>> = _packs.asStateFlow()
    val activePack: StateFlow<String?> = prefs.iconPack

    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    init {
        scope.launch { load(packageName = prefs.iconPack.value) }
    }

    fun refreshPacks() {
        scope.launch {
            val found = queryPacks()
            _packs.value = found
            val active = prefs.iconPack.value ?: return@launch
            val info = found.firstOrNull { it.packageName == active }
            if (info == null) {
                prefs.setIconPack(null)
                invalidate(active)
            } else {
                val updated = updateTime(active)
                if (packUpdateTimes[active] != updated) invalidate(active)
                load(active)
            }
        }
    }

    fun setActive(packageName: String?) {
        prefs.setIconPack(packageName)
        scope.launch {
            cache.evictAll()
            misses.clear()
            load(packageName)
        }
    }

    fun icon(app: AppEntry, sizePx: Int): ImageBitmap? {
        val pack = prefs.iconPack.value ?: return null
        val mapping = mappings[pack] ?: return null
        val drawableName = mapping.drawableName(app.packageName, app.activityName) ?: return null
        val cacheKey = "$pack|${app.key}|$sizePx"
        cache.get(cacheKey)?.let { return it }
        if (cacheKey in misses) return null
        return bitmapFor(pack, drawableName, sizePx, cacheKey)
    }

    fun iconByDrawable(packageName: String, drawable: String, sizePx: Int): ImageBitmap? {
        val cacheKey = "$packageName|drawable:$drawable|$sizePx"
        cache.get(cacheKey)?.let { return it }
        if (cacheKey in misses) return null
        return bitmapFor(packageName, drawable, sizePx, cacheKey)
    }

    fun icons(packageName: String?): List<PackIcon> {
        val pack = packageName ?: return emptyList()
        iconsByPack[pack]?.let { return it }
        if (loadingIcons.add(pack)) {
            scope.launch {
                runCatching { loadIcons(pack) }.getOrNull()?.let {
                    loadedIcons[pack] = it
                    iconsByPack = HashMap(loadedIcons)
                    _version.value += 1
                }
                loadingIcons.remove(pack)
            }
        }
        return emptyList()
    }

    private fun bitmapFor(pack: String, drawableName: String, sizePx: Int, cacheKey: String): ImageBitmap? {
        val result = runCatching {
            val packContext = packContexts.getOrPut(pack) { appContext.createPackageContext(pack, 0) }
            val id = packContext.resources.getIdentifier(drawableName, "drawable", pack)
                .takeIf { it != 0 }
                ?: packContext.resources.getIdentifier(drawableName, "mipmap", pack)
            if (id == 0) return null
            packContext.getDrawable(id)?.toBitmap(sizePx, sizePx)?.asImageBitmap()
        }.getOrNull()
        if (result == null) {
            if (misses.size > 512) misses.remove(misses.first())
            misses += cacheKey
        } else {
            cache.put(cacheKey, result)
        }
        return result
    }

    fun packAppIcon(packageName: String, sizePx: Int): ImageBitmap? = runCatching {
        appContext.packageManager.getApplicationIcon(packageName)
            .toBitmap(sizePx, sizePx)
            .asImageBitmap()
    }.getOrNull()

    private fun load(packageName: String?) {
        if (packageName == null) return
        synchronized(loadLock) {
            if (loadedMappings.containsKey(packageName) && packUpdateTimes[packageName] == updateTime(packageName)) return
            loadNow(packageName)
        }
    }

    private fun loadNow(packageName: String) {
        val result = runCatching { loadMapping(packageName) }
        if (result.isSuccess) {
            loadedMappings[packageName] = result.getOrNull()
            packUpdateTimes[packageName] = updateTime(packageName)
        }
        mappings = HashMap(loadedMappings)
        iconsByPack = HashMap(loadedIcons)
        _version.value += 1
    }

    private fun invalidate(packageName: String) {
        loadedMappings.remove(packageName)
        loadedIcons.remove(packageName)
        loadingIcons.remove(packageName)
        packUpdateTimes.remove(packageName)
        packContexts.remove(packageName)
        mappings = HashMap(loadedMappings)
        iconsByPack = HashMap(loadedIcons)
        cache.evictAll()
        misses.clear()
        _version.value += 1
    }

    private fun updateTime(packageName: String): Long = runCatching {
        appContext.packageManager.getPackageInfo(packageName, 0).lastUpdateTime
    }.getOrDefault(0L)

    private fun queryPacks(): List<IconPackInfo> {
        val intent = Intent("org.adw.launcher.THEMES").addCategory(Intent.CATEGORY_DEFAULT)
        return appContext.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.packageName }
            .distinct()
            .mapNotNull { pkg ->
                runCatching {
                    IconPackInfo(
                        pkg,
                        appContext.packageManager.getApplicationLabel(
                            appContext.packageManager.getApplicationInfo(pkg, 0)
                        ).toString(),
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun loadIcons(packageName: String): List<PackIcon> {
        val packContext = appContext.createPackageContext(packageName, 0)
        runCatching {
            packContext.assets.open("drawable.xml").use { parseIcons(it) }
        }.getOrNull()?.let { if (it.isNotEmpty()) return it }
        val id = packContext.resources.getIdentifier("drawable", "xml", packageName)
        if (id == 0) return emptyList()
        val items = mutableListOf<PackIcon>()
        packContext.resources.getXml(id).use { parser ->
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (!drawable.isNullOrBlank()) {
                        items += PackIcon(drawable, parser.getAttributeValue(null, "name") ?: drawable)
                    }
                }
                event = parser.next()
            }
        }
        return uniqueIcons(items).sortedBy { it.name.lowercase() }
    }

    private fun parseIcons(input: java.io.InputStream): List<PackIcon> = runCatching {
        val factory = DocumentBuilderFactory.newInstance()
        runCatching { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        runCatching { factory.setFeature("http://xml.org/sax/features/external-general-entities", false) }
        runCatching { factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        factory.isExpandEntityReferences = false
        val doc = factory.newDocumentBuilder().parse(input)
        val nodes = doc.getElementsByTagName("item")
        (0 until nodes.length).mapNotNull { index ->
            val element = nodes.item(index) as? org.w3c.dom.Element ?: return@mapNotNull null
            val drawable = element.getAttribute("drawable")
            if (drawable.isBlank()) return@mapNotNull null
            PackIcon(drawable, element.getAttribute("name").ifBlank { drawable })
        }.let(::uniqueIcons).sortedBy { it.name.lowercase() }
    }.getOrDefault(emptyList())

    private fun loadMapping(packageName: String): IconPackMapping? {
        val time = updateTime(packageName)
        val cacheFile = File(appContext.cacheDir, "iconpack-$packageName-$time.txt")
        if (cacheFile.exists()) {
            runCatching {
                val raw = cacheFile.bufferedReader().useLines { lines ->
                    val out = ArrayList<Pair<String, String>>(16000)
                    lines.forEach { line ->
                        val tab = line.indexOf('\t')
                        if (tab > 0) out += line.substring(0, tab) to line.substring(tab + 1)
                    }
                    out
                }
                IconPackMapping.fromItems(raw)
            }.getOrNull()?.let {
                return it
            }
        }
        val packContext = appContext.createPackageContext(packageName, 0)
        val fromAssets = runCatching {
            packContext.assets.open("appfilter.xml").use { IconPackMapping.parse(it) }
        }.getOrNull()
        if (fromAssets != null) return cache(packageName, time, cacheFile, fromAssets)
        val id = packContext.resources.getIdentifier("appfilter", "xml", packageName)
        if (id == 0) return null
        val items = mutableListOf<Pair<String, String>>()
        packContext.resources.getXml(id).use { parser ->
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                        items += component to drawable
                    }
                }
                event = parser.next()
            }
        }
        return cache(packageName, time, cacheFile, IconPackMapping.fromItems(items))
    }

    /** Parsing a 3 MB appfilter costs seconds; the normalised pairs reload in milliseconds. */
    private fun cache(
        packageName: String,
        time: Long,
        file: File,
        mapping: IconPackMapping,
    ): IconPackMapping {
        runCatching {
            val temp = File(file.parentFile, file.name + ".tmp")
            temp.bufferedWriter().use { writer ->
                mapping.entries().forEach { (component, drawable) ->
                    writer.write(component)
                    writer.write("\t")
                    writer.write(drawable)
                    writer.newLine()
                }
            }
            temp.renameTo(file)
            appContext.cacheDir.listFiles { f ->
                f.name.startsWith("iconpack-$packageName-") && f.name != file.name
            }?.forEach { it.delete() }
        }
        return mapping
    }
}
