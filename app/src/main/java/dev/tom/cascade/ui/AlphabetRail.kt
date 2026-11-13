package dev.tom.cascade.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import dev.tom.cascade.ui.theme.LocalHomeStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp

@Composable
fun AlphabetRail(
    letters: List<String>,
    presentLetters: Set<String>,
    active: String?,
    onFavorites: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var railHeight by remember { mutableIntStateOf(1) }
    var fingerIndex by remember { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val maxLetterSize = with(density) {
        (railHeight.toFloat() / letters.size).toSp()
    }
    val style = LocalHomeStyle.current
    val letterSize = if (maxLetterSize.value < style.railMaxSize.value) maxLetterSize else style.railMaxSize
    // The 1.0 rail sits on a strip with muted letters and a bold active one.
    val strip = style.railStrip

    Column(
        modifier = modifier
            .width(34.dp)
            .fillMaxHeight()
            .then(
                if (strip) {
                    Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                } else {
                    Modifier
                },
            )
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onFavorites()
                }
                .semantics { contentDescription = "Favorites" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { railHeight = it.height.coerceAtLeast(1) }
                .semantics { contentDescription = "Alphabet" }
                .pointerInput(letters) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    fun pick(y: Float) {
                        val index = ((y / railHeight) * letters.size)
                            .toInt()
                            .coerceIn(0, letters.lastIndex)
                        if (index != fingerIndex) {
                            fingerIndex = index
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onSelect(letters[index])
                        }
                    }

                    pick(down.position.y)
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        pick(change.position.y)
                        change.consume()
                    }
                    fingerIndex = -1
                }
            },
        ) {
        Column(Modifier.fillMaxSize()) {
            letters.forEachIndexed { index, letter ->
                val targetScale = if (fingerIndex >= 0) {
                    1f + 0.8f * exp(-0.5f * (index - fingerIndex) * (index - fingerIndex))
                } else {
                    1f
                }
                val scale by animateFloatAsState(targetScale, label = "rail-scale")
                val present = letter in presentLetters
                val isActive = letter == active
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .alpha(if (present) 1f else 0.3f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = letter,
                        style = style.rail,
                        fontSize = letterSize,
                        lineHeight = letterSize,
                        fontWeight = if (strip && isActive) FontWeight.Bold else null,
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            strip -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        },
                    )
                }
            }
        }
        }
    }
}
