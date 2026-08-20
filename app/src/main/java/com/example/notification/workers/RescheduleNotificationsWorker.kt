package com.example.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import com.example.notification.RudraAlarmScheduler
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

class RescheduleNotificationsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val preferences = UserPreferences(applicationContext)
            val database = AppDatabase.getInstance(applicationContext)

            // Reschedule routine study blocks, shutdown, weekly review
            RudraAlarmScheduler.rescheduleAllRoutineAlarms(applicationContext, preferences)

            // Reschedule pending task alarms if task reminders enabled
            val taskRemindersEnabled = preferences.taskRemindersEnabled.first()
            val masterEnabled = preferences.notificationsEnabled.first()

            if (masterEnabled && taskRemindersEnabled) {
                val offsetType = preferences.taskReminderOffset.first()
                val offsetMillis = when (offsetType) {
                    "10_MIN" -> 10 * 60 * 1000L
                    "30_MIN" -> 30 * 60 * 1000L
                    "1_HOUR" -> 60 * 60 * 1000L
                    "1_DAY" -> 24 * 60 * 60 * 1000L
                    else -> 30 * 60 * 1000L
                }

                val activeTasks = database.taskDao().getPendingTasksList()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val now = System.currentTimeMillis()

                for (task in activeTasks) {
                    val dueDateStr = task.dueDate
                    if (!dueDateStr.isNullOrBlank()) {
                        try {
                            val parsedDate = dateFormat.parse(dueDateStr)
                            if (parsedDate != null) {
                                // Default due time to 18:00 on due date if time not specified
                                val dueTimeMillis = parsedDate.time + (18 * 3600 * 1000L)
                                val alertTime = dueTimeMillis - offsetMillis
                                if (alertTime > now) {
                                    RudraAlarmScheduler.scheduleTaskAlarm(
                                        context = applicationContext,
                                        taskId = task.id,
                                        taskTitle = task.title,
                                        triggerTimeMillis = alertTime
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Skip parse failures
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
