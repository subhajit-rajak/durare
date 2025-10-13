package com.subhajitrajak.durare.utils.reminderUtils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.subhajitrajak.durare.utils.Preferences
import java.util.Calendar

class PushupReminderManager {

    companion object {
        const val CHANNEL_ID = "pushup_reminder_channel"
        const val NOTIFICATION_ID = 1001

        // Schedule daily notification
        fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
            // Save user preference

            val preferences = Preferences.getInstance(context)
            preferences.setReminder(hour, minute)

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

            Preferences.getInstance(context).setReminderSet(true)
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

            // Update stored reminder flag
            Preferences.getInstance(context).setReminderSet(false)
        }

        // Reschedule on boot
        fun rescheduleReminder(context: Context) {
            val preferences = Preferences.getInstance(context)
            val hour = preferences.getReminderHour()
            val minute = preferences.getReminderMinute()
            scheduleDailyReminder(context, hour, minute)
        }

        fun isReminderSet(context: Context): Boolean {
            return Preferences.getInstance(context).getReminderSet()
        }
    }
}