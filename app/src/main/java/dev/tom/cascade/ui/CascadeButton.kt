package dev.tom.cascade.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun CascadeButton(
    onTap: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeThreshold = with(LocalDensity.current) { 48.dp.toPx() }
    val tapSlop = with(LocalDensity.current) { 12.dp.toPx() }
    Surface(
        modifier = modifier
            .size(64.dp)
            .semantics { contentDescription = "Cascade button" }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    var last = down.position
                    var totalDy = 0f
                    var moved = 0f
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        totalDy += change.position.y - last.y
                        moved += (change.position - last).getDistance()
                        last = change.position
                        pressed = change.pressed
                        if (pressed) change.consume()
                    }
                    when {
                        totalDy <= -swipeThreshold -> onSwipeUp()
                        moved < tapSlop -> onTap()
                    }
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
    }
}
