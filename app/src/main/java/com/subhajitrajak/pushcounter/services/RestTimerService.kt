package com.subhajitrajak.pushcounter.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.subhajitrajak.pushcounter.R
import java.util.*

class RestTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val NOTIFICATION_ID = 2001
        const val EXTRA_REST_DURATION_MS = "extra_rest_duration_ms"
    }

    private var remainingMs: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var lastTick: Long = 0L

    private val updateRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val delta = now - lastTick
            remainingMs = (remainingMs - delta).coerceAtLeast(0)
            lastTick = now

            updateNotification()

            if (remainingMs > 0) {
                handler.postDelayed(this, 1000L)
            } else {
                stopSelf() // End when timer hits 0
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainingMs = intent?.getLongExtra(EXTRA_REST_DURATION_MS, 0L) ?: 0L
        lastTick = System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, buildNotification())
        handler.post(updateRunnable)

        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val formattedTime = formatTime(remainingMs)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentTitle("Resting...")
            .setContentText("Time remaining: $formattedTime")
            .setColor(getColor(R.color.primary))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the remaining rest time between sets"
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
