package dev.tom.cascade.notifications

import android.app.Notification

fun isOngoing(flags: Int): Boolean =
    flags and (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_FOREGROUND_SERVICE) != 0

fun isDismissible(flags: Int): Boolean = !isOngoing(flags) && flags and Notification.FLAG_NO_CLEAR == 0

fun isGroupSummary(flags: Int): Boolean = flags and Notification.FLAG_GROUP_SUMMARY != 0
