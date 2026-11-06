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

data class IconPackInfo(val packageName: String, val label: String)

class IconPackRepository(
    context: Context,
    private val prefs: Prefs,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Published wholesale so composition can read them without locking.
    @Volatile private var mappings: Map<String, IconPackMapping?> = emptyMap()
    private val loadedMappings = HashMap<String, IconPackMapping?>()
    private val packUpdateTimes = HashMap<String, Long>()
    private val packContexts = HashMap<String, Context>()
    private val misses = LinkedHashSet<String>()
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
        val result = runCatching { loadMapping(packageName) }
        if (result.isSuccess) {
            loadedMappings[packageName] = result.getOrNull()
            packUpdateTimes[packageName] = updateTime(packageName)
        }
        mappings = HashMap(loadedMappings)
        _version.value += 1
    }

    private fun invalidate(packageName: String) {
        loadedMappings.remove(packageName)
        packUpdateTimes.remove(packageName)
        packContexts.remove(packageName)
        mappings = HashMap(loadedMappings)
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

    private fun loadMapping(packageName: String): IconPackMapping? {
        val packContext = appContext.createPackageContext(packageName, 0)
        val fromAssets = runCatching {
            packContext.assets.open("appfilter.xml").use { IconPackMapping.parse(it) }
        }.getOrNull()
        if (fromAssets != null) return fromAssets
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
        return IconPackMapping.fromItems(items)
    }
}
