package com.subhajitrajak.durare.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Preferences private constructor(context: Context) {
    companion object {
        const val PREFS_NAME = "push_counter_prefs"
        const val KEY_THEME = "app_theme" // boolean
        const val KEY_SHOW_CAMERA = "show_camera_card" // boolean
        const val KEY_SOUND_FEEDBACK = "sound_feedback" // boolean
        const val KEY_VIBRATION_FEEDBACK = "vibration_feedback" // boolean
        const val KEY_TOTAL_REPS = "total_reps" // int
        const val KEY_REST_TIME = "rest_time" // long
        const val PREF_HOUR = "hour" // reminder hours - int
        const val PREF_MINUTE = "minute" // reminder minutes - int
        const val PREF_REMINDER = "reminder" // boolean
        const val DOWN_THRESHOLD = "downThreshold" // float
        const val UP_THRESHOLD = "upThreshold" // float
        const val GOAL = "goal" // int
        const val WEIGHT = "weight" // double

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

    // Sound feedback
    fun isSoundFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_FEEDBACK, false)
    fun setSoundFeedbackEnabled(enabled: Boolean) = prefs.edit { putBoolean(KEY_SOUND_FEEDBACK, enabled) }

    // Vibration feedback
    fun isVibrationFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION_FEEDBACK, true)
    fun setVibrationFeedbackEnabled(enabled: Boolean) = prefs.edit { putBoolean(KEY_VIBRATION_FEEDBACK, enabled) }

    // Total reps
    fun getTotalReps(): Int = prefs.getInt(KEY_TOTAL_REPS, 5)
    fun setTotalReps(totalReps: Int) = prefs.edit { putInt(KEY_TOTAL_REPS, totalReps) }

    // Rest time
    fun getRestTime(): Long = prefs.getLong(KEY_REST_TIME, 2 * 60 * 1000L)
    fun setRestTime(restTime: Long) = prefs.edit { putLong(KEY_REST_TIME, restTime) }

    // Reminder
    fun getReminderHour(): Int = prefs.getInt(PREF_HOUR, 8)
    fun getReminderMinute(): Int = prefs.getInt(PREF_MINUTE, 30)
    fun setReminder(hour: Int, minute: Int) = prefs.edit { putInt(PREF_HOUR, hour).putInt(PREF_MINUTE, minute) }
    fun getReminderSet(): Boolean = prefs.getBoolean(PREF_REMINDER, false)
    fun setReminderSet(set: Boolean) = prefs.edit { putBoolean(PREF_REMINDER, set) }

    // thresholds for face detector
    fun getDownThreshold(): Float = prefs.getFloat(DOWN_THRESHOLD, 40f)
    fun setDownThreshold(downThreshold: Float) = prefs.edit { putFloat(DOWN_THRESHOLD, downThreshold) }
    fun getUpThreshold(): Float = prefs.getFloat(UP_THRESHOLD, 25f)
    fun setUpThreshold(upThreshold: Float) = prefs.edit { putFloat(UP_THRESHOLD, upThreshold) }

    fun getGoal(): Int = prefs.getInt(GOAL, 50)
    fun setGoal(value: Int) = prefs.edit { putInt(GOAL, value) }

    fun getWeight(): Float = prefs.getFloat(WEIGHT, 0f)
    fun setWeight(value: Float) = prefs.edit { putFloat(WEIGHT, value) }

    // reset all preferences in personalize screen to defaults
    fun resetPersonalizationsToDefaults() {
        prefs.edit {
            remove(KEY_SHOW_CAMERA)
            remove(KEY_SOUND_FEEDBACK)
            remove(KEY_VIBRATION_FEEDBACK)
            remove(KEY_TOTAL_REPS)
            remove(KEY_REST_TIME)
            remove(DOWN_THRESHOLD)
            remove(UP_THRESHOLD)
        }
    }
}