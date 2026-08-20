package com.example.notification

import android.content.Context

/**
 * Backward compatibility facade delegating to NotificationManagerService.
 */
object NotificationHelper {

    fun createNotificationChannels(context: Context) {
        NotificationManagerService(context).createNotificationChannels()
    }

    suspend fun showStudyBlockNotification(
        context: Context,
        blockNumber: Int,
        title: String = "Study Block $blockNumber",
        message: String = "Time to begin your planned study session."
    ) {
        NotificationManagerService(context).showStudyBlockNotification(blockNumber, title, message)
    }

    suspend fun showRevisionDueNotification(
        context: Context,
        title: String = "Revision Due",
        message: String = "You have revisions scheduled today."
    ) {
        NotificationManagerService(context).showRevisionDueNotification(title, message)
    }

    suspend fun showTaskReminderNotification(
        context: Context,
        taskId: Long,
        title: String,
        message: String
    ) {
        NotificationManagerService(context).showTaskReminderNotification(taskId, title, message)
    }

    suspend fun showShutdownRitualNotification(
        context: Context,
        title: String = "Shutdown Ritual",
        message: String = "Review today and prepare tomorrow."
    ) {
        NotificationManagerService(context).showShutdownRitualNotification(title, message)
    }

    suspend fun showWeeklyReviewNotification(
        context: Context,
        title: String = "Weekly Review",
        message: String = "Review your week and plan next week."
    ) {
        NotificationManagerService(context).showWeeklyReviewNotification(title, message)
    }

    suspend fun showRecoveryModeNotification(
        context: Context,
        title: String = "Recovery Mode Suggested",
        message: String = "You are falling behind. Open Recovery Mode."
    ) {
        NotificationManagerService(context).showRecoveryModeNotification(title, message)
    }

    fun showTestNotification(context: Context) {
        NotificationManagerService(context).showTestNotification()
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerService(context).cancelNotification(notificationId)
    }

    fun isInQuietHours(enabled: Boolean, startStr: String, endStr: String): Boolean {
        if (!enabled) return false
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(java.util.Calendar.MINUTE)
        val currentTotalMin = currentHour * 60 + currentMin

        val (startH, startM) = try {
            val parts = startStr.split(":").map { it.trim().toInt() }
            Pair(parts.getOrElse(0) { 22 }, parts.getOrElse(1) { 0 })
        } catch (e: Exception) {
            Pair(22, 0)
        }
        val startTotalMin = startH * 60 + startM

        val (endH, endM) = try {
            val parts = endStr.split(":").map { it.trim().toInt() }
            Pair(parts.getOrElse(0) { 5 }, parts.getOrElse(1) { 45 })
        } catch (e: Exception) {
            Pair(5, 45)
        }
        val endTotalMin = endH * 60 + endM

        return if (startTotalMin <= endTotalMin) {
            currentTotalMin in startTotalMin..endTotalMin
        } else {
            currentTotalMin >= startTotalMin || currentTotalMin <= endTotalMin
        }
    }
}
