package dev.tom.cascade.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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

private val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_bold, FontWeight.Bold),
)

enum class SearchShape { Pill, Underline, Box }

/** How a section's letter is set: 1.0 label, large initial, `> A` prompt, or ruled. */
enum class SectionMark { Label, Initial, Prompt, Ruled }

/** Everything about the home screen's type and furniture that a theme changes. */
@Immutable
data class HomeStyle(
    val clock: TextStyle,
    val clockCompact: TextStyle,
    val clockInWords: Boolean = false,
    /** Pulls thin numerals onto the text column's left edge. */
    val clockNudge: Dp = 0.dp,
    val date: TextStyle,
    val verse: TextStyle,
    val verseReference: TextStyle,
    /** Epigraph: italic text with the reference hung beneath on the right. 1.0 puts it above. */
    val verseAsEpigraph: Boolean = true,
    val referenceInAccent: Boolean = false,
    val appLabel: TextStyle,
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
)

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

fun HomeTheme.style(): HomeStyle = when (this) {
    HomeTheme.ORIGINAL -> OriginalStyle
    HomeTheme.INDEX -> IndexStyle
    HomeTheme.RUBRIC -> RubricStyle
    HomeTheme.PIXEL -> PixelStyle
    HomeTheme.TIDE -> TideStyle
    HomeTheme.NIGHT_WATCH -> NightStyle
    HomeTheme.PAPER -> PaperStyle
}

val LocalHomeStyle = staticCompositionLocalOf { IndexStyle }
