package com.subhajitrajak.pushcounter

import android.app.Application
import com.subhajitrajak.pushcounter.utils.ThemeManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applyTheme(this)
    }
}