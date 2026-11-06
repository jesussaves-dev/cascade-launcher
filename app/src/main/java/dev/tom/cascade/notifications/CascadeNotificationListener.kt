package dev.tom.cascade.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class CascadeNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationStore.replaceAll(activeNotifications.orEmpty().mapNotNull(::toItem))
        NotificationStore.dismisser = { key -> cancelNotification(key) }
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
        if (flags and Notification.FLAG_GROUP_SUMMARY != 0) return null

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

        val ongoing = flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE) != 0
        return NotificationItem(
            key = sbn.key,
            packageName = sbn.packageName,
            title = title,
            text = text,
            timeMillis = sbn.postTime,
            ongoing = ongoing,
            dismissible = !ongoing && flags and Notification.FLAG_NO_CLEAR == 0,
            contentIntent = notification.contentIntent,
        )
    }
}
