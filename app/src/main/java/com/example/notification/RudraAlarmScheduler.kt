package com.example.notification

import android.content.Context
import com.example.data.preferences.UserPreferences

/**
 * Backward compatibility facade delegating to NotificationScheduler.
 */
object RudraAlarmScheduler {

    fun scheduleDailyAlarm(
        context: Context,
        alarmType: String,
        requestCode: Int,
        timeStr: String
    ) {
        NotificationScheduler(context).scheduleDailyAlarm(alarmType, requestCode, timeStr)
    }

    fun scheduleWeeklySundayAlarm(
        context: Context,
        requestCode: Int = NotificationConstants.NOTIF_ID_WEEKLY
    ) {
        NotificationScheduler(context).scheduleWeeklySundayAlarm(requestCode)
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        blockNumber: Int,
        snoozeMinutes: Int = 15
    ) {
        NotificationScheduler(context).scheduleSnoozeAlarm(blockNumber, snoozeMinutes)
    }

    fun scheduleTaskAlarm(
        context: Context,
        taskId: Long,
        taskTitle: String,
        triggerTimeMillis: Long
    ) {
        NotificationScheduler(context).scheduleTaskAlarm(taskId, taskTitle, triggerTimeMillis)
    }

    fun cancelAlarm(context: Context, requestCode: Int) {
        NotificationScheduler(context).cancelAlarm(requestCode)
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        NotificationScheduler(context).cancelTaskAlarm(taskId)
    }

    suspend fun rescheduleAllRoutineAlarms(context: Context, preferences: UserPreferences) {
        NotificationScheduler(context).rescheduleAllRoutineAlarms(preferences)
    }

    fun cancelAllRoutineAlarms(context: Context) {
        NotificationScheduler(context).cancelAllRoutineAlarms()
    }
}
