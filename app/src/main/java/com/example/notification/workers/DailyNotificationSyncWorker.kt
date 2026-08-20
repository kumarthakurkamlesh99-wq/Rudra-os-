package com.example.notification.workers

import android.content.Context
import androidx.work.*
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.notification.NotificationHelper
import com.example.notification.RudraAlarmScheduler
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailyNotificationSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "rudra_daily_notification_sync"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DailyNotificationSyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 30,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val preferences = UserPreferences(applicationContext)
            val database = AppDatabase.getInstance(applicationContext)

            val masterEnabled = preferences.notificationsEnabled.first()
            if (!masterEnabled) {
                return Result.success()
            }

            val quietHoursEnabled = preferences.quietHoursEnabled.first()
            val quietHoursStart = preferences.quietHoursStart.first()
            val quietHoursEnd = preferences.quietHoursEnd.first()

            val inQuietHours = NotificationHelper.isInQuietHours(quietHoursEnabled, quietHoursStart, quietHoursEnd)

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Revision Reminder Check (if not in quiet hours and enabled)
            val revisionEnabled = preferences.revisionRemindersEnabled.first()
            if (revisionEnabled && !inQuietHours) {
                val dueRevisions = database.revisionLogDao().getDueRevisionsList(today)
                if (dueRevisions.isNotEmpty()) {
                    val count = dueRevisions.size
                    val message = if (count == 1) {
                        "1 revision scheduled today."
                    } else {
                        "$count revisions scheduled today."
                    }
                    NotificationHelper.showRevisionDueNotification(applicationContext, message)
                }
            }

            // 2. Recovery Mode Check
            val recoveryEnabled = preferences.recoveryRemindersEnabled.first()
            if (recoveryEnabled && !inQuietHours) {
                val overdueTasks = database.taskDao().getOverdueTasksList(today)
                val missedRevisions = database.revisionLogDao().getMissedRevisionsList(today)

                if (overdueTasks.size >= 2 || missedRevisions.size >= 2) {
                    NotificationHelper.showRecoveryModeNotification(
                        context = applicationContext,
                        title = "Recovery Mode Suggested",
                        message = "You are falling behind. Open Recovery Mode."
                    )
                }
            }

            // 3. Ensure routine alarms are active
            RudraAlarmScheduler.rescheduleAllRoutineAlarms(applicationContext, preferences)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
