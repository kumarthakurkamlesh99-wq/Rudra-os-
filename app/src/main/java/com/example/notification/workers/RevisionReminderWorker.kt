package com.example.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.notification.NotificationManagerService
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RevisionReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = UserPreferences(applicationContext)
            val masterEnabled = prefs.notificationsEnabled.first()
            val revisionEnabled = prefs.revisionRemindersEnabled.first()

            if (!masterEnabled || !revisionEnabled) {
                return Result.success()
            }

            val database = AppDatabase.getInstance(applicationContext)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val dueRevisions = database.revisionLogDao().getDueRevisionsList(today)

            if (dueRevisions.isNotEmpty()) {
                val notifService = NotificationManagerService(applicationContext)
                val count = dueRevisions.size
                val message = if (count == 1) {
                    "1 revision scheduled today."
                } else {
                    "$count revisions scheduled today."
                }
                notifService.showRevisionDueNotification(
                    title = "Revision Due",
                    message = message
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
