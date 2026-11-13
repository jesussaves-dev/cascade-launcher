package dev.tom.cascade.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.tom.cascade.data.HomeTheme
import dev.tom.cascade.data.ThemeMode

// Cascade 1.0 accents: Tailwind blue, emerald, amber, red, violet, pink.
private val OriginalAccents: List<Color> = listOf(
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
)

// Glacier, Moss, Ochre, Ember, Iris, Rose. Tuned for the slate background.
private val IndexAccents: List<Color> = listOf(
    Color(0xFF62C3DB),
    Color(0xFF7DBB8A),
    Color(0xFFE2A84B),
    Color(0xFFE5654F),
    Color(0xFF9C8CF2),
    Color(0xFFE58AB4),
)

// The same hues, deepened to hold 4.5:1 on the light paper background.
private val IndexAccentsOnLight: List<Color> = listOf(
    Color(0xFF136F86),
    Color(0xFF2F6B3D),
    Color(0xFF8A5A0B),
    Color(0xFFA8341F),
    Color(0xFF5543B8),
    Color(0xFFA23468),
)

/**
 * One light or dark colouring of a theme. A single accent means the theme fixes it.
 * [iconFilter] replaces the style's, for palettes where its icons would vanish.
 */
private class Palette(
    val scheme: ColorScheme,
    val accents: List<Color>,
    val ground: Brush? = null,
    val iconFilter: ColorFilter? = null,
)

private fun palette(
    dark: Boolean,
    background: Long,
    text: Long,
    surface: Long,
    fill: Long,
    muted: Long,
    outline: Long,
    outlineVariant: Long,
    accents: List<Color>,
    ground: Brush? = null,
    iconFilter: ColorFilter? = null,
): Palette {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return Palette(
        scheme = base.copy(
            background = Color(background),
            onBackground = Color(text),
            surface = Color(surface),
            onSurface = Color(text),
            surfaceVariant = Color(fill),
            onSurfaceVariant = Color(muted),
            outline = Color(outline),
            outlineVariant = Color(outlineVariant),
        ),
        accents = accents,
        ground = ground,
        iconFilter = iconFilter,
    )
}

private val OriginalDark = Palette(darkColorScheme(), OriginalAccents)
private val OriginalLight = Palette(lightColorScheme(), OriginalAccents)

private val Slate = palette(
    dark = true, background = 0xFF0F1B22, text = 0xFFE4EBEE, surface = 0xFF16252E, fill = 0xFF1E313B,
    muted = 0xFF8CA0AA, outline = 0xFF34505D, outlineVariant = 0xFF243A45, accents = IndexAccents,
)
private val Paper = palette(
    dark = false, background = 0xFFE8EDEF, text = 0xFF13232B, surface = 0xFFF3F6F7, fill = 0xFFD6DFE3,
    muted = 0xFF4A5F69, outline = 0xFF8FA2AB, outlineVariant = 0xFFBCCAD0, accents = IndexAccentsOnLight,
)

// Rubric: a printed page with headings in red. The outline is the red search rule.
private val RubricPage = palette(
    dark = false, background = 0xFFF2EEE4, text = 0xFF1C1A17, surface = 0xFFF8F5EE, fill = 0xFFE6E0D2,
    muted = 0xFF5E574C, outline = 0xFFB3261E, outlineVariant = 0xFFD3CBB9, accents = listOf(Color(0xFFB3261E)),
)
private val RubricInk = palette(
    dark = true, background = 0xFF1C1A17, text = 0xFFEDE6D6, surface = 0xFF24211D, fill = 0xFF2E2A24,
    muted = 0xFFA69C8A, outline = 0xFFE0695F, outlineVariant = 0xFF3A352D, accents = listOf(Color(0xFFE0695F)),
)

private val PixelScreen = palette(
    dark = true, background = 0xFF101418, text = 0xFFE9F1EC, surface = 0xFF161C21, fill = 0xFF1F2830,
    muted = 0xFF7E9488, outline = 0xFF7E9488, outlineVariant = 0xFF2A363D, accents = listOf(Color(0xFFF2C14E)),
)
private val PixelLcd = palette(
    dark = false, background = 0xFFE9F1EC, text = 0xFF101418, surface = 0xFFF2F7F4, fill = 0xFFD5E0DA,
    muted = 0xFF4C5E55, outline = 0xFF4C5E55, outlineVariant = 0xFFB9C8C0, accents = listOf(Color(0xFF8A5A0B)),
)

