package dev.tom.cascade.notifications

import android.app.PendingIntent

/** Notification content is kept in memory only; nothing is written to disk. */
data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val timeMillis: Long,
    val ongoing: Boolean,
    val dismissible: Boolean,
    val contentIntent: PendingIntent? = null,
)
