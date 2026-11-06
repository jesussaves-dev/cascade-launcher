package dev.tom.cascade.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

fun isDefaultLauncher(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(android.app.role.RoleManager::class.java)
        return roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_HOME) == true
    }
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolved = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
    return resolved?.activityInfo?.packageName == context.packageName
}

fun openDefaultLauncherSettings(context: Context) {
    val candidates = listOf(
        Intent(Settings.ACTION_HOME_SETTINGS),
        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
        Intent(Settings.ACTION_SETTINGS),
    )
    candidates.firstOrNull { it.resolveActivity(context.packageManager) != null }?.let {
        context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
