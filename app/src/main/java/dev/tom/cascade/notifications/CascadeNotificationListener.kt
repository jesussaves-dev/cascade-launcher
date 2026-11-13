package dev.tom.cascade.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class CascadeNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        // The system can drop our listener token between connecting and this call (e.g. the
        // process restarting quickly), and then rejects it with a SecurityException.
        val active = try {
            activeNotifications.orEmpty()
        } catch (e: SecurityException) {
            return
        }
        NotificationStore.replaceAll(active.mapNotNull(::toItem))
        NotificationStore.dismisser = { key ->
            try {
                cancelNotification(key)
            } catch (e: SecurityException) {
                // Listener was unbound mid-dismiss; the notification stays until we reconnect.
            }
        }
    }

    override fun onListenerDisconnected() {
        NotificationStore.dismisser = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        toItem(sbn)?.let(NotificationStore::put)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationStore.remove(sbn.key)
    }

    override fun onDestroy() {
        NotificationStore.dismisser = null
        NotificationStore.clear()
        super.onDestroy()
    }

    private fun toItem(sbn: StatusBarNotification): NotificationItem? {
        val notification = sbn.notification
        val flags = notification.flags
        if (isGroupSummary(flags)) return null

        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "Notification"
        val text = sequenceOf(
            Notification.EXTRA_TEXT,
            Notification.EXTRA_BIG_TEXT,
            Notification.EXTRA_SUMMARY_TEXT,
        ).mapNotNull { extras.getCharSequence(it)?.toString()?.trim() }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()

        val ongoing = isOngoing(flags)
        return NotificationItem(
            key = sbn.key,
            packageName = sbn.packageName,
            title = title,
            text = text,
            timeMillis = sbn.postTime,
            ongoing = ongoing,
            dismissible = isDismissible(flags),
            contentIntent = notification.contentIntent,
        )
    }
}
