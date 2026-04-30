package com.example.freetv.data

import android.content.Context
import android.content.SharedPreferences

class ThemeRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("freetv_settings", Context.MODE_PRIVATE)

    fun isDarkThemeEnabled(): Boolean {
        return prefs.getBoolean("dark_theme", false)
    }

    fun setDarkThemeEnabled(isDark: Boolean) {
        prefs.edit().putBoolean("dark_theme", isDark).apply()
    }
}