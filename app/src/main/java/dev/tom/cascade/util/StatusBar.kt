package dev.tom.cascade.util

import android.annotation.SuppressLint
import android.content.Context

/**
 * Pulls down the notification shade. There is no public API for this; launchers call
 * StatusBarManager.expandNotificationsPanel, which needs EXPAND_STATUS_BAR (a normal
 * permission). Returns false if this Android build refuses, so the caller can fall back.
 */
@SuppressLint("WrongConstant")
fun expandNotificationShade(context: Context): Boolean = runCatching {
    val statusBar = context.getSystemService("statusbar") ?: return false
    Class.forName("android.app.StatusBarManager")
        .getMethod("expandNotificationsPanel")
        .invoke(statusBar)
    true
}.getOrDefault(false)
