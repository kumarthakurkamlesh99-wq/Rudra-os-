package com.example.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.preferences.UserPreferences
import com.example.notification.NotificationManagerService
import kotlinx.coroutines.flow.first

class WeeklyReviewWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val preferences = UserPreferences(applicationContext)
            val masterEnabled = preferences.notificationsEnabled.first()
            val weeklyEnabled = preferences.weeklyReviewEnabled.first()

            if (masterEnabled && weeklyEnabled) {
                val service = NotificationManagerService(applicationContext)
                val quietHoursEnabled = preferences.quietHoursEnabled.first()
                val quietHoursStart = preferences.quietHoursStart.first()
                val quietHoursEnd = preferences.quietHoursEnd.first()

                if (!service.isInQuietHours(quietHoursEnabled, quietHoursStart, quietHoursEnd)) {
                    service.showWeeklyReviewNotification(
                        title = "Weekly Review",
                        message = "Review your week and plan next week."
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
