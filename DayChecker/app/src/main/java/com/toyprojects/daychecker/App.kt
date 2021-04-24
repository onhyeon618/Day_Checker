package com.toyprojects.daychecker
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner

class App: Application() {
    companion object {
        lateinit var prefs: SaveSharedPreferences
    }

    override fun onCreate() {
        prefs = SaveSharedPreferences(applicationContext)
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(applicationContext))
    }
}
