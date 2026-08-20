package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MainActivity

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notifId = intent.getIntExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, 0)
        val blockNumber = intent.getIntExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, 1)

        val notifService = NotificationManagerService(context)
        val scheduler = NotificationScheduler(context)

        // Dismiss the active notification first
        if (notifId != 0) {
            notifService.cancelNotification(notifId)
        }

        when (action) {
            NotificationConstants.ACTION_START_STUDY -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(NotificationConstants.EXTRA_TARGET_SCREEN, NotificationConstants.SCREEN_LETS_STUDY)
                    putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
                }
                context.startActivity(launchIntent)
            }

            NotificationConstants.ACTION_SNOOZE_15_MIN -> {
                scheduler.scheduleSnoozeAlarm(blockNumber, 15)
            }

            NotificationConstants.ACTION_SKIP_BLOCK -> {
                // Already dismissed
            }
        }
    }
}
