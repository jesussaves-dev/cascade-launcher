package dev.tom.cascade.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Paints the theme's [GroundPattern] over the home ground. Geometry is built once per size
 * in [drawWithCache], so scrolling the list never reallocates it.
 */
@Composable
fun Modifier.groundPattern(pattern: GroundPattern): Modifier {
    if (pattern == GroundPattern.None) return this
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onBackground
    val rule = scheme.outlineVariant
    val line = scheme.outline
    val accent = scheme.primary
    return drawWithCache {
        when (pattern) {
            GroundPattern.None -> onDrawBehind {}
            GroundPattern.Scanlines -> {
                val pitch = 3.dp.toPx()
                val band = 1.dp.toPx()
                val color = ink.copy(alpha = 0.045f)
                onDrawBehind {
                    var y = 0f
                    while (y < size.height) {
                        drawRect(color, Offset(0f, y), Size(size.width, band))
                        y += pitch
                    }
                }
            }
            GroundPattern.Grid -> {
                val cell = 12.dp.toPx()
                val minor = rule.copy(alpha = rule.alpha * 0.45f)
                val thin = 1f
                val thick = 1.dp.toPx()
                onDrawBehind {
                    var i = 0
                    var x = 0f
                    while (x < size.width) {
                        val major = i % 5 == 0
                        drawLine(if (major) rule else minor, Offset(x, 0f), Offset(x, size.height), if (major) thick else thin)
                        x += cell; i++
                    }
                    i = 0
                    var y = 0f
                    while (y < size.height) {
                        val major = i % 5 == 0
                        drawLine(if (major) rule else minor, Offset(0f, y), Offset(size.width, y), if (major) thick else thin)
                        y += cell; i++
                    }
                }
            }
            GroundPattern.Halftone -> {
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
                            val points = buildList {
                                var x = shift
                                while (x < w + pitch) { add(Offset(x, y)); x += pitch }
                            }
                            add(dot to points)
                        }
                        y += pitch; row++
                    }
                }
                onDrawBehind {
                    rows.forEach { (dot, points) ->
                        drawPoints(points, PointMode.Points, color, strokeWidth = dot, cap = StrokeCap.Round)
                    }
                }
            }
            GroundPattern.Horizon -> {
                val horizon = size.height * 0.72f
                val vanish = Offset(size.width / 2, horizon)
                val grid = line.copy(alpha = 0.42f)
                val stroke = 1.dp.toPx()
                val sunRadius = size.width * 0.3f
                val sun = Brush.verticalGradient(
                    listOf(Color(0xFFFFD36E), accent),
                    startY = horizon - sunRadius,
                    endY = horizon,
                )
                onDrawBehind {
                    drawSun(vanish, sunRadius, horizon, sun)
                    drawLine(line.copy(alpha = 0.7f), Offset(0f, horizon), Offset(size.width, horizon), stroke * 1.5f)
                    // Rails run from below the screen's edges up to the vanishing point.
                    val rails = 14
                    for (k in 0..rails) {
                        val x = -size.width + 3 * size.width * k / rails
                        drawLine(grid, vanish, Offset(x, size.height), stroke)
                    }
                    // Sleepers bunch up toward the horizon.
                    val sleepers = 9
                    for (k in 1..sleepers) {
                        val t = k.toFloat() / sleepers
                        val y = horizon + (size.height - horizon) * t * t
                        drawLine(grid, Offset(0f, y), Offset(size.width, y), stroke)
                    }
                }
            }
        }
    }.let { this.then(it) }
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