// Tide lets the wallpaper through a water gradient; its search pill is translucent.
private val TideDeep = palette(
    dark = true, background = 0xFF0B2A35, text = 0xFFEAF6F7, surface = 0xFF103440, fill = 0x1AEAF6F7,
    muted = 0xFF9BC3CB, outline = 0xFF2E5D6B, outlineVariant = 0xFF1D4553, accents = listOf(Color(0xFF8FE3E0)),
    ground = Brush.verticalGradient(listOf(Color(0xFF1D5466), Color(0xFF0B2A35), Color(0xFF061A21))),
)
private val TideShallow = palette(
    dark = false, background = 0xFFE3F1F3, text = 0xFF0B2A35, surface = 0xFFEEF7F8, fill = 0x140B2A35,
    muted = 0xFF3D6470, outline = 0xFF9BC3CB, outlineVariant = 0xFFC4DDE2, accents = listOf(Color(0xFF17706E)),
    ground = Brush.verticalGradient(listOf(Color(0xFFF2FAFB), Color(0xFFD8ECEF), Color(0xFFC6E2E6))),
)

// Night watch: OLED black and red only, so night vision survives.
private val NightWatch = palette(
    dark = true, background = 0xFF000000, text = 0xFFC9483C, surface = 0xFF0A0000, fill = 0xFF1A0705,
    muted = 0xFF8A3329, outline = 0xFF5A1D18, outlineVariant = 0xFF2A0D0A, accents = listOf(Color(0xFFE0604F)),
)

// Paper: black on white with no grey, for e-ink and bright sun.
private val PaperInk = palette(
    dark = false, background = 0xFFFFFFFF, text = 0xFF000000, surface = 0xFFFFFFFF, fill = 0xFFFFFFFF,
    muted = 0xFF000000, outline = 0xFF000000, outlineVariant = 0xFF000000, accents = listOf(Color(0xFF000000)),
)

// Outrun: indigo sky running down into a magenta horizon. The outline is the cyan grid.
private val OutrunNight = palette(
    dark = true, background = 0xFF0D0221, text = 0xFFF6E9FF, surface = 0xFF1A0838, fill = 0x26F6E9FF,
    muted = 0xFFC3A6E6, outline = 0xFF2DE2E6, outlineVariant = 0xFF3A1D66, accents = listOf(Color(0xFFFF3EA5)),
    ground = Brush.verticalGradient(
        0f to Color(0xFF0D0221),
        0.55f to Color(0xFF241047),
        0.8f to Color(0xFF5B1150),
        1f to Color(0xFF8C1E4F),
    ),
)

// Phosphor: the tube is never quite black.
private val PhosphorTube = palette(
    dark = true, background = 0xFF030A05, text = 0xFF8CFFA0, surface = 0xFF07140A, fill = 0xFF0C2212,
    muted = 0xFF4FBF68, outline = 0xFF3FA857, outlineVariant = 0xFF12301A, accents = listOf(Color(0xFFD4FFDC)),
)

// Poster: safety yellow by day, yellow on black by night, red for the shouting.
private val PosterYellow = palette(
    dark = false, background = 0xFFFFE600, text = 0xFF000000, surface = 0xFFFFEE4D, fill = 0xFFF2D900,
    muted = 0xFF2B2600, outline = 0xFF000000, outlineVariant = 0xFF000000, accents = listOf(Color(0xFFD7001F)),
    // Inverted: white glyphs print black on the yellow.
    iconFilter = duotone(Color(0xFFFFE600), Color(0xFF000000)),
)
private val PosterBlack = palette(
    dark = true, background = 0xFF000000, text = 0xFFFFE600, surface = 0xFF0D0D0D, fill = 0xFF1A1A1A,
    muted = 0xFFD9C400, outline = 0xFFFFE600, outlineVariant = 0xFFFFE600, accents = listOf(Color(0xFFFF4D5E)),
)

// Blueprint: cyanotype by night, the drafting table's white sheet by day. outlineVariant draws the grid.
private val BlueprintSheet = palette(
    dark = true, background = 0xFF0E3A7A, text = 0xFFEAF2FF, surface = 0xFF124489, fill = 0xFF184E98,
    muted = 0xFFA9C2E8, outline = 0xFFB9CFF0, outlineVariant = 0xFF3D68AD, accents = listOf(Color(0xFFFFE08A)),
    ground = Brush.radialGradient(listOf(Color(0xFF15498F), Color(0xFF0E3A7A), Color(0xFF0A2C5E)), radius = 2400f),
)
private val BlueprintVellum = palette(
    dark = false, background = 0xFFF4F7FB, text = 0xFF0B2E6B, surface = 0xFFFFFFFF, fill = 0xFFE3EBF6,
    muted = 0xFF3F5D8F, outline = 0xFF0B2E6B, outlineVariant = 0xFFC3D3EC, accents = listOf(Color(0xFFC2361F)),
    // Inverted: white glyphs are drawn in the drafting ink.
    iconFilter = duotone(Color(0xFFF4F7FB), Color(0xFF0B2E6B)),
)

// Riso: newsprint, blue ink for words, fluorescent pink for everything loud.
private val RisoPrint = palette(
    dark = false, background = 0xFFF5EFE3, text = 0xFF3255A4, surface = 0xFFFAF6EE, fill = 0xFFEDE4D3,
    muted = 0xFF5B6BA8, outline = 0xFFFF48B0, outlineVariant = 0xFFFF48B0, accents = listOf(Color(0xFFFF48B0)),
)

/** Dark and light palettes; null when the theme has only the other one. */
private fun HomeTheme.palettes(): Pair<Palette?, Palette?> = when (this) {
    HomeTheme.ORIGINAL -> OriginalDark to OriginalLight
    HomeTheme.INDEX -> Slate to Paper
    HomeTheme.RUBRIC -> RubricInk to RubricPage
    HomeTheme.PIXEL -> PixelScreen to PixelLcd
    HomeTheme.TIDE -> TideDeep to TideShallow
    HomeTheme.NIGHT_WATCH -> NightWatch to null
    HomeTheme.PAPER -> null to PaperInk
    HomeTheme.OUTRUN -> OutrunNight to null
    HomeTheme.PHOSPHOR -> PhosphorTube to null
    HomeTheme.POSTER -> PosterBlack to PosterYellow
    HomeTheme.BLUEPRINT -> BlueprintSheet to BlueprintVellum
    HomeTheme.RISO -> null to RisoPrint
}

/** The accent swatches to offer in Settings, or null when the theme fixes its accent. */
fun HomeTheme.accentChoices(): List<Color>? =
    palettes().let { (dark, light) -> (dark ?: light)!!.accents }.takeIf { it.size > 1 }

/** "Dark only" / "Light only" for themes with a single palette, else null. */
fun HomeTheme.fixedMode(): String? {
    val (dark, light) = palettes()
    return when {
        light == null -> "Dark only"
        dark == null -> "Light only"
        else -> null
    }
}

@Composable
fun CascadeTheme(
    mode: ThemeMode,
    accentIndex: Int,
    homeTheme: HomeTheme,
    content: @Composable () -> Unit,
) {
    val (darkPalette, lightPalette) = homeTheme.palettes()
    val dark = when {
        lightPalette == null -> true
        darkPalette == null -> false
        else -> when (mode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }
    val palette = (if (dark) darkPalette else lightPalette)!!
    val accent = palette.accents.getOrElse(accentIndex) { palette.accents.first() }
    val onAccent = if (dark) palette.scheme.background else palette.scheme.surface
    val scheme = palette.scheme.copy(
        primary = accent,
        secondary = accent,
        onPrimary = onAccent,
        onSecondary = onAccent,
    )
    val base = homeTheme.style()
    val style = base.copy(ground = palette.ground, iconFilter = palette.iconFilter ?: base.iconFilter)
    // Edge-to-edge: system bar icons must contrast with our background even
    // when the app theme is forced opposite to the system theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalHomeStyle provides style) {
            Surface(color = Color.Transparent, contentColor = scheme.onBackground, content = content)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
