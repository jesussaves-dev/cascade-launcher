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
    private val context: Context,
    private val prefs: Prefs,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mappings = HashMap<String, IconPackMapping?>()
    private val cache = LruCache<String, ImageBitmap>(128)

    private val _packs = MutableStateFlow<List<IconPackInfo>>(emptyList())
    val packs: StateFlow<List<IconPackInfo>> = _packs.asStateFlow()
    val activePack: StateFlow<String?> = prefs.iconPack

    fun refreshPacks() {
        scope.launch {
            val found = queryPacks()
            _packs.value = found
            val active = prefs.iconPack.value
            if (active != null && found.none { it.packageName == active }) {
                prefs.setIconPack(null)
            }
        }
    }

    fun setActive(packageName: String?) {
        prefs.setIconPack(packageName)
        cache.evictAll()
    }

    fun icon(app: AppEntry, sizePx: Int): ImageBitmap? {
        val pack = prefs.iconPack.value ?: return null
        val drawableName = mappingFor(pack)?.drawableName(app.packageName, app.activityName) ?: return null
        val cacheKey = "$pack|${app.key}|$sizePx"
        cache.get(cacheKey)?.let { return it }
        val result = runCatching {
            val packContext = context.createPackageContext(pack, 0)
            val id = packContext.resources.getIdentifier(drawableName, "drawable", pack)
                .takeIf { it != 0 }
                ?: packContext.resources.getIdentifier(drawableName, "mipmap", pack)
            if (id == 0) return null
            packContext.getDrawable(id)?.toBitmap(sizePx, sizePx)?.asImageBitmap()
        }.getOrNull() ?: return null
        cache.put(cacheKey, result)
        return result
    }

    fun packAppIcon(packageName: String, sizePx: Int): ImageBitmap? = runCatching {
        context.packageManager.getApplicationIcon(packageName)
            .toBitmap(sizePx, sizePx)
            .asImageBitmap()
    }.getOrNull()

    private fun queryPacks(): List<IconPackInfo> {
        val intent = Intent("org.adw.launcher.THEMES").addCategory(Intent.CATEGORY_DEFAULT)
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.packageName }
            .distinct()
            .mapNotNull { pkg ->
                runCatching {
                    IconPackInfo(pkg, context.packageManager.getApplicationLabel(
                        context.packageManager.getApplicationInfo(pkg, 0)
                    ).toString())
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun mappingFor(packageName: String): IconPackMapping? {
        if (mappings.containsKey(packageName)) return mappings[packageName]
        val mapping = runCatching { loadMapping(packageName) }.getOrNull()
        mappings[packageName] = mapping
        return mapping
    }

    private fun loadMapping(packageName: String): IconPackMapping? {
        val packContext = context.createPackageContext(packageName, 0)
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
