package dev.tom.cascade.data

import android.content.Context
import dev.tom.cascade.iconpack.IconOverrideCodec
import dev.tom.cascade.model.Folder
import dev.tom.cascade.model.Place
import dev.tom.cascade.model.Weather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Component keys never contain "|", so it is a safe list separator.
    private val _favorites = MutableStateFlow(
        prefs.getString(KEY_FAVORITES, null)
            ?.split("|")
            ?.filter { it.isNotEmpty() }
            .orEmpty()
    )
    val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val _hidden = MutableStateFlow(
        prefs.getStringSet(KEY_HIDDEN, null).orEmpty().toSet()
    )
    val hidden: StateFlow<Set<String>> = _hidden.asStateFlow()

    private val _theme = MutableStateFlow(
        prefs.getString(KEY_THEME, null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
    )
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _accentIndex = MutableStateFlow(prefs.getInt(KEY_ACCENT, 0))
    val accentIndex: StateFlow<Int> = _accentIndex.asStateFlow()

    private val _iconSizeDp = MutableStateFlow(prefs.getInt(KEY_ICON_SIZE, DEFAULT_ICON_SIZE_DP))
    val iconSizeDp: StateFlow<Int> = _iconSizeDp.asStateFlow()

    private val _bannerDismissed = MutableStateFlow(prefs.getBoolean(KEY_BANNER_DISMISSED, false))
    val bannerDismissed: StateFlow<Boolean> = _bannerDismissed.asStateFlow()

    private val _buttonTap = MutableStateFlow(
        decodeButtonAction(prefs.getString(KEY_BUTTON_TAP, null), ButtonAction.Search)
    )
    val buttonTap: StateFlow<ButtonAction> = _buttonTap.asStateFlow()

    private val _buttonSwipe = MutableStateFlow(
        decodeButtonAction(prefs.getString(KEY_BUTTON_SWIPE, null), ButtonAction.Voice)
    )
    val buttonSwipe: StateFlow<ButtonAction> = _buttonSwipe.asStateFlow()

    private val _showNotificationPreviews =
        MutableStateFlow(prefs.getBoolean(KEY_SHOW_NOTIFICATION_PREVIEWS, true))
    val showNotificationPreviews: StateFlow<Boolean> = _showNotificationPreviews.asStateFlow()

    private val _searchContacts = MutableStateFlow(prefs.getBoolean(KEY_SEARCH_CONTACTS, false))
    val searchContacts: StateFlow<Boolean> = _searchContacts.asStateFlow()

    private val _folders = MutableStateFlow(FolderCodec.decode(prefs.getString(KEY_FOLDERS, null)))
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _iconPack = MutableStateFlow(
        prefs.getString(KEY_ICON_PACK, null)?.takeIf { it.isNotEmpty() }
    )
    val iconPack: StateFlow<String?> = _iconPack.asStateFlow()

    private val _iconOverrides = MutableStateFlow(
        IconOverrideCodec.decode(prefs.getString(KEY_ICON_OVERRIDES, null))
    )
    val iconOverrides: StateFlow<Map<String, String>> = _iconOverrides.asStateFlow()

    private val _widgetIds = MutableStateFlow(WidgetIds.decode(prefs.getString(KEY_WIDGET_IDS, null)))
    val widgetIds: StateFlow<List<Int>> = _widgetIds.asStateFlow()

    fun toggleFavorite(key: String) {
        val next = _favorites.value.toMutableList()
        if (!next.remove(key)) next.add(key)
        writeFavorites(next)
    }

    fun createFolder(name: String, appKey: String): Folder {
        val folder = Folder(
            id = UUID.randomUUID().toString(),
            name = sanitizeFolderName(name).ifEmpty { "Folder" },
            appKeys = emptyList(),
        )
        writeFolders(moveApp(_folders.value + folder, folder.id, appKey))
        writeFavorites(_favorites.value - appKey)
        return folder
    }

    fun addToFolder(folderId: String, appKey: String) {
        if (_folders.value.none { it.id == folderId }) return
        writeFolders(moveApp(_folders.value, folderId, appKey))
        writeFavorites(_favorites.value - appKey)
    }

    fun removeFromFolder(folderId: String, appKey: String) {
        writeFolders(
            _folders.value.map { folder ->
                if (folder.id == folderId) folder.copy(appKeys = folder.appKeys - appKey) else folder
            }
        )
    }

    fun renameFolder(folderId: String, name: String) {
        val clean = sanitizeFolderName(name)
        if (clean.isEmpty()) return
        writeFolders(_folders.value.map { if (it.id == folderId) it.copy(name = clean) else it })
    }

    fun deleteFolder(folderId: String) {
        writeFolders(deleteFolder(_folders.value, folderId))
    }

    private fun writeFavorites(next: List<String>) {
        _favorites.value = next
        prefs.edit().putString(KEY_FAVORITES, next.joinToString("|")).apply()
    }

    private fun writeFolders(next: List<Folder>) {
        _folders.value = next
        prefs.edit().putString(KEY_FOLDERS, FolderCodec.encode(next)).apply()
    }

    fun hide(key: String) {
        val next = _hidden.value + key
        _hidden.value = next
        prefs.edit().putStringSet(KEY_HIDDEN, next).apply()
    }

    fun unhide(key: String) {
        val next = _hidden.value - key
        _hidden.value = next
        prefs.edit().putStringSet(KEY_HIDDEN, next).apply()
    }

    fun setTheme(mode: ThemeMode) {
        _theme.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setAccent(index: Int) {
        _accentIndex.value = index
        prefs.edit().putInt(KEY_ACCENT, index).apply()
    }

    fun setIconSize(dp: Int) {
        _iconSizeDp.value = dp
        prefs.edit().putInt(KEY_ICON_SIZE, dp).apply()
    }

    fun dismissBanner() {
        _bannerDismissed.value = true
        prefs.edit().putBoolean(KEY_BANNER_DISMISSED, true).apply()
    }

    fun setButtonTap(action: ButtonAction) {
        _buttonTap.value = action
        prefs.edit().putString(KEY_BUTTON_TAP, encodeButtonAction(action)).apply()
    }

    fun setButtonSwipe(action: ButtonAction) {
        _buttonSwipe.value = action
        prefs.edit().putString(KEY_BUTTON_SWIPE, encodeButtonAction(action)).apply()
    }

    fun setIconPack(packageName: String?) {
        _iconPack.value = packageName
        prefs.edit().putString(KEY_ICON_PACK, packageName.orEmpty()).apply()
    }

    fun setWidgetIds(ids: List<Int>) {
        _widgetIds.value = ids
        prefs.edit().putString(KEY_WIDGET_IDS, WidgetIds.encode(ids)).apply()
    }

    fun addWidgetId(id: Int) {
        if (id !in _widgetIds.value) setWidgetIds(_widgetIds.value + id)
    }

    fun removeWidgetId(id: Int) {
        setWidgetIds(_widgetIds.value - id)
    }

    fun setIconOverride(appKey: String, drawable: String) {
        writeIconOverrides(_iconOverrides.value + (appKey to drawable))
    }

    fun clearIconOverride(appKey: String) {
        writeIconOverrides(_iconOverrides.value - appKey)
    }

    private fun writeIconOverrides(next: Map<String, String>) {
        _iconOverrides.value = next
        prefs.edit().putString(KEY_ICON_OVERRIDES, IconOverrideCodec.encode(next)).apply()
    }

    fun setShowNotificationPreviews(enabled: Boolean) {
        _showNotificationPreviews.value = enabled
        prefs.edit().putBoolean(KEY_SHOW_NOTIFICATION_PREVIEWS, enabled).apply()
    }

    fun setSearchContacts(enabled: Boolean) {
        _searchContacts.value = enabled
        prefs.edit().putBoolean(KEY_SEARCH_CONTACTS, enabled).apply()
    }

    private val _showWeather = MutableStateFlow(prefs.getBoolean(KEY_SHOW_WEATHER, false))
    val showWeather: StateFlow<Boolean> = _showWeather.asStateFlow()

    private val _place = MutableStateFlow(
        PrefsCodec.decodePlace(prefs.getString(KEY_WEATHER_PLACE, null))
    )
    val place: StateFlow<Place?> = _place.asStateFlow()

    private val _cachedWeather = MutableStateFlow(
        PrefsCodec.decodeWeather(prefs.getString(KEY_WEATHER_CACHE, null))
    )
    val cachedWeather: StateFlow<Weather?> = _cachedWeather.asStateFlow()

    private val _showVerse = MutableStateFlow(prefs.getBoolean(KEY_SHOW_VERSE, false))
    val showVerse: StateFlow<Boolean> = _showVerse.asStateFlow()

    fun setShowWeather(enabled: Boolean) {
        _showWeather.value = enabled
        prefs.edit().putBoolean(KEY_SHOW_WEATHER, enabled).apply()
    }

    fun setPlace(place: Place?) {
        _place.value = place
        prefs.edit().apply {
            if (place == null) remove(KEY_WEATHER_PLACE) else putString(KEY_WEATHER_PLACE, PrefsCodec.encodePlace(place))
        }.apply()
    }

    fun setCachedWeather(weather: Weather?) {
        _cachedWeather.value = weather
        prefs.edit().apply {
            if (weather == null) remove(KEY_WEATHER_CACHE) else putString(KEY_WEATHER_CACHE, PrefsCodec.encodeWeather(weather))
        }.apply()
    }

    fun setShowVerse(enabled: Boolean) {
        _showVerse.value = enabled
        prefs.edit().putBoolean(KEY_SHOW_VERSE, enabled).apply()
    }

    companion object {
        const val PREFS_NAME = "cascade"
        const val DEFAULT_ICON_SIZE_DP = 40
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_HIDDEN = "hidden"
        private const val KEY_THEME = "theme"
        private const val KEY_ACCENT = "accent"
        private const val KEY_ICON_SIZE = "icon_size"
        private const val KEY_BANNER_DISMISSED = "banner_dismissed"
        private const val KEY_BUTTON_TAP = "button_tap"
        private const val KEY_BUTTON_SWIPE = "button_swipe"
        private const val KEY_SHOW_NOTIFICATION_PREVIEWS = "show_notification_previews"
        private const val KEY_SEARCH_CONTACTS = "search_contacts"
        private const val KEY_FOLDERS = "folders"
        private const val KEY_ICON_PACK = "icon_pack"
        private const val KEY_ICON_OVERRIDES = "icon_overrides"
        private const val KEY_WIDGET_IDS = "widget_ids"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_WEATHER_PLACE = "weather_place"
        private const val KEY_WEATHER_CACHE = "weather_cache"
        private const val KEY_SHOW_VERSE = "show_verse"
    }
}
