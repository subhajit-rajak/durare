package com.subhajitrajak.pushcounter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.subhajitrajak.pushcounter.services.RestTimerService.Companion.CHANNEL_ID
import com.subhajitrajak.pushcounter.utils.reminderUtils.PushupReminderManager
import com.subhajitrajak.pushcounter.utils.ThemeManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applyTheme(this)
        Firebase.initialize(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            PushupReminderManager.CHANNEL_ID,
            "Pushup Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )
        val restTimerChannel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows the remaining rest time between sets"
        }
        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(restTimerChannel)
    }
}