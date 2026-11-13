package dev.tom.cascade.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.util.sortApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppsRepository(private val context: Context) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    @Volatile
    private var activityInfos: Map<String, LauncherActivityInfo> = emptyMap()

    // Decoded icons are reused while scrolling; remember() alone drops them
    // when a row leaves composition.
    private val iconCache = LruCache<String, ImageBitmap>(96)

    // Themed icons, keyed by app, size and colours; and apps known to have no monochrome layer.
    private val themedCache = LruCache<String, ImageBitmap>(96)
    private val noMonochrome: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refresh()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refresh()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refresh()
    }

    fun start() {
        launcherApps.registerCallback(callback)
        refresh()
    }

    fun stop() {
        launcherApps.unregisterCallback(callback)
        scope.cancel()
    }

    fun refresh() {
        scope.launch {
            val infos = runCatching {
                launcherApps.getActivityList(null, Process.myUserHandle())
            }.getOrDefault(emptyList())
            activityInfos = infos.associateBy { it.componentName.flattenToString() }
            iconCache.evictAll()
            themedCache.evictAll()
            noMonochrome.clear()
            val entries = infos.map {
                AppEntry(
                    key = it.componentName.flattenToString(),
                    label = it.label.toString(),
                    packageName = it.componentName.packageName,
                    activityName = it.componentName.className,
                )
            }
            _apps.value = sortApps(entries)
        }
    }

    fun icon(app: AppEntry, sizePx: Int): ImageBitmap? {
        val cacheKey = "${app.key}@$sizePx"
        iconCache.get(cacheKey)?.let { return it }
        val info = activityInfos[app.key] ?: return null
        return runCatching {
            info.getIcon(context.resources.displayMetrics.densityDpi)
                .toBitmap(sizePx, sizePx)
                .asImageBitmap()
        }.getOrNull()?.also { iconCache.put(cacheKey, it) }
    }

    /**
     * The app's monochrome icon layer (Android 13+) drawn in [ink] on a [plate], or null when
     * the app ships none. Colours are ARGB ints.
     */
    fun themedIcon(app: AppEntry, sizePx: Int, plate: Int, ink: Int): ImageBitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || app.key in noMonochrome) return null
        val cacheKey = "${app.key}@$sizePx#$plate/$ink"
        themedCache.get(cacheKey)?.let { return it }
        val info = activityInfos[app.key] ?: return null
        val monochrome = runCatching {
            (unwrap(info.getIcon(context.resources.displayMetrics.densityDpi)) as? AdaptiveIconDrawable)
                ?.monochrome
        }.getOrNull()
        if (monochrome == null) {
            noMonochrome += app.key
            return null
        }
        return runCatching {
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(plate)
            // Adaptive layers are 108 units with the visible 72 in the middle: 1.5x the icon.
            val bleed = sizePx / 4
            monochrome.mutate().apply {
                setBounds(-bleed, -bleed, sizePx + bleed, sizePx + bleed)
                colorFilter = PorterDuffColorFilter(ink, PorterDuff.Mode.SRC_IN)
                draw(canvas)
            }
            bitmap.asImageBitmap()
        }.getOrNull()?.also { themedCache.put(cacheKey, it) }
    }

    private tailrec fun unwrap(drawable: Drawable?): Drawable? =
        if (drawable is DrawableWrapper) unwrap(drawable.drawable) else drawable

    fun isRemovable(app: AppEntry): Boolean = runCatching {
        val info = launcherApps.getApplicationInfo(app.packageName, 0, Process.myUserHandle())
        info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    }.getOrDefault(false)
}
