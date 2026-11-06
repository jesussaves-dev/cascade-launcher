package dev.tom.cascade.ui

import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.tom.cascade.model.AppEntry

/** A favourite row with a drag handle; moves are reported live and committed on release. */
@Composable
fun FavoriteRow(
    app: AppEntry,
    icon: ImageBitmap?,
    iconSizeDp: Int,
    hasNotification: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMove: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveEnd: () -> Unit,
) {
    val rowHeight = (maxOf(iconSizeDp, 24) + 16).dp
    val rowHeightPx = with(LocalDensity.current) { rowHeight.toPx() }
    var pulled by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppRow(
            app = app,
            icon = icon,
            iconSizeDp = iconSizeDp,
            hasNotification = hasNotification,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Reorder ${app.label}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .pointerInput(rowHeightPx) {
                    detectDragGestures(
                        onDragStart = { pulled = 0f },
                        onDragEnd = { onMoveEnd() },
                        onDragCancel = { onMoveEnd() },
                    ) { change, dragAmount ->
                        change.consume()
                        pulled += dragAmount.y
                        val steps = (pulled / rowHeightPx).toInt()
                        if (steps != 0) {
                            onMove(steps)
                            pulled -= steps * rowHeightPx
                        }
                    }
                }
                .semantics {
                    customActions = listOf(
                        CustomAccessibilityAction("Move up") { onMoveUp(); true },
                        CustomAccessibilityAction("Move down") { onMoveDown(); true },
                    )
                },
        )
    }
}
