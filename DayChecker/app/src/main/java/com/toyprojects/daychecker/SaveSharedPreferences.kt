package com.toyprojects.daychecker

import android.content.Context
import android.content.SharedPreferences

class SaveSharedPreferences(context: Context) {
    private val PREFS_FILE_NAME = "dc_prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, 0)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }
    fun setBoolean(key: String, bool: Boolean) {
        prefs.edit().putBoolean(key, bool).apply()
    }
}