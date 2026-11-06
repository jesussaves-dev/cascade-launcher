package dev.tom.cascade

import android.app.Application
import android.content.Context
import dev.tom.cascade.data.AppsRepository
import dev.tom.cascade.data.Prefs
import dev.tom.cascade.iconpack.IconPackRepository

class CascadeApp : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var apps: AppsRepository
        private set
    lateinit var iconPacks: IconPackRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        apps = AppsRepository(this)
        apps.start()
        iconPacks = IconPackRepository(this, prefs)
    }
}

fun prefs(context: Context): Prefs = (context.applicationContext as CascadeApp).prefs

fun apps(context: Context): AppsRepository = (context.applicationContext as CascadeApp).apps
