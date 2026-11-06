package dev.tom.cascade.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
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

    fun isRemovable(app: AppEntry): Boolean = runCatching {
        val info = launcherApps.getApplicationInfo(app.packageName, 0, Process.myUserHandle())
        info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    }.getOrDefault(false)
}
