package com.subhajitrajak.pushcounter.utils.reminderUtils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import java.util.Calendar

class PushupReminderManager {

    companion object {

        private const val PREFS_NAME = "pushup_prefs"
        private const val PREF_HOUR = "hour"
        private const val PREF_MINUTE = "minute"
        const val CHANNEL_ID = "pushup_reminder_channel"
        const val NOTIFICATION_ID = 1001

        // Schedule daily notification
        fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
            // Save user preference
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPref.edit { putInt(PREF_HOUR, hour).putInt(PREF_MINUTE, minute) }

            val intent = Intent(context, PushupAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            // If the time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Permission granted, schedule exact alarm
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // fallback to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                // Pre-API 31, no permission required
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

        }

        // Cancel the reminder
        fun cancelReminder(context: Context) {
            val intent = Intent(context, PushupAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }

        // Reschedule on boot
        fun rescheduleAfterBoot(context: Context) {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hour = sharedPref.getInt(PREF_HOUR, 20)
            val minute = sharedPref.getInt(PREF_MINUTE, 0)
            scheduleDailyReminder(context, hour, minute)
        }
    }
}