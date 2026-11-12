package dev.tom.cascade.ui

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tom.cascade.CascadeApp
import dev.tom.cascade.data.WeatherRepository
import dev.tom.cascade.util.isFahrenheitLocale
import dev.tom.cascade.data.ContactsRepository
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.model.Folder
import dev.tom.cascade.iconpack.overrideDrawable
import dev.tom.cascade.notifications.NotificationStore
import dev.tom.cascade.ui.theme.CascadeTheme
import dev.tom.cascade.util.favoriteApps
import dev.tom.cascade.util.folderApps
import dev.tom.cascade.util.isDefaultLauncher
import dev.tom.cascade.util.isDeviceAdminActive
import dev.tom.cascade.util.requestDeviceAdmin
import dev.tom.cascade.util.lockDevice
import dev.tom.cascade.util.isNotificationListenerEnabled
import dev.tom.cascade.util.openDefaultLauncherSettings
import dev.tom.cascade.util.openNotificationListenerSettings
import dev.tom.cascade.util.sections
import java.text.Collator
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private lateinit var widgetHost: AppWidgetHost
    private var pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val pickWidget = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val pending = pendingWidgetId
        pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
        val returned = result.data?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (result.resultCode == Activity.RESULT_OK) {
            // A recreated activity loses `pending`; the result still names the widget.
            val id = returned.takeIf { it != AppWidgetManager.INVALID_APPWIDGET_ID } ?: pending
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                (application as CascadeApp).prefs.addWidgetId(id)
            }
            if (pending != AppWidgetManager.INVALID_APPWIDGET_ID && pending != id) {
                widgetHost.deleteAppWidgetId(pending)
            }
        } else if (pending != AppWidgetManager.INVALID_APPWIDGET_ID) {
            widgetHost.deleteAppWidgetId(pending)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PENDING_WIDGET, pendingWidgetId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keepScroll = lastNonConfigurationInstance != null
        pendingWidgetId = savedInstanceState?.getInt(
            STATE_PENDING_WIDGET,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        widgetHost = AppWidgetHost(this, WIDGET_HOST_ID)
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
            val homeTheme by prefs.homeTheme.collectAsStateWithLifecycle()
            val iconSizeDp by prefs.iconSizeDp.collectAsStateWithLifecycle()
            val bannerDismissed by prefs.bannerDismissed.collectAsStateWithLifecycle()
            val showPreviews by prefs.showNotificationPreviews.collectAsStateWithLifecycle()
            val folders by prefs.folders.collectAsStateWithLifecycle()
            val byPackage by NotificationStore.byPackage.collectAsStateWithLifecycle()
            val widgetIds by prefs.widgetIds.collectAsStateWithLifecycle()
            val searchContacts by prefs.searchContacts.collectAsStateWithLifecycle()
            val showWeather by prefs.showWeather.collectAsStateWithLifecycle()
            val place by prefs.place.collectAsStateWithLifecycle()
            val cachedWeather by prefs.cachedWeather.collectAsStateWithLifecycle()
            val showVerse by prefs.showVerse.collectAsStateWithLifecycle()
            val weatherRepository = remember { WeatherRepository(prefs) }
            var weatherTick by remember { mutableIntStateOf(0) }
            LaunchedEffect(showWeather, place, weatherTick) {
                val target = place
                if (showWeather && target != null) weatherRepository.refreshIfStale(target)
            }

            val widgetManager = remember { AppWidgetManager.getInstance(this@HomeActivity) }
            var widgetRefresh by remember { mutableIntStateOf(0) }
            val removeWidget: (Int) -> Unit = { id ->
                widgetHost.deleteAppWidgetId(id)
                prefs.removeWidgetId(id)
            }
            LaunchedEffect(widgetIds) {
                widgetIds.filter { widgetManager.getAppWidgetInfo(it) == null }.forEach(removeWidget)
            }
            val widgetEntries = remember(widgetIds, widgetRefresh) {
                widgetIds.map { id ->
                    val label = widgetManager.getAppWidgetInfo(id)
                        ?.loadLabel(this@HomeActivity.packageManager)
                        ?.toString()
                        ?: "Widget"
                    id to label
                }
            }
            val iconPacks = app.iconPacks
            val packs by iconPacks.packs.collectAsStateWithLifecycle()
            val activeIconPack by iconPacks.activePack.collectAsStateWithLifecycle()
            val packVersion by iconPacks.version.collectAsStateWithLifecycle()
            val iconOverrides by prefs.iconOverrides.collectAsStateWithLifecycle()
            val contactsRepo = remember { ContactsRepository(this@HomeActivity) }
            var contactsGranted by remember { mutableStateOf(hasContactsPermission()) }
            val requestContacts = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                contactsGranted = granted
                prefs.setSearchContacts(granted)
                if (granted) {
                    contactsRepo.refresh()
                } else {
                    Toast.makeText(this@HomeActivity, "Contacts permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            val contactList by contactsRepo.contacts.collectAsStateWithLifecycle()
            val density = LocalDensity.current.density
            val iconPx = (iconSizeDp * density).toInt()
            val iconFor: (AppEntry) -> androidx.compose.ui.graphics.ImageBitmap? = { app ->
                val override = overrideDrawable(iconOverrides, app.key, activeIconPack)
                (if (override != null) {
                    iconPacks.iconByDrawable(activeIconPack!!, override, iconPx)
                } else {
                    null
                }) ?: iconPacks.icon(app, iconPx) ?: repo.icon(app, iconPx)
            }

            var searching by rememberSaveable { mutableStateOf(false) }
            var showSettings by rememberSaveable { mutableStateOf(false) }
            var defaultLauncher by remember { mutableStateOf(true) }
            var notificationsEnabled by remember {
                mutableStateOf(isNotificationListenerEnabled(this@HomeActivity))
            }
            var adminActive by remember { mutableStateOf(isDeviceAdminActive(this@HomeActivity)) }
            var selected by remember { mutableStateOf<AppEntry?>(null) }
            var iconPickerFor by remember { mutableStateOf<AppEntry?>(null) }
            var openedFolder by remember { mutableStateOf<Folder?>(null) }
            var folderAction by remember { mutableStateOf<Folder?>(null) }
            var removingFromFolder by remember { mutableStateOf<Pair<Folder, AppEntry>?>(null) }
            var folderPickerFor by remember { mutableStateOf<AppEntry?>(null) }
            var renamingFolder by remember { mutableStateOf<Folder?>(null) }
            var deletingFolder by remember { mutableStateOf<Folder?>(null) }

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                defaultLauncher = isDefaultLauncher(this@HomeActivity)
                notificationsEnabled = isNotificationListenerEnabled(this@HomeActivity)
                adminActive = isDeviceAdminActive(this@HomeActivity)
                iconPacks.refreshPacks()
                prefs.widgetIds.value
                    .filter { widgetManager.getAppWidgetInfo(it) == null }
                    .forEach(removeWidget)
                widgetRefresh++
                weatherTick++
                contactsGranted = hasContactsPermission()
                if (prefs.searchContacts.value && contactsGranted) {
                    contactsRepo.refresh()
                } else if (prefs.searchContacts.value) {
                    prefs.setSearchContacts(false)
                    contactsRepo.clear()
                }
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

            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            var startHandled by remember { mutableStateOf(false) }
            LaunchedEffect(allApps.isNotEmpty()) {
                if (!keepScroll && !startHandled && allApps.isNotEmpty()) {
                    // Fresh launch (including after a crash) opens on Favourites.
                    listState.scrollToItem(0)
                    startHandled = true
                }
            }

            CascadeTheme(mode = theme, accentIndex = accent, homeTheme = homeTheme) {
                key(activeIconPack, packVersion, iconOverrides) {
                when {
                    showSettings -> {
                        BackHandler { showSettings = false }
                        SettingsScreen(
                            theme = theme,
                            homeTheme = homeTheme,
                            accentIndex = accent,
                            iconSizeDp = iconSizeDp,
                            hiddenApps = allApps.filter { it.key in hidden },
                            iconPacks = packs,
                            activeIconPack = activeIconPack,
                            packIconFor = { pack -> iconPacks.packAppIcon(pack, (40 * density).toInt()) },
                            onIconPack = { iconPacks.setActive(it) },
                            notificationsEnabled = notificationsEnabled,
                            showPreviews = showPreviews,
                            onShowPreviews = prefs::setShowNotificationPreviews,
                            onOpenNotificationAccess = {
                                openNotificationListenerSettings(this@HomeActivity)
                            },
                            widgets = widgetEntries,
                            onAddWidget = ::addWidget,
                            onRemoveWidget = removeWidget,
                            searchContacts = searchContacts,
                            onSearchContacts = { enabled ->
                                if (!enabled) {
                                    prefs.setSearchContacts(false)
                                    contactsRepo.clear()
                                } else if (contactsGranted) {
                                    prefs.setSearchContacts(true)
                                    contactsRepo.refresh()
                                } else {
                                    requestContacts.launch(Manifest.permission.READ_CONTACTS)
                                }
                            },
                            showWeather = showWeather,
                            place = place,
                            onShowWeather = prefs::setShowWeather,
                            onPickPlace = { picked ->
                                prefs.setPlace(picked)
                                prefs.setCachedWeather(null)
                                prefs.setShowWeather(picked != null)
                            },
                            showVerse = showVerse,
                            onShowVerse = prefs::setShowVerse,
                            onSearchPlaces = weatherRepository::searchPlaces,
                            adminActive = adminActive,
                            onToggleAdmin = { requestDeviceAdmin(this@HomeActivity) },
                            onTheme = prefs::setTheme,
                            onHomeTheme = prefs::setHomeTheme,
                            onAccent = prefs::setAccent,
                            onIconSize = prefs::setIconSize,
                            onUnhide = prefs::unhide,
                            onSetDefault = { openDefaultLauncherSettings(this@HomeActivity) },
                            onBack = { showSettings = false },
                        )
                    }

                    searching -> {
                        BackHandler { searching = false }
                        SearchOverlay(
                            contacts = if (searchContacts && contactsGranted) contactList else emptyList(),
                            onOpenContact = { contact ->
                                searching = false
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", contact.number, null),
                                )
                                runCatching { startActivity(intent) }.onFailure {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "No dialer available",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            onWebSearch = { query ->
                                searching = false
                                openWebSearch(query)
                            },
                            iconFor = iconFor,
                            iconSizeDp = iconSizeDp,
                            onLaunch = {
                                searching = false
                                launchApp(it)
                            },
                            apps = visibleApps,
                            onDismiss = { searching = false },
                        )
                    }

                    else -> {
                        BackHandler { /* launcher home: Back does nothing */ }
                        HomeScreen(
                            sections = appSections,
                            favorites = favorites,
                            onFavoriteMoved = prefs::setFavoriteOrder,
                            showBanner = !bannerDismissed && !defaultLauncher,
                            onDismissBanner = prefs::dismissBanner,
                            onSetDefault = { openDefaultLauncherSettings(this@HomeActivity) },
                            onLaunch = ::launchApp,
                            onLongPress = { selected = it },
                            onOpenSearch = { searching = true },
                            onOpenSettings = { showSettings = true },
                            iconFor = iconFor,
                            iconSizeDp = iconSizeDp,
                            notifiedPackages = notifiedPackages,
                            folderContents = folderContents,
                            onFolderOpen = { openedFolder = it },
                            onFolderLongPress = { folderAction = it },
                            widgets = { WidgetArea(widgetHost, widgetIds, removeWidget) },
                            weather = cachedWeather?.takeIf {
                                showWeather && System.currentTimeMillis() - it.updatedAtMillis < 24 * 60 * 60_000
                            },
                            showWeather = showWeather,
                            showVerse = showVerse,
                            fahrenheit = isFahrenheitLocale(Locale.getDefault()),
                            listState = listState,
                            onClockDoubleTap = {
                                if (!lockDevice(this@HomeActivity)) {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "Turn on double-tap to lock in Settings",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
                            onClockLongPress = {
                                runCatching {
                                    startActivity(
                                        Intent(Intent.ACTION_SET_WALLPAPER)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }.onFailure {
                                    Toast.makeText(
                                        this@HomeActivity,
                                        "No wallpaper picker available",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
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
                            prefs.toggleFavorite(app.key)
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
                        canChooseIcon = activeIconPack != null,
                        onChooseIcon = {
                            selected = null
                            iconPickerFor = app
                        },
                        onDismiss = { selected = null },
                    )
                }
                iconPickerFor?.let { app ->
                    IconPickerDialog(
                        appLabel = app.label,
                        packageName = app.packageName,
                        icons = iconPacks.icons(activeIconPack),
                        hasOverride = app.key in iconOverrides,
                        iconFor = { icon ->
                            activeIconPack?.let { iconPacks.iconByDrawable(it, icon.drawable, iconPx) }
                        },
                        onPick = { icon ->
                            prefs.setIconOverride(app.key, icon.drawable)
                            iconPickerFor = null
                        },
                        onReset = {
                            prefs.clearIconOverride(app.key)
                            iconPickerFor = null
                        },
                        onDismiss = { iconPickerFor = null },
                    )
                }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        widgetHost.stopListening()
        super.onStop()
    }

    private fun addWidget() {
        if (pendingWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) return
        val id = widgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        pendingWidgetId = id
        if (intent.resolveActivity(packageManager) == null) {
            widgetHost.deleteAppWidgetId(id)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            Toast.makeText(this, "No widget picker available", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { pickWidget.launch(intent) }.onFailure {
            widgetHost.deleteAppWidgetId(id)
            pendingWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            Toast.makeText(this, "No widget picker available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    private fun openWebSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query)
        runCatching { startActivity(intent) }.recoverCatching {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://duckduckgo.com/?q=${Uri.encode(query)}")),
            )
        }.onFailure {
            Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val WIDGET_HOST_ID = 1024
        private const val STATE_PENDING_WIDGET = "pending_widget_id"
    }
}
