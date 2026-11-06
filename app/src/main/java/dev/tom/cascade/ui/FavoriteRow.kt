package dev.tom.cascade.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import dev.tom.cascade.model.AppEntry
import kotlin.math.abs
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

/**
 * A favourite row that lifts on long-press and reorders under the finger; a
 * long-press released without moving opens the app sheet. The grip only shows
 * while a favourite is being moved.
 */
@Composable
fun FavoriteRow(
    app: AppEntry,
    icon: ImageBitmap?,
    iconSizeDp: Int,
    hasNotification: Boolean,
    showHandle: Boolean,
    onDragStart: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMove: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowHeightPx = with(LocalDensity.current) { (maxOf(iconSizeDp, 24) + 16).dp.toPx() }
    val haptics = LocalHapticFeedback.current
    val latestOnMove by rememberUpdatedState(onMove)
    val latestOnMoveEnd by rememberUpdatedState(onMoveEnd)
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnLongClick by rememberUpdatedState(onLongClick)
    val latestOnDragStart by rememberUpdatedState(onDragStart)

    var pulled by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val liftedShape = remember { RoundedCornerShape(12.dp) }
    var travelled by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Opaque while lifted so the row underneath cannot show through it mid-swap.
            .background(if (dragging) MaterialTheme.colorScheme.surface else Color.Transparent, liftedShape)
            .graphicsLayer {
                translationY = pulled
                shadowElevation = if (dragging) 8.dp.toPx() else 0f
                shape = liftedShape
                clip = false
            }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Move up") { onMoveUp(); true },
                    CustomAccessibilityAction("Move down") { onMoveDown(); true },
                )
            }
            .pointerInput(rowHeightPx) {
                val longPressMs = viewConfiguration.longPressTimeoutMillis
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var tapped = false
                    var startedDrag = false
                    try {
                        withTimeout(longPressMs) {
                            while (true) {
                                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                                if (change == null) return@withTimeout
                                if (!change.pressed) {
                                    change.consume()
                                    tapped = true
                                    return@withTimeout
                                }
                                // Past the slop the list is scrolling, so leave it alone.
                                if ((change.position - down.position).getDistance() > slop) return@withTimeout
                            }
                        }
                    } catch (_: PointerEventTimeoutCancellationException) {
                        startedDrag = true
                        pulled = 0f
                        travelled = 0f
                        dragging = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        latestOnDragStart()
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val dy = change.positionChange().y
                            if (dy == 0f) continue
                            change.consume()
                            pulled += dy
                            travelled += abs(dy)
                            // Swap at half a row so the list keeps up with the finger.
                            val steps = (pulled / (rowHeightPx / 2f)).roundToInt()
                            if (steps != 0) {
                                latestOnMove(steps)
                                pulled -= steps * rowHeightPx
                            }
                        }
                    }
                    if (tapped) latestOnClick()
                    if (startedDrag) {
                        dragging = false
                        pulled = 0f
                        if (travelled < 12f) latestOnLongClick()
                        latestOnMoveEnd()
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppRow(
            app = app,
            icon = icon,
            iconSizeDp = iconSizeDp,
            hasNotification = hasNotification,
            onClick = onClick,
            onLongClick = onLongClick,
            interactive = false,
            modifier = Modifier.weight(1f),
        )
        if (showHandle || dragging) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
