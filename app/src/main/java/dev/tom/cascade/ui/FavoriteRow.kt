package dev.tom.cascade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.tom.cascade.model.AppEntry
import kotlin.math.abs
import kotlinx.coroutines.withTimeout

/**
 * A favourite row that lifts on long-press and follows the finger; the list order is
 * only changed on release, so the row never relayouts mid-gesture and cannot drift.
 * A long-press released without moving opens the app sheet.
 */
@Composable
fun FavoriteRow(
    app: AppEntry,
    icon: ImageBitmap?,
    iconSizeDp: Int,
    hasNotification: Boolean,
    showHandle: Boolean,
    dragging: Boolean,
    offsetPx: Float,
    onDragStart: (rowHeightPx: Float) -> Unit,
    onDrag: (deltaPx: Float) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val latestOnDragStart by rememberUpdatedState(onDragStart)
    val latestOnDrag by rememberUpdatedState(onDrag)
    val latestOnDragEnd by rememberUpdatedState(onDragEnd)
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnLongClick by rememberUpdatedState(onLongClick)
    val latestOnMoveUp by rememberUpdatedState(onMoveUp)
    val latestOnMoveDown by rememberUpdatedState(onMoveDown)

    var rowHeightPx by remember { mutableFloatStateOf(0f) }
    val liftedShape = remember { RoundedCornerShape(12.dp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { rowHeightPx = it.height.toFloat() }
            // Opaque while lifted so the row underneath cannot show through it mid-swap.
            // The layer comes first so the card travels with the row instead of staying in its old slot.
            .graphicsLayer {
                translationY = offsetPx
                shadowElevation = if (dragging) 8.dp.toPx() else 0f
                shape = liftedShape
                clip = false
            }
            .background(if (dragging) MaterialTheme.colorScheme.surface else Color.Transparent, liftedShape)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Move up") { latestOnMoveUp(); true },
                    CustomAccessibilityAction("Move down") { latestOnMoveDown(); true },
                )
            }
            .pointerInput(Unit) {
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
                        var travelled = 0f
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        latestOnDragStart(rowHeightPx)
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val dy = change.positionChange().y
                            if (dy == 0f) continue
                            change.consume()
                            travelled += abs(dy)
                            latestOnDrag(dy)
                        }
                        if (travelled < 12f) latestOnLongClick()
                        latestOnDragEnd()
                    }
                    if (tapped) latestOnClick()
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
