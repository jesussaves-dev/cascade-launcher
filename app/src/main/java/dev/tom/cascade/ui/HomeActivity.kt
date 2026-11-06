package dev.tom.cascade.ui

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tom.cascade.CascadeApp
import dev.tom.cascade.data.ButtonAction
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.ui.theme.CascadeTheme
import dev.tom.cascade.util.canFavorite
import dev.tom.cascade.util.favoriteApps
import dev.tom.cascade.util.isDefaultLauncher
import dev.tom.cascade.util.openDefaultLauncherSettings
import dev.tom.cascade.util.sections
import java.text.Collator

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as CascadeApp
            val prefs = app.prefs
            val repo = app.apps

            val allApps by repo.apps.collectAsStateWithLifecycle()
            val favoriteKeys by prefs.favorites.collectAsStateWithLifecycle()
            val hidden by prefs.hidden.collectAsStateWithLifecycle()
            val theme by prefs.theme.collectAsStateWithLifecycle()
            val accent by prefs.accentIndex.collectAsStateWithLifecycle()
            val iconSizeDp by prefs.iconSizeDp.collectAsStateWithLifecycle()
            val bannerDismissed by prefs.bannerDismissed.collectAsStateWithLifecycle()
            val buttonTap by prefs.buttonTap.collectAsStateWithLifecycle()
            val buttonSwipe by prefs.buttonSwipe.collectAsStateWithLifecycle()
            val density = LocalDensity.current.density
            val iconPx = (iconSizeDp * density).toInt()
            val iconFor: (AppEntry) -> androidx.compose.ui.graphics.ImageBitmap? = { repo.icon(it, iconPx) }

            var searching by rememberSaveable { mutableStateOf(false) }
            var showSettings by rememberSaveable { mutableStateOf(false) }
            var defaultLauncher by remember { mutableStateOf(true) }
            var selected by remember { mutableStateOf<AppEntry?>(null) }

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                defaultLauncher = isDefaultLauncher(this@HomeActivity)
            }

            val collator = remember { Collator.getInstance() }
            val favorites = remember(allApps, favoriteKeys, hidden) {
                favoriteApps(allApps, favoriteKeys, hidden)
            }
            val appSections = remember(allApps, favoriteKeys, hidden, collator) {
                sections(allApps, favoriteKeys, hidden, collator)
            }
            val visibleApps = remember(allApps, hidden) {
                allApps.filterNot { it.key in hidden }
            }
            val installedFavoriteCount = remember(allApps, favoriteKeys) {
                allApps.count { it.key in favoriteKeys }
            }

            fun runAction(action: ButtonAction) {
                when (action) {
                    ButtonAction.Search -> searching = true
                    ButtonAction.Voice -> startVoiceAssistant()
                    is ButtonAction.OpenApp ->
                        allApps.firstOrNull { it.key == action.key }?.let(::launchApp)
                }
            }

            CascadeTheme(mode = theme, accentIndex = accent) {
                when {
                    showSettings -> {
                        BackHandler { showSettings = false }
                        SettingsScreen(
                            theme = theme,
                            accentIndex = accent,
                            iconSizeDp = iconSizeDp,
                            hiddenApps = allApps.filter { it.key in hidden },
                            buttonTap = buttonTap,
                            buttonSwipe = buttonSwipe,
                            apps = visibleApps,
                            onTheme = prefs::setTheme,
                            onAccent = prefs::setAccent,
                            onIconSize = prefs::setIconSize,
                            onUnhide = prefs::unhide,
                            onButtonTap = prefs::setButtonTap,
                            onButtonSwipe = prefs::setButtonSwipe,
                            onSetDefault = { openDefaultLauncherSettings(this@HomeActivity) },
                            onBack = { showSettings = false },
                        )
                    }

                    searching -> {
                        BackHandler { searching = false }
                        SearchOverlay(
                            apps = visibleApps,
                            iconFor = iconFor,
                            iconSizeDp = iconSizeDp,
                            onLaunch = {
                                searching = false
                                launchApp(it)
                            },
                            onDismiss = { searching = false },
                        )
                    }

                    else -> {
                        BackHandler { /* launcher home: Back does nothing */ }
                        HomeScreen(
                            sections = appSections,
                            favorites = favorites,
                            showBanner = !bannerDismissed && !defaultLauncher,
                            onDismissBanner = prefs::dismissBanner,
                            onSetDefault = { openDefaultLauncherSettings(this@HomeActivity) },
                            onLaunch = ::launchApp,
                            onLongPress = { selected = it },
                            onOpenSearch = { searching = true },
                            onOpenSettings = { showSettings = true },
                            onButtonTap = { runAction(buttonTap) },
                            onButtonSwipeUp = { runAction(buttonSwipe) },
                            iconFor = iconFor,
                            iconSizeDp = iconSizeDp,
                        )
                    }
                }

                selected?.let { app ->
                    AppActionSheet(
                        app = app,
                        pinned = app.key in favoriteKeys,
                        removable = repo.isRemovable(app),
                        onPinToggle = {
                            if (canFavorite(favoriteKeys, app.key, installedFavoriteCount)) {
                                prefs.toggleFavorite(app.key)
                            } else {
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Favourites are full — unpin one first",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                            selected = null
                        },
                        onAppInfo = {
                            selected = null
                            openAppInfo(app)
                        },
                        onUninstall = {
                            selected = null
                            uninstall(app)
                        },
                        onHide = {
                            prefs.hide(app.key)
                            selected = null
                        },
                        onDismiss = { selected = null },
                    )
                }
            }
        }
    }

    private fun launchApp(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        startActivity(intent)
    }

    private fun openAppInfo(app: AppEntry) {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${app.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun startVoiceAssistant() {
        val candidates = listOf(
            Intent(Intent.ACTION_ASSIST).addCategory(Intent.CATEGORY_DEFAULT),
            Intent(Intent.ACTION_VOICE_COMMAND),
        )
        for (intent in candidates) {
            try {
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
                // try the next one
            }
        }
        Toast.makeText(this, "No voice assistant available", Toast.LENGTH_SHORT).show()
    }

    private fun uninstall(app: AppEntry) {
        startActivity(
            Intent(Intent.ACTION_DELETE)
                .setData(Uri.parse("package:${app.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
