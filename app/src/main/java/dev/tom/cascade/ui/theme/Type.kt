package dev.tom.cascade.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.tom.cascade.R
import dev.tom.cascade.data.HomeTheme

// Condensed display face: the Index clock and initials.
val Shoulders = FontFamily(
    Font(R.font.shoulders_light, FontWeight.Light),
    Font(R.font.shoulders_semibold, FontWeight.SemiBold),
)

// Text serif: Index app names, the date line and the verse.
val Newsreader = FontFamily(
    Font(R.font.newsreader_regular, FontWeight.Normal),
    Font(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic),
)

private val Cormorant = FontFamily(
    Font(R.font.cormorant_medium, FontWeight.Medium),
    Font(R.font.cormorant_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.cormorant_bold, FontWeight.Bold),
)

private val Pixelify = FontFamily(
    Font(R.font.pixelify_regular, FontWeight.Normal),
    Font(R.font.pixelify_semibold, FontWeight.SemiBold),
)

private val Outfit = FontFamily(
    Font(R.font.outfit_extralight, FontWeight.ExtraLight),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
)

private val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_light, FontWeight.Light),
    Font(R.font.barlow_condensed_medium, FontWeight.Medium),
)

private val Audiowide = FontFamily(Font(R.font.audiowide_regular, FontWeight.Normal))

private val Vt323 = FontFamily(Font(R.font.vt323_regular, FontWeight.Normal))

private val ArchivoBlack = FontFamily(Font(R.font.archivo_black, FontWeight.Black))

private val SpaceMono = FontFamily(
    Font(R.font.spacemono_regular, FontWeight.Normal),
    Font(R.font.spacemono_bold, FontWeight.Bold),
)

private val RubikMonoOne = FontFamily(Font(R.font.rubik_mono_one, FontWeight.Normal))

private val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_bold, FontWeight.Bold),
)

enum class SearchShape { Pill, Underline, Box }

/** How a section's letter is set: 1.0 label, large initial, `> A` prompt, or ruled. */
enum class SectionMark { Label, Initial, Prompt, Ruled }

/** Drawn over the home ground, under everything else. */
enum class GroundPattern { None, Scanlines, Grid, Halftone, Horizon }

/** Everything about the home screen's type and furniture that a theme changes. */
@Immutable
data class HomeStyle(
    val clock: TextStyle,
    val clockCompact: TextStyle,
    val clockInWords: Boolean = false,
    val clockInAccent: Boolean = false,
    /** Pulls thin numerals onto the text column's left edge. */
    val clockNudge: Dp = 0.dp,
    val date: TextStyle,
    val verse: TextStyle,
    val verseReference: TextStyle,
    /** Epigraph: italic text with the reference hung beneath on the right. 1.0 puts it above. */
    val verseAsEpigraph: Boolean = true,
    val referenceInAccent: Boolean = false,
    val appLabel: TextStyle,
    val labelsUpper: Boolean = false,
    val rowPadding: Dp = 9.dp,
    val iconGap: Dp = 18.dp,
    val initial: TextStyle,
    val section: SectionMark = SectionMark.Initial,
    val favoritesAsStar: Boolean = true,
    val search: TextStyle,
    val searchShape: SearchShape = SearchShape.Underline,
    val rail: TextStyle,
    val railMaxSize: TextUnit = 12.sp,
    /** The 1.0 rail: a dark strip, muted letters, bold active letter. */
    val railStrip: Boolean = false,
    /** How much the background covers the wallpaper; 1 hides it. */
    val scrim: Float = 0.7f,
    /** Paints the home ground instead of the flat background colour. */
    val ground: Brush? = null,
    val iconFilter: ColorFilter? = null,
    val pattern: GroundPattern = GroundPattern.None,
) {
    fun label(text: String): String = if (labelsUpper) text.uppercase() else text
}

private val m3 = Typography()

private val OriginalStyle = HomeStyle(
    clock = m3.displayLarge,
    clockCompact = m3.titleLarge,
    date = m3.bodyMedium,
    verse = m3.bodySmall,
    verseReference = m3.labelSmall,
    verseAsEpigraph = false,
    referenceInAccent = true,
    appLabel = m3.bodyLarge,
    rowPadding = 8.dp,
    iconGap = 16.dp,
    initial = m3.titleSmall,
    section = SectionMark.Label,
    favoritesAsStar = false,
    search = m3.bodyLarge,
    searchShape = SearchShape.Pill,
    rail = TextStyle(fontWeight = FontWeight.Medium),
    railMaxSize = 10.sp,
    railStrip = true,
    scrim = 0.62f,
)

private val IndexClock = TextStyle(
    fontFamily = Shoulders,
    fontWeight = FontWeight.Light,
    fontSize = 128.sp,
    lineHeight = 112.sp,
    letterSpacing = (-0.01).em,
    fontFeatureSettings = "tnum",
)

private val IndexStyle = HomeStyle(
    clock = IndexClock,
    clockCompact = IndexClock.copy(fontSize = 56.sp, lineHeight = 52.sp),
    clockNudge = (-4).dp,
    date = TextStyle(fontFamily = Newsreader, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 24.sp),
    verse = TextStyle(fontFamily = Newsreader, fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp),
    verseReference = TextStyle(
        fontFamily = Newsreader,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.04.em,
        fontFeatureSettings = "onum",
    ),
    appLabel = TextStyle(fontFamily = Newsreader, fontSize = 19.sp, lineHeight = 24.sp),
    initial = TextStyle(fontFamily = Shoulders, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 28.sp),
    search = TextStyle(fontFamily = Newsreader, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 22.sp),
    rail = TextStyle(fontFamily = Shoulders, fontWeight = FontWeight.SemiBold, letterSpacing = 0.02.em),
)

private val RubricClock = TextStyle(
    fontFamily = Cormorant,
    fontWeight = FontWeight.Medium,
    fontSize = 104.sp,
    lineHeight = 104.sp,
    fontFeatureSettings = "lnum",
)

private val RubricStyle = HomeStyle(
    clock = RubricClock,
    clockCompact = RubricClock.copy(fontSize = 48.sp, lineHeight = 50.sp),
    clockNudge = (-3).dp,
    date = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 26.sp),
    verse = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 24.sp),
    verseReference = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 20.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 26.sp),
    initial = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 36.sp),
    search = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 24.sp),
    rail = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Bold),
    railMaxSize = 13.sp,
    scrim = 1f,
)

private val PixelClock = TextStyle(
    fontFamily = Pixelify,
    fontWeight = FontWeight.SemiBold,
    fontSize = 96.sp,
    lineHeight = 96.sp,
)

private val PixelStyle = HomeStyle(
    clock = PixelClock,
    clockCompact = PixelClock.copy(fontSize = 44.sp, lineHeight = 46.sp),
    date = TextStyle(fontFamily = Pixelify, fontSize = 17.sp, lineHeight = 22.sp),
    verse = TextStyle(fontFamily = Pixelify, fontSize = 14.sp, lineHeight = 20.sp),
    verseReference = TextStyle(fontFamily = Pixelify, fontSize = 13.sp, lineHeight = 18.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = Pixelify, fontSize = 18.sp, lineHeight = 24.sp),
    initial = TextStyle(fontFamily = Pixelify, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp),
    section = SectionMark.Prompt,
    search = TextStyle(fontFamily = Pixelify, fontSize = 16.sp, lineHeight = 22.sp),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = Pixelify),
    railMaxSize = 11.sp,
    scrim = 1f,
)

private val TideStyle = HomeStyle(
    clock = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.ExtraLight, fontSize = 54.sp, lineHeight = 58.sp, letterSpacing = (-0.01).em),
    clockCompact = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.ExtraLight, fontSize = 30.sp, lineHeight = 34.sp),
    clockInWords = true,
    date = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.sp),
    verse = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 23.sp),
    verseReference = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 13.sp, lineHeight = 18.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 20.sp, lineHeight = 26.sp),
    initial = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    search = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.sp),
    searchShape = SearchShape.Pill,
    rail = TextStyle(fontFamily = Outfit),
    railMaxSize = 10.sp,
    scrim = 0.55f,
)

private val NightClock = TextStyle(
    fontFamily = BarlowCondensed,
    fontWeight = FontWeight.Light,
    fontSize = 116.sp,
    lineHeight = 108.sp,
    fontFeatureSettings = "tnum",
)

// Every pixel of an icon becomes a dim red of the same brightness.
private val NightIcons = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            0.24f, 0.47f, 0.09f, 0f, 0f,
            0.05f, 0.10f, 0.02f, 0f, 0f,
            0.04f, 0.08f, 0.02f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ),
    ),
)

private val NightStyle = HomeStyle(
    clock = NightClock,
    clockCompact = NightClock.copy(fontSize = 54.sp, lineHeight = 52.sp),
    clockNudge = (-3).dp,
    date = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Light, fontSize = 20.sp, lineHeight = 24.sp),
    verse = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 22.sp),
    verseReference = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 20.sp),
    appLabel = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Light, fontSize = 21.sp, lineHeight = 26.sp),
    initial = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 24.sp),
    search = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Light, fontSize = 19.sp, lineHeight = 24.sp),
    rail = TextStyle(fontFamily = BarlowCondensed, fontWeight = FontWeight.Medium),
    scrim = 1f,
    iconFilter = NightIcons,
)

private val PaperClock = TextStyle(
    fontFamily = Atkinson,
    fontWeight = FontWeight.Bold,
    fontSize = 100.sp,
    lineHeight = 100.sp,
    letterSpacing = (-0.03).em,
    fontFeatureSettings = "tnum",
)

private val PaperStyle = HomeStyle(
    clock = PaperClock,
    clockCompact = PaperClock.copy(fontSize = 46.sp, lineHeight = 48.sp),
    clockNudge = (-3).dp,
    date = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp),
    verse = TextStyle(fontFamily = Atkinson, fontSize = 15.sp, lineHeight = 22.sp),
    verseReference = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
    appLabel = TextStyle(fontFamily = Atkinson, fontSize = 18.sp, lineHeight = 24.sp),
    initial = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp),
    section = SectionMark.Ruled,
    search = TextStyle(fontFamily = Atkinson, fontSize = 16.sp, lineHeight = 22.sp),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold),
    railMaxSize = 11.sp,
    scrim = 1f,
    iconFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
)

/**
 * Maps an icon's brightness onto a two-ink ramp: black becomes [dark], white becomes [light].
 * Compose's matrix offsets are on the 0..255 scale.
 */
internal fun duotone(dark: Color, light: Color): ColorFilter {
    fun row(d: Float, l: Float): FloatArray {
        val span = l - d
        return floatArrayOf(0.299f * span, 0.587f * span, 0.114f * span, 0f, d * 255f)
    }
    return ColorFilter.colorMatrix(
        ColorMatrix(
            row(dark.red, light.red) +
                row(dark.green, light.green) +
                row(dark.blue, light.blue) +
                floatArrayOf(0f, 0f, 0f, 1f, 0f),
        ),
    )
}

// Outrun: chrome-era sunset. The clock and initials burn hot pink.
private val NeonPink = Color(0xFFFF3EA5)
private val PinkGlow = Shadow(NeonPink.copy(alpha = 0.85f), Offset.Zero, blurRadius = 28f)

private val OutrunClock = TextStyle(
    fontFamily = Audiowide,
    fontSize = 88.sp,
    lineHeight = 96.sp,
    letterSpacing = 0.02.em,
    fontFeatureSettings = "tnum",
    shadow = PinkGlow,
)

private val OutrunStyle = HomeStyle(
    clock = OutrunClock,
    clockCompact = OutrunClock.copy(fontSize = 42.sp, lineHeight = 48.sp, shadow = PinkGlow.copy(blurRadius = 16f)),
    clockInAccent = true,
    date = TextStyle(fontFamily = Audiowide, fontSize = 14.sp, lineHeight = 22.sp, letterSpacing = 0.04.em),
    verse = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 15.sp, lineHeight = 23.sp),
    verseReference = TextStyle(fontFamily = Audiowide, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.1.em),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Light, fontSize = 20.sp, lineHeight = 26.sp),
    initial = TextStyle(fontFamily = Audiowide, fontSize = 24.sp, lineHeight = 28.sp, shadow = PinkGlow.copy(blurRadius = 18f)),
    search = TextStyle(fontFamily = Audiowide, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.08.em),
    searchShape = SearchShape.Underline,
    rail = TextStyle(fontFamily = Audiowide),
    railMaxSize = 10.sp,
    scrim = 0.88f,
    pattern = GroundPattern.Horizon,
)

// Phosphor: P1 green on a dead-black tube. Everything glows a little.
private val PhosphorGreen = Color(0xFF8CFFA0)
private val GreenGlow = Shadow(Color(0xFF33FF66).copy(alpha = 0.7f), Offset.Zero, blurRadius = 14f)

private val PhosphorClock = TextStyle(
    fontFamily = Vt323,
    fontSize = 132.sp,
    lineHeight = 116.sp,
    shadow = GreenGlow.copy(blurRadius = 32f),
)

private val PhosphorStyle = HomeStyle(
    clock = PhosphorClock,
    clockCompact = PhosphorClock.copy(fontSize = 60.sp, lineHeight = 56.sp, shadow = GreenGlow),
    clockNudge = (-2).dp,
    date = TextStyle(fontFamily = Vt323, fontSize = 22.sp, lineHeight = 24.sp, shadow = GreenGlow),
    verse = TextStyle(fontFamily = Vt323, fontSize = 20.sp, lineHeight = 22.sp, shadow = GreenGlow),
    verseReference = TextStyle(fontFamily = Vt323, fontSize = 18.sp, lineHeight = 20.sp, shadow = GreenGlow),
    appLabel = TextStyle(fontFamily = Vt323, fontSize = 25.sp, lineHeight = 28.sp, shadow = GreenGlow),
    rowPadding = 8.dp,
    initial = TextStyle(fontFamily = Vt323, fontSize = 24.sp, lineHeight = 26.sp, shadow = GreenGlow),
    section = SectionMark.Prompt,
    search = TextStyle(fontFamily = Vt323, fontSize = 22.sp, lineHeight = 24.sp, shadow = GreenGlow),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = Vt323),
    railMaxSize = 14.sp,
    scrim = 1f,
    iconFilter = duotone(Color(0xFF020803), PhosphorGreen),
    pattern = GroundPattern.Scanlines,
)

// Poster: brutalist street bill. Heavy capitals, hard rules, nothing soft.
private val PosterClock = TextStyle(
    fontFamily = ArchivoBlack,
    fontWeight = FontWeight.Black,
    fontSize = 96.sp,
    lineHeight = 92.sp,
    letterSpacing = (-0.05).em,
    fontFeatureSettings = "tnum",
)

private val PosterStyle = HomeStyle(
    clock = PosterClock,
    clockCompact = PosterClock.copy(fontSize = 46.sp, lineHeight = 46.sp),
    clockNudge = (-5).dp,
    date = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black, fontSize = 14.sp, lineHeight = 20.sp),
    verse = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 21.sp),
    verseReference = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black, fontSize = 13.sp, lineHeight = 18.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black, fontSize = 18.sp, lineHeight = 22.sp, letterSpacing = (-0.01).em),
    labelsUpper = true,
    rowPadding = 10.dp,
    iconGap = 16.dp,
    initial = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 40.sp, letterSpacing = (-0.04).em),
    section = SectionMark.Ruled,
    search = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 20.sp),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = ArchivoBlack, fontWeight = FontWeight.Black),
    railMaxSize = 12.sp,
    scrim = 1f,
)

// Blueprint: a drafting sheet. Monospace annotations on a ruled grid.
private val BlueprintClock = TextStyle(
    fontFamily = SpaceMono,
    fontWeight = FontWeight.Bold,
    fontSize = 80.sp,
    lineHeight = 88.sp,
    letterSpacing = (-0.04).em,
)

private val BlueprintStyle = HomeStyle(
    clock = BlueprintClock,
    clockCompact = BlueprintClock.copy(fontSize = 38.sp, lineHeight = 44.sp),
    clockNudge = (-3).dp,
    date = TextStyle(fontFamily = SpaceMono, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.06.em),
    verse = TextStyle(fontFamily = SpaceMono, fontSize = 13.sp, lineHeight = 20.sp),
    verseReference = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 18.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = SpaceMono, fontSize = 16.sp, lineHeight = 22.sp),
    rowPadding = 10.dp,
    initial = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp),
    section = SectionMark.Ruled,
    search = TextStyle(fontFamily = SpaceMono, fontSize = 14.sp, lineHeight = 20.sp),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold),
    railMaxSize = 10.sp,
    scrim = 0.94f,
    pattern = GroundPattern.Grid,
)

// Riso: fluorescent pink printed over medium blue, a few points out of register.
private val RisoBlue = Color(0xFF3255A4)
private val Misregister = Shadow(RisoBlue.copy(alpha = 0.9f), Offset(7f, 5f), blurRadius = 0f)

private val RisoClock = TextStyle(
    fontFamily = RubikMonoOne,
    fontSize = 78.sp,
    lineHeight = 88.sp,
    letterSpacing = (-0.04).em,
    shadow = Misregister,
)

private val RisoStyle = HomeStyle(
    clock = RisoClock,
    clockCompact = RisoClock.copy(fontSize = 36.sp, lineHeight = 42.sp, shadow = Misregister.copy(offset = Offset(4f, 3f))),
    clockInAccent = true,
    clockNudge = (-2).dp,
    date = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    verse = TextStyle(fontFamily = SpaceMono, fontSize = 13.sp, lineHeight = 20.sp),
    verseReference = TextStyle(fontFamily = RubikMonoOne, fontSize = 11.sp, lineHeight = 18.sp),
    referenceInAccent = true,
    appLabel = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 26.sp),
    initial = TextStyle(fontFamily = RubikMonoOne, fontSize = 30.sp, lineHeight = 34.sp, shadow = Misregister.copy(offset = Offset(5f, 4f))),
    search = TextStyle(fontFamily = SpaceMono, fontSize = 14.sp, lineHeight = 20.sp),
    searchShape = SearchShape.Box,
    rail = TextStyle(fontFamily = RubikMonoOne),
    railMaxSize = 9.sp,
    scrim = 1f,
    // Shadows print pink, light prints blue, so white-glyph icon packs stay visible on paper.
    iconFilter = duotone(Color(0xFFFF48B0), RisoBlue),
    pattern = GroundPattern.Halftone,
)

fun HomeTheme.style(): HomeStyle = when (this) {
    HomeTheme.ORIGINAL -> OriginalStyle
    HomeTheme.INDEX -> IndexStyle
    HomeTheme.RUBRIC -> RubricStyle
    HomeTheme.PIXEL -> PixelStyle
    HomeTheme.TIDE -> TideStyle
    HomeTheme.NIGHT_WATCH -> NightStyle
    HomeTheme.PAPER -> PaperStyle
    HomeTheme.OUTRUN -> OutrunStyle
    HomeTheme.PHOSPHOR -> PhosphorStyle
    HomeTheme.POSTER -> PosterStyle
    HomeTheme.BLUEPRINT -> BlueprintStyle
    HomeTheme.RISO -> RisoStyle
}

val LocalHomeStyle = staticCompositionLocalOf { IndexStyle }
