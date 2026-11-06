package dev.tom.cascade.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.tom.cascade.data.WeatherGlyph
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherGlyphIcon(glyph: WeatherGlyph, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier.size(16.dp)) {
        when (glyph) {
            WeatherGlyph.Sun -> drawSun(color, Offset(size.width / 2, size.height / 2), size.minDimension * 0.22f)
            WeatherGlyph.PartlyCloudy -> {
                drawSun(color, Offset(size.width * 0.68f, size.height * 0.3f), size.minDimension * 0.16f)
                drawCloud(color, y = size.height * 0.62f)
            }
            WeatherGlyph.Cloud -> drawCloud(color, y = size.height * 0.55f)
            WeatherGlyph.Rain -> {
                drawCloud(color, y = size.height * 0.45f)
                repeat(3) { i ->
                    val x = size.width * (0.28f + i * 0.22f)
                    drawLine(color, Offset(x, size.height * 0.72f), Offset(x - size.width * 0.06f, size.height * 0.92f), strokeWidth = size.minDimension * 0.06f)
                }
            }
            WeatherGlyph.Snow -> {
                drawCloud(color, y = size.height * 0.45f)
                repeat(3) { i ->
                    drawCircle(color, radius = size.minDimension * 0.045f, center = Offset(size.width * (0.28f + i * 0.22f), size.height * 0.82f))
                }
            }
            WeatherGlyph.Storm -> {
                drawCloud(color, y = size.height * 0.45f)
                val bolt = Path().apply {
                    moveTo(size.width * 0.52f, size.height * 0.62f)
                    lineTo(size.width * 0.4f, size.height * 0.84f)
                    lineTo(size.width * 0.5f, size.height * 0.84f)
                    lineTo(size.width * 0.42f, size.height)
                }
                drawPath(bolt, color, style = Stroke(width = size.minDimension * 0.08f))
            }
            WeatherGlyph.Fog -> repeat(3) { i ->
                val y = size.height * (0.3f + i * 0.2f)
                drawLine(color, Offset(size.width * 0.12f, y), Offset(size.width * 0.88f, y), strokeWidth = size.minDimension * 0.09f)
            }
        }
    }
}

private fun DrawScope.drawSun(color: androidx.compose.ui.graphics.Color, center: Offset, radius: Float) {
    drawCircle(color, radius = radius, center = center)
    repeat(8) { i ->
        val angle = Math.PI * i / 4
        val start = Offset(center.x + cos(angle).toFloat() * radius * 1.35f, center.y + sin(angle).toFloat() * radius * 1.35f)
        val end = Offset(center.x + cos(angle).toFloat() * radius * 1.9f, center.y + sin(angle).toFloat() * radius * 1.9f)
        drawLine(color, start, end, strokeWidth = radius * 0.28f)
    }
}

private fun DrawScope.drawCloud(color: androidx.compose.ui.graphics.Color, y: Float) {
    val unit = size.minDimension
    val left = Offset(size.width * 0.3f, y)
    val middle = Offset(size.width * 0.5f, y - unit * 0.1f)
    val right = Offset(size.width * 0.68f, y)
    drawCircle(color, radius = unit * 0.15f, center = left)
    drawCircle(color, radius = unit * 0.2f, center = middle)
    drawCircle(color, radius = unit * 0.15f, center = right)
    drawRoundRect(
        color,
        topLeft = Offset(size.width * 0.22f, y),
        size = Size(size.width * 0.56f, unit * 0.16f),
        cornerRadius = CornerRadius(unit * 0.08f),
    )
}
