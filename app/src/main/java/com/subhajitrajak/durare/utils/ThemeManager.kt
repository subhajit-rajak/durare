package com.subhajitrajak.durare.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    fun applyTheme(context: Context) {
        val prefs = Preferences.getInstance(context)
        val isDarkMode = prefs.isDarkTheme()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = Preferences.getInstance(context)
        prefs.setDarkTheme(enabled)
    }
}