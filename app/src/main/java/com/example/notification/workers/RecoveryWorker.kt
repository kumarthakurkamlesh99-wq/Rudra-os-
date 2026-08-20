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

class RecoveryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val preferences = UserPreferences(applicationContext)
            val masterEnabled = preferences.notificationsEnabled.first()
            val recoveryEnabled = preferences.recoveryRemindersEnabled.first()

            if (masterEnabled && recoveryEnabled) {
                val service = NotificationManagerService(applicationContext)
                val quietHoursEnabled = preferences.quietHoursEnabled.first()
                val quietHoursStart = preferences.quietHoursStart.first()
                val quietHoursEnd = preferences.quietHoursEnd.first()

                if (!service.isInQuietHours(quietHoursEnabled, quietHoursStart, quietHoursEnd)) {
                    val database = AppDatabase.getInstance(applicationContext)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val overdueTasks = database.taskDao().getOverdueTasksList(today)
                    val missedRevisions = database.revisionLogDao().getMissedRevisionsList(today)

                    // Threshold: >= 2 overdue tasks OR >= 2 missed revisions
                    if (overdueTasks.size >= 2 || missedRevisions.size >= 2) {
                        service.showRecoveryModeNotification(
                            title = "Recovery Mode Suggested",
                            message = "You are falling behind. Tap to open Recovery Mode."
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
