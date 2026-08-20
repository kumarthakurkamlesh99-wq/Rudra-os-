package com.example.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.notification.NotificationConstants
import com.example.notification.NotificationManagerService
import kotlinx.coroutines.flow.first

class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong(NotificationConstants.EXTRA_TASK_ID, 0L)
            if (taskId <= 0L) return Result.failure()

            val prefs = UserPreferences(applicationContext)
            val masterEnabled = prefs.notificationsEnabled.first()
            val taskRemindersEnabled = prefs.taskRemindersEnabled.first()

            if (!masterEnabled || !taskRemindersEnabled) {
                return Result.success()
            }

            val database = AppDatabase.getInstance(applicationContext)
            val task = database.taskDao().getTaskById(taskId)
            if (task != null && !task.isCompleted && !task.isArchived) {
                val notifService = NotificationManagerService(applicationContext)
                val subjectPart = if (task.subjectName.isNotBlank()) "${task.subjectName} • " else ""
                val duePart = if (!task.dueDate.isNullOrBlank()) "Due: ${task.dueDate}" else "Priority: ${task.priority}"

                notifService.showTaskReminderNotification(
                    taskId = task.id,
                    title = "Task Reminder: ${task.title}",
                    message = "$subjectPart$duePart"
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
