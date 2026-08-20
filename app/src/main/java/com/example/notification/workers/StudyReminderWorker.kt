package com.example.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notification.NotificationConstants
import com.example.notification.NotificationManagerService

class StudyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val blockNumber = inputData.getInt(NotificationConstants.EXTRA_BLOCK_NUMBER, 1)
            val notifService = NotificationManagerService(applicationContext)
            notifService.showStudyBlockNotification(
                blockNumber = blockNumber,
                title = "Study Block $blockNumber",
                message = "Time to begin your planned study session."
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
