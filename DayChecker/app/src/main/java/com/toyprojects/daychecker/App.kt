package com.toyprojects.daychecker
import android.app.Application

class App: Application() {
    companion object {
        lateinit var prefs: SaveSharedPreferences
    }

    override fun onCreate() {
        prefs = SaveSharedPreferences(applicationContext)
        super.onCreate()
    }
}
