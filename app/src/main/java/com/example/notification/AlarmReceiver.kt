package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmType = intent.getStringExtra(NotificationConstants.EXTRA_ALARM_TYPE) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = UserPreferences(context)
                val masterEnabled = preferences.notificationsEnabled.first()
                if (!masterEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val notifService = NotificationManagerService(context)
                val scheduler = NotificationScheduler(context)

                when (alarmType) {
                    NotificationConstants.TYPE_BLOCK_1 -> {
                        val enabled = preferences.studyRemindersEnabled.first()
                        if (enabled) {
                            notifService.showStudyBlockNotification(
                                blockNumber = 1,
                                title = "Study Block 1",
                                message = "Time to begin your planned study session."
                            )
                        }
                        val timeStr = preferences.block1Time.first()
                        scheduler.scheduleDailyAlarm(alarmType, NotificationConstants.NOTIF_ID_BLOCK_1, timeStr)
                    }

                    NotificationConstants.TYPE_BLOCK_3 -> {
                        val enabled = preferences.studyRemindersEnabled.first()
                        if (enabled) {
                            notifService.showStudyBlockNotification(
                                blockNumber = 3,
                                title = "Study Block 3",
                                message = "Time to begin your planned study session."
                            )
                        }
                        val timeStr = preferences.block3Time.first()
                        scheduler.scheduleDailyAlarm(alarmType, NotificationConstants.NOTIF_ID_BLOCK_3, timeStr)
                    }

                    NotificationConstants.TYPE_BLOCK_5 -> {
                        val enabled = preferences.studyRemindersEnabled.first()
                        if (enabled) {
                            notifService.showStudyBlockNotification(
                                blockNumber = 5,
                                title = "Study Block 5",
                                message = "Time to begin your planned study session."
                            )
                        }
                        val timeStr = preferences.block5Time.first()
                        scheduler.scheduleDailyAlarm(alarmType, NotificationConstants.NOTIF_ID_BLOCK_5, timeStr)
                    }

                    NotificationConstants.TYPE_SHUTDOWN -> {
                        val enabled = preferences.shutdownRemindersEnabled.first()
                        if (enabled) {
                            notifService.showShutdownRitualNotification(
                                title = "Shutdown Ritual",
                                message = "Review today and prepare tomorrow."
                            )
                        }
                        val timeStr = preferences.shutdownTime.first()
                        scheduler.scheduleDailyAlarm(alarmType, NotificationConstants.NOTIF_ID_SHUTDOWN, timeStr)
                    }

                    NotificationConstants.TYPE_WEEKLY -> {
                        val enabled = preferences.weeklyReviewEnabled.first()
                        if (enabled) {
                            notifService.showWeeklyReviewNotification(
                                title = "Weekly Review",
                                message = "Review your week and plan next week."
                            )
                        }
                        scheduler.scheduleWeeklySundayAlarm(NotificationConstants.NOTIF_ID_WEEKLY)
                    }

                    NotificationConstants.TYPE_REVISION -> {
                        val enabled = preferences.revisionRemindersEnabled.first()
                        if (enabled) {
                            notifService.showRevisionDueNotification(
                                message = "You have revisions scheduled today."
                            )
                        }
                    }

                    NotificationConstants.TYPE_TASK -> {
                        val enabled = preferences.taskRemindersEnabled.first()
                        val taskId = intent.getLongExtra(NotificationConstants.EXTRA_TASK_ID, 0L)
                        val taskTitle = intent.getStringExtra(NotificationConstants.EXTRA_TASK_TITLE) ?: "Task Reminder"
                        if (enabled && taskId > 0L) {
                            val database = AppDatabase.getInstance(context)
                            val task = database.taskDao().getTaskById(taskId)
                            if (task != null && !task.isCompleted && !task.isArchived) {
                                val subjectPart = if (task.subjectName.isNotBlank()) "${task.subjectName} • " else ""
                                val duePart = if (!task.dueDate.isNullOrBlank()) "Due: ${task.dueDate}" else "Priority: ${task.priority}"
                                notifService.showTaskReminderNotification(
                                    taskId = taskId,
                                    title = "Task Reminder: ${task.title}",
                                    message = "$subjectPart$duePart"
                                )
                            }
                        }
                    }

                    NotificationConstants.TYPE_RECOVERY -> {
                        val enabled = preferences.recoveryRemindersEnabled.first()
                        if (enabled) {
                            notifService.showRecoveryModeNotification(
                                title = "Recovery Mode Suggested",
                                message = "You are falling behind. Open Recovery Mode."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
