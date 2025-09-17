package com.subhajitrajak.pushcounter.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Preferences private constructor(context: Context) {
    companion object {
        const val PREFS_NAME = "push_counter_prefs"
        const val KEY_THEME = "app_theme" // boolean
        const val KEY_SHOW_CAMERA = "show_camera_card" // boolean
        const val KEY_COUNTER_FEEDBACK = "counter_feedback" // boolean
        const val KEY_TOTAL_REPS = "total_reps" // int
        const val KEY_REST_TIME = "rest_time" // long

        @Volatile private var instance: Preferences? = null

        fun getInstance(context: Context): Preferences =
            instance ?: synchronized(this) {
                instance ?: Preferences(context.applicationContext).also { instance = it }
            }
    }

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Theme
    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_THEME, false)
    fun setDarkTheme(isDarkTheme: Boolean) = prefs.edit { putBoolean(KEY_THEME, isDarkTheme) }

    // Camera card
    fun isCameraCardEnabled(): Boolean = prefs.getBoolean(KEY_SHOW_CAMERA, true)
    fun setCameraCardEnabled(show: Boolean) = prefs.edit { putBoolean(KEY_SHOW_CAMERA, show) }

    // Counter feedback
    fun isCounterFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_COUNTER_FEEDBACK, true)
    fun setCounterFeedbackEnabled(enabled: Boolean) = prefs.edit { putBoolean(KEY_COUNTER_FEEDBACK, enabled) }

    // Total reps
    fun getTotalReps(): Int = prefs.getInt(KEY_TOTAL_REPS, 3)
    fun setTotalReps(totalReps: Int) = prefs.edit { putInt(KEY_TOTAL_REPS, totalReps) }

    // Rest time
    fun getRestTime(): Long = prefs.getLong(KEY_REST_TIME, 0L)
    fun setRestTime(restTime: Long) = prefs.edit { putLong(KEY_REST_TIME, restTime) }

    fun clear() = prefs.edit { clear() }
    fun remove(key: String) = prefs.edit { remove(key) }
}