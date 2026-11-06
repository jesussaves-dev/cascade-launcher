package dev.tom.cascade.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    fun toggleFavorite(key: String) {
        val next = _favorites.value.toMutableList()
        if (!next.remove(key)) next.add(key)
        _favorites.value = next
        prefs.edit().putString(KEY_FAVORITES, next.joinToString("|")).apply()
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

    companion object {
        const val PREFS_NAME = "cascade"
        const val DEFAULT_ICON_SIZE_DP = 40
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_HIDDEN = "hidden"
        private const val KEY_THEME = "theme"
        private const val KEY_ACCENT = "accent"
        private const val KEY_ICON_SIZE = "icon_size"
        private const val KEY_BANNER_DISMISSED = "banner_dismissed"
    }
}
