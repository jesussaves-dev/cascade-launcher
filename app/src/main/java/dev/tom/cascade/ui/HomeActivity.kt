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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tom.cascade.CascadeApp
import dev.tom.cascade.data.ButtonAction
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder
import dev.tom.cascade.notifications.NotificationStore
import dev.tom.cascade.ui.theme.CascadeTheme
import dev.tom.cascade.util.canFavorite
import dev.tom.cascade.util.favoriteApps
import dev.tom.cascade.util.folderApps
import dev.tom.cascade.util.isDefaultLauncher
import dev.tom.cascade.util.isNotificationListenerEnabled
import dev.tom.cascade.util.openDefaultLauncherSettings
import dev.tom.cascade.util.openNotificationListenerSettings
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
            val showPreviews by prefs.showNotificationPreviews.collectAsStateWithLifecycle()
            val folders by prefs.folders.collectAsStateWithLifecycle()
            val byPackage by NotificationStore.byPackage.collectAsStateWithLifecycle()
            val density = LocalDensity.current.density
            val iconPx = (iconSizeDp * density).toInt()
            val iconFor: (AppEntry) -> androidx.compose.ui.graphics.ImageBitmap? = { repo.icon(it, iconPx) }

            var searching by rememberSaveable { mutableStateOf(false) }
            var showSettings by rememberSaveable { mutableStateOf(false) }
            var defaultLauncher by remember { mutableStateOf(true) }
            var notificationsEnabled by remember {
                mutableStateOf(isNotificationListenerEnabled(this@HomeActivity))
            }
            var selected by remember { mutableStateOf<AppEntry?>(null) }
            var openedFolder by remember { mutableStateOf<Folder?>(null) }
            var folderAction by remember { mutableStateOf<Folder?>(null) }
            var removingFromFolder by remember { mutableStateOf<Pair<Folder, AppEntry>?>(null) }
            var folderPickerFor by remember { mutableStateOf<AppEntry?>(null) }
            var renamingFolder by remember { mutableStateOf<Folder?>(null) }
            var deletingFolder by remember { mutableStateOf<Folder?>(null) }

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                defaultLauncher = isDefaultLauncher(this@HomeActivity)
                notificationsEnabled = isNotificationListenerEnabled(this@HomeActivity)
            }

            val notifiedPackages = if (notificationsEnabled) byPackage.keys else emptySet()

            val collator = remember { Collator.getInstance() }
            val favorites = remember(allApps, favoriteKeys, hidden, folders) {
                favoriteApps(allApps, favoriteKeys, hidden, folders)
            }
            val appSections = remember(allApps, favoriteKeys, hidden, collator, folders) {
                sections(allApps, favoriteKeys, hidden, collator, folders)
            }
            val folderContents = remember(allApps, folders, hidden) {
                folders.associate { it.id to folderApps(it, allApps, hidden) }
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
                            notificationsEnabled = notificationsEnabled,
                            showPreviews = showPreviews,
                            onShowPreviews = prefs::setShowNotificationPreviews,
                            onOpenNotificationAccess = {
                                openNotificationListenerSettings(this@HomeActivity)
                            },
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
                            notifiedPackages = notifiedPackages,
                            folderContents = folderContents,
                            onFolderOpen = { openedFolder = it },
                            onFolderLongPress = { folderAction = it },
                        )
                    }
                }

                folderPickerFor?.let { target ->
                    AddToFolderDialog(
                        folders = folders,
                        onPick = { folderId ->
                            prefs.addToFolder(folderId, target.key)
                            folderPickerFor = null
                        },
                        onCreate = { name ->
                            prefs.createFolder(name, target.key)
                            folderPickerFor = null
                        },
                        onDismiss = { folderPickerFor = null },
                    )
                }

                folderAction?.let { folder ->
                    FolderActionSheet(
                        folder = folder,
                        onRename = {
                            folderAction = null
                            renamingFolder = folder
                        },
                        onDelete = {
                            folderAction = null
                            deletingFolder = folder
                        },
                        onDismiss = { folderAction = null },
                    )
                }

                renamingFolder?.let { folder ->
                    NameFolderDialog(
                        title = "Rename folder",
                        initial = folder.name,
                        onConfirm = { name ->
                            prefs.renameFolder(folder.id, name)
                            renamingFolder = null
                        },
                        onDismiss = { renamingFolder = null },
                    )
                }

                deletingFolder?.let { folder ->
                    AlertDialog(
                        onDismissRequest = { deletingFolder = null },
                        title = { Text("Delete folder?") },
                        text = { Text("Apps inside return to the list.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    prefs.deleteFolder(folder.id)
                                    deletingFolder = null
                                },
                            ) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { deletingFolder = null }) { Text("Cancel") }
                        },
                    )
                }

                removingFromFolder?.let { (folder, app) ->
                    AlertDialog(
                        onDismissRequest = { removingFromFolder = null },
                        title = { Text("Remove from ${folder.name}?") },
                        text = { Text("${app.label} returns to the app list.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    prefs.removeFromFolder(folder.id, app.key)
                                    removingFromFolder = null
                                },
                            ) { Text("Remove") }
                        },
                        dismissButton = {
                            TextButton(onClick = { removingFromFolder = null }) { Text("Cancel") }
                        },
                    )
                }

                openedFolder?.let { folder ->
                    FolderSheet(
                        folder = folder,
                        apps = folderContents[folder.id].orEmpty(),
                        iconFor = iconFor,
                        onLaunch = {
                            openedFolder = null
                            launchApp(it)
                        },
                        onLongPressApp = { removingFromFolder = folder to it },
                        onDismiss = { openedFolder = null },
                    )
                }

                selected?.let { app ->
                    AppActionSheet(
                        app = app,
                        pinned = app.key in favoriteKeys,
                        removable = repo.isRemovable(app),
                        notifications = if (notificationsEnabled) {
                            byPackage[app.packageName].orEmpty()
                        } else {
                            emptyList()
                        },
                        showPreviews = showPreviews && notificationsEnabled,
                        onOpenNotification = { item ->
                            selected = null
                            runCatching { item.contentIntent?.send() }
                        },
                        onDismissNotification = { NotificationStore.dismiss(it.key) },
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
                        onAddToFolder = {
                            selected = null
                            folderPickerFor = app
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
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            (application as CascadeApp).apps.refresh()
            Toast.makeText(this, "${app.label} is not installed", Toast.LENGTH_SHORT).show()
        }
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
