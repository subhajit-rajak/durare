package com.subhajitrajak.durare.utils.reminderUtils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Receiver for device reboot
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            PushupReminderManager.rescheduleReminder(context)
        }
    }
}