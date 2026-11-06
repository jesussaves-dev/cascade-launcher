package dev.tom.cascade.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dev.tom.cascade.admin.CascadeDeviceAdmin

private fun admin(context: Context) = ComponentName(context, CascadeDeviceAdmin::class.java)

fun isDeviceAdminActive(context: Context): Boolean =
    context.getSystemService(DevicePolicyManager::class.java).isAdminActive(admin(context))

fun requestDeviceAdmin(context: Context) {
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin(context))
        .putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Lets Cascade lock the screen when you double-tap the clock.",
        )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun lockDevice(context: Context): Boolean {
    if (!isDeviceAdminActive(context)) return false
    return runCatching {
        context.getSystemService(DevicePolicyManager::class.java).lockNow()
    }.isSuccess
}
