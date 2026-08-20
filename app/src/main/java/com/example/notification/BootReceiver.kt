package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notification.workers.RescheduleNotificationsWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            // Re-create notification channels in case of app update or reinstall
            val notifService = NotificationManagerService(context)
            notifService.createNotificationChannels()

            // Enqueue work to restore all alarms from database and datastore
            val workRequest = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
