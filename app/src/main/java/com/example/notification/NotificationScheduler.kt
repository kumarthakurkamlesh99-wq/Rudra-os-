package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar

class NotificationScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun scheduleExactOrAllowWhileIdle(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (canScheduleExact()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleDailyAlarm(alarmType: String, requestCode: Int, timeStr: String) {
        val parts = try {
            timeStr.split(":").map { it.trim().toInt() }
        } catch (e: Exception) {
            listOf(6, 15)
        }

        val hour = parts.getOrElse(0) { 6 }
        val minute = parts.getOrElse(1) { 15 }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ALARM_TRIGGER
            putExtra(NotificationConstants.EXTRA_ALARM_TYPE, alarmType)
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrAllowWhileIdle(calendar.timeInMillis, pendingIntent)
    }

    fun scheduleWeeklySundayAlarm(requestCode: Int = NotificationConstants.NOTIF_ID_WEEKLY) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ALARM_TRIGGER
            putExtra(NotificationConstants.EXTRA_ALARM_TYPE, NotificationConstants.TYPE_WEEKLY)
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrAllowWhileIdle(calendar.timeInMillis, pendingIntent)
    }

    fun scheduleSnoozeAlarm(blockNumber: Int, snoozeMinutes: Int = 15) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCode = when (blockNumber) {
            1 -> NotificationConstants.NOTIF_ID_BLOCK_1 + 500
            3 -> NotificationConstants.NOTIF_ID_BLOCK_3 + 500
            else -> NotificationConstants.NOTIF_ID_BLOCK_5 + 500
        }

        val alarmType = when (blockNumber) {
            1 -> NotificationConstants.TYPE_BLOCK_1
            3 -> NotificationConstants.TYPE_BLOCK_3
            else -> NotificationConstants.TYPE_BLOCK_5
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ALARM_TRIGGER
            putExtra(NotificationConstants.EXTRA_ALARM_TYPE, alarmType)
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(NotificationConstants.EXTRA_BLOCK_NUMBER, blockNumber)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrAllowWhileIdle(triggerTime, pendingIntent)
    }

    fun scheduleTaskAlarm(taskId: Long, taskTitle: String, triggerTimeMillis: Long) {
        if (triggerTimeMillis <= System.currentTimeMillis()) return

        val requestCode = (NotificationConstants.NOTIF_ID_TASK_BASE + (taskId % 1000)).toInt()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationConstants.ACTION_ALARM_TRIGGER
            putExtra(NotificationConstants.EXTRA_ALARM_TYPE, NotificationConstants.TYPE_TASK)
            putExtra(NotificationConstants.EXTRA_TASK_ID, taskId)
            putExtra(NotificationConstants.EXTRA_TASK_TITLE, taskTitle)
            putExtra(NotificationConstants.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactOrAllowWhileIdle(triggerTimeMillis, pendingIntent)
    }

    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun cancelTaskAlarm(taskId: Long) {
        val requestCode = (NotificationConstants.NOTIF_ID_TASK_BASE + (taskId % 1000)).toInt()
        cancelAlarm(requestCode)
    }

    suspend fun rescheduleAllRoutineAlarms(preferences: UserPreferences) {
        val enabled = preferences.notificationsEnabled.first()
        if (!enabled) {
            cancelAllRoutineAlarms()
            return
        }

        val studyEnabled = preferences.studyRemindersEnabled.first()
        val shutdownEnabled = preferences.shutdownRemindersEnabled.first()
        val weeklyEnabled = preferences.weeklyReviewEnabled.first()

        val block1Time = preferences.block1Time.first()
        val block3Time = preferences.block3Time.first()
        val block5Time = preferences.block5Time.first()
        val shutdownTime = preferences.shutdownTime.first()

        if (studyEnabled) {
            scheduleDailyAlarm(NotificationConstants.TYPE_BLOCK_1, NotificationConstants.NOTIF_ID_BLOCK_1, block1Time)
            scheduleDailyAlarm(NotificationConstants.TYPE_BLOCK_3, NotificationConstants.NOTIF_ID_BLOCK_3, block3Time)
            scheduleDailyAlarm(NotificationConstants.TYPE_BLOCK_5, NotificationConstants.NOTIF_ID_BLOCK_5, block5Time)
        } else {
            cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_1)
            cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_3)
            cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_5)
        }

        if (shutdownEnabled) {
            scheduleDailyAlarm(NotificationConstants.TYPE_SHUTDOWN, NotificationConstants.NOTIF_ID_SHUTDOWN, shutdownTime)
        } else {
            cancelAlarm(NotificationConstants.NOTIF_ID_SHUTDOWN)
        }

        if (weeklyEnabled) {
            scheduleWeeklySundayAlarm(NotificationConstants.NOTIF_ID_WEEKLY)
        } else {
            cancelAlarm(NotificationConstants.NOTIF_ID_WEEKLY)
        }
    }

    fun cancelAllRoutineAlarms() {
        cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_1)
        cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_3)
        cancelAlarm(NotificationConstants.NOTIF_ID_BLOCK_5)
        cancelAlarm(NotificationConstants.NOTIF_ID_SHUTDOWN)
        cancelAlarm(NotificationConstants.NOTIF_ID_WEEKLY)
    }
}
