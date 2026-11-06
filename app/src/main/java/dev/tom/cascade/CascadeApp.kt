package dev.tom.cascade

import android.app.Application
import android.content.Context
import dev.tom.cascade.data.AppsRepository
import dev.tom.cascade.data.Prefs

class CascadeApp : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var apps: AppsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        apps = AppsRepository(this)
        apps.start()
    }
}

fun prefs(context: Context): Prefs = (context.applicationContext as CascadeApp).prefs

fun apps(context: Context): AppsRepository = (context.applicationContext as CascadeApp).apps
