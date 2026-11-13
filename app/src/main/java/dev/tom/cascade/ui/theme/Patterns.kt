package dev.tom.cascade.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.floor
import kotlin.math.max

/** One loop of the pattern's motion in milliseconds, or null for a still pattern. */
val GroundPattern.periodMs: Int?
    get() = when (this) {
        GroundPattern.Horizon -> 2400 // the grid rolls one sleeper toward you
        GroundPattern.Scanlines -> 7000 // the bright band rolls down the tube
        GroundPattern.Halftone -> 9000 // the dots drift one pitch sideways
        GroundPattern.None, GroundPattern.Grid -> null
    }

/** Redraws at most this often; a background never needs the display's full rate. */
private const val FRAME_NS = 33_000_000L

/**
 * The pattern's position in its loop, 0 until 1. It only advances while [run] is true, the
 * home screen is resumed and the system's animator scale is above zero ("Remove animations"
 * sets it to zero). Read it inside a draw lambda so a tick redraws without recomposing.
 */
@Composable
fun rememberGroundPhase(pattern: GroundPattern, run: Boolean): FloatState {
    val phase = remember { mutableFloatStateOf(0f) }
    val period = pattern.periodMs
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val resumed = state.isAtLeast(Lifecycle.State.RESUMED)
    val resolver = LocalContext.current.contentResolver
    // Re-read on every resume, so changing the system setting takes effect on return.
    val motionAllowed = remember(resumed) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    val active = period != null && run && resumed && motionAllowed
    LaunchedEffect(active, period) {
        if (!active || period == null) return@LaunchedEffect
        // Carry on from where it stopped rather than jumping back to the start.
        val from = phase.floatValue
        var start = -1L
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (start < 0) start = now
                if (now - last >= FRAME_NS) {
                    last = now
                    val loops = from + (now - start) / 1_000_000.0 / period
                    phase.floatValue = (loops - floor(loops)).toFloat()
                }
            }
        }
    }
    return phase
}

/**
 * Paints the theme's [GroundPattern]. Geometry is built once per size in [drawWithCache];
 * [phase] is read only while drawing, so motion redraws this layer and nothing else.
 */
@Composable
fun Modifier.groundPattern(pattern: GroundPattern, phase: () -> Float = { 0f }): Modifier {
    if (pattern == GroundPattern.None) return this
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onBackground
    val rule = scheme.outlineVariant
    val line = scheme.outline
    val accent = scheme.primary
    return this.then(
        Modifier.drawWithCache {
            when (pattern) {
                GroundPattern.None -> onDrawBehind {}
                GroundPattern.Scanlines -> scanlines(ink, phase)
                GroundPattern.Grid -> grid(rule)
                GroundPattern.Halftone -> halftone(accent, phase)
                GroundPattern.Horizon -> horizon(line, accent, phase)
            }
        },
    )
}

private fun CacheDrawScope.scanlines(ink: Color, phase: () -> Float): DrawResult {
    val pitch = 3.dp.toPx()
    val band = 1.dp.toPx()
    val rollHeight = size.height * 0.22f
    return onDrawBehind {
        val p = phase()
        // A faint, irregular breathing of the tube: about ±8% of an already faint line.
        val alpha = 0.045f * (0.92f + 0.16f * smoothNoise(p * 50f))
        val color = ink.copy(alpha = alpha)
        var y = 0f
        while (y < size.height) {
            drawRect(color, Offset(0f, y), Size(size.width, band))
            y += pitch
        }
        val top = -rollHeight + p * (size.height + rollHeight)
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, ink.copy(alpha = 0.05f), Color.Transparent),
                startY = top,
                endY = top + rollHeight,
            ),
            Offset(0f, top),
            Size(size.width, rollHeight),
        )
    }
}

private fun CacheDrawScope.grid(rule: Color): DrawResult {
    val cell = 12.dp.toPx()
    val minor = rule.copy(alpha = rule.alpha * 0.45f)
    val thick = 1.dp.toPx()
    return onDrawBehind {
        var i = 0
        var x = 0f
        while (x < size.width) {
            val major = i % 5 == 0
            drawLine(if (major) rule else minor, Offset(x, 0f), Offset(x, size.height), if (major) thick else 1f)
            x += cell; i++
        }
        i = 0
        var y = 0f
        while (y < size.height) {
            val major = i % 5 == 0
            drawLine(if (major) rule else minor, Offset(0f, y), Offset(size.width, y), if (major) thick else 1f)
            y += cell; i++
        }
    }
}

private fun CacheDrawScope.halftone(accent: Color, phase: () -> Float): DrawResult {
    // Dots swell from nothing at mid-screen to full size at the bottom edge.
    val pitch = 9.dp.toPx()
    val maxDot = 6.dp.toPx()
    // Read the canvas size here: inside buildList, `size` is the list's.
    val w = size.width
    val h = size.height
    val start = h * 0.4f
    val color = accent.copy(alpha = 0.16f)
    val rows = buildList {
        var y = start
        var row = 0
        while (y < h + pitch) {
            val t = ((y - start) / (h - start)).coerceIn(0f, 1f)
            val dot = maxDot * t
            if (dot >= 1f) {
                val shift = if (row % 2 == 0) 0f else pitch / 2
                // One pitch spare on the left so the drift never shows an empty edge.
                val points = buildList {
                    var x = shift - pitch
                    while (x < w + pitch) { add(Offset(x, y)); x += pitch }
                }
                add(dot to points)
            }
            y += pitch; row++
        }
    }
    return onDrawBehind {
        translate(left = phase() * pitch) {
            rows.forEach { (dot, points) ->
                drawPoints(points, PointMode.Points, color, strokeWidth = dot, cap = StrokeCap.Round)
            }
        }
    }
}

private fun CacheDrawScope.horizon(line: Color, accent: Color, phase: () -> Float): DrawResult {
    val horizon = size.height * 0.72f
    val vanish = Offset(size.width / 2, horizon)
    val grid = line.copy(alpha = 0.42f)
    val stroke = 1.dp.toPx()
    val sunRadius = size.width * 0.3f
    val sun = Brush.verticalGradient(listOf(Color(0xFFFFD36E), accent), startY = horizon - sunRadius, endY = horizon)
    val depth = size.height - horizon
    return onDrawBehind {
        drawSun(vanish, sunRadius, horizon, sun)
        drawLine(line.copy(alpha = 0.7f), Offset(0f, horizon), Offset(size.width, horizon), stroke * 1.5f)
        // Rails run from below the screen's edges up to the vanishing point.
        val rails = 14
        for (k in 0..rails) {
            val x = -size.width + 3 * size.width * k / rails
            drawLine(grid, vanish, Offset(x, size.height), stroke)
        }
        // Sleepers bunch up toward the horizon and roll toward the viewer as the phase turns.
        val sleepers = 9
        val p = phase()
        for (k in 0..sleepers) {
            val t = (k + p) / sleepers
            if (t <= 0f || t > 1f) continue
            val y = horizon + depth * t * t
            // Fade each one in as it leaves the horizon, so new sleepers don't pop.
            val fade = (t * 4f).coerceAtMost(1f)
            drawLine(grid.copy(alpha = grid.alpha * fade), Offset(0f, y), Offset(size.width, y), stroke)
        }
    }
}

/** A striped half-sun sitting on the horizon; the stripes thicken toward the bottom. */
private fun DrawScope.drawSun(centre: Offset, radius: Float, horizon: Float, brush: Brush) {
    val top = horizon - radius
    val bands = 7
    var y = top
    for (i in 0 until bands) {
        val solid = radius * (0.26f - i * 0.028f)
        val gap = max(2f, radius * 0.012f * (i + 1))
        clipRect(top = y, bottom = minOf(y + solid, horizon)) {
            drawCircle(brush, radius, centre, alpha = 0.34f)
        }
        y += solid + gap
        if (y >= horizon) break
    }
}

/** Smooth value noise in 0..1, looping every 50 units so the scanline phase wraps cleanly. */
private fun smoothNoise(x: Float): Float {
    val i = floor(x).toInt()
    val f = x - i
    val a = hash(Math.floorMod(i, 50))
    val b = hash(Math.floorMod(i + 1, 50))
    val s = f * f * (3 - 2 * f)
    return a + (b - a) * s
}

private fun hash(n: Int): Float {
    var h = n * 374761393
    h = (h xor (h ushr 13)) * 1274126177
    return ((h xor (h ushr 16)) and 0xFFFF) / 65535f
}
