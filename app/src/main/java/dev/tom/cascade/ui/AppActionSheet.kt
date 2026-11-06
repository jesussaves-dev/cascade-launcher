package dev.tom.cascade.ui

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tom.cascade.model.AppEntry
import dev.tom.cascade.notifications.NotificationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionSheet(
    app: AppEntry,
    pinned: Boolean,
    removable: Boolean,
    notifications: List<NotificationItem> = emptyList(),
    showPreviews: Boolean = true,
    onOpenNotification: (NotificationItem) -> Unit = {},
    onDismissNotification: (NotificationItem) -> Unit = {},
    onPinToggle: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onHide: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            if (notifications.isNotEmpty()) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                notifications.forEach { notification ->
                    NotificationRow(
                        notification = notification,
                        showPreview = showPreviews,
                        onOpen = { onOpenNotification(notification) },
                        onDismiss = { onDismissNotification(notification) },
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            ActionRow(if (pinned) "Unpin from favorites" else "Pin to favorites", onPinToggle)
            ActionRow("App info", onAppInfo)
            if (removable) ActionRow("Uninstall", onUninstall)
            ActionRow("Hide app", onHide)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationItem,
    showPreview: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpen)
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (showPreview) notification.title else "Notification",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showPreview && notification.text.isNotBlank()) {
                Text(
                    text = notification.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = DateUtils.getRelativeTimeSpanString(notification.timeMillis).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (notification.dismissible) {
            IconButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss notification")
            }
        } else {
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
