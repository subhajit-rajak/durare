package com.subhajitrajak.durare.utils.reminderUtils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.subhajitrajak.durare.R

class RestAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val REST_ALARM_NOTIFICATION_ID = 3001
        const val REST_COUNTDOWN_NOTIFICATION_ID = 3002
        const val REST_ALARM_REQUEST_CODE = 3001
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentTitle("Rest finished")
            .setContentText("Time to start your next set")
            .setColor(context.getColor(R.color.primary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(REST_ALARM_NOTIFICATION_ID, notification)
        }
    }
}