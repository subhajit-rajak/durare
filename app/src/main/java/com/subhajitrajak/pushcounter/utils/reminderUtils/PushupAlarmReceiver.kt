package com.subhajitrajak.pushcounter.utils.reminderUtils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.ui.dashboard.HomeActivity

// Receiver to show notification
class PushupAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // intent to open the app when notification is clicked
        val activityIntent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PushupReminderManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentTitle("Time to Pushup!")
            .setContentText("Don't forget to complete your pushups today 💪")
            .setColor(context.getColor(R.color.primary))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(PushupReminderManager.NOTIFICATION_ID, notification)

        // Reschedule for next day
        PushupReminderManager.rescheduleReminder(context)
    }
}