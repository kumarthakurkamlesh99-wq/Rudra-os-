package com.example.notification

import android.content.Context
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

class NotificationPreferencesRepository(context: Context) {

    private val preferences = UserPreferences(context)

    val notificationsEnabled: Flow<Boolean> = preferences.notificationsEnabled
    val studyRemindersEnabled: Flow<Boolean> = preferences.studyRemindersEnabled
    val revisionRemindersEnabled: Flow<Boolean> = preferences.revisionRemindersEnabled
    val taskRemindersEnabled: Flow<Boolean> = preferences.taskRemindersEnabled
    val shutdownRemindersEnabled: Flow<Boolean> = preferences.shutdownRemindersEnabled
    val recoveryRemindersEnabled: Flow<Boolean> = preferences.recoveryRemindersEnabled
    val weeklyReviewEnabled: Flow<Boolean> = preferences.weeklyReviewEnabled
    val soundEnabled: Flow<Boolean> = preferences.soundEnabled
    val vibrationEnabled: Flow<Boolean> = preferences.vibrationEnabled
    val quietHoursEnabled: Flow<Boolean> = preferences.quietHoursEnabled
    val quietHoursStart: Flow<String> = preferences.quietHoursStart
    val quietHoursEnd: Flow<String> = preferences.quietHoursEnd
    val taskReminderOffset: Flow<String> = preferences.taskReminderOffset
    val block1Time: Flow<String> = preferences.block1Time
    val block3Time: Flow<String> = preferences.block3Time
    val block5Time: Flow<String> = preferences.block5Time
    val shutdownTime: Flow<String> = preferences.shutdownTime
    val permissionPromptShown: Flow<Boolean> = preferences.permissionPromptShown

    suspend fun setNotificationsEnabled(enabled: Boolean) = preferences.setNotificationsEnabled(enabled)
    suspend fun setStudyRemindersEnabled(enabled: Boolean) = preferences.setStudyRemindersEnabled(enabled)
    suspend fun setRevisionRemindersEnabled(enabled: Boolean) = preferences.setRevisionRemindersEnabled(enabled)
    suspend fun setTaskRemindersEnabled(enabled: Boolean) = preferences.setTaskRemindersEnabled(enabled)
    suspend fun setShutdownRemindersEnabled(enabled: Boolean) = preferences.setShutdownRemindersEnabled(enabled)
    suspend fun setRecoveryRemindersEnabled(enabled: Boolean) = preferences.setRecoveryRemindersEnabled(enabled)
    suspend fun setWeeklyReviewEnabled(enabled: Boolean) = preferences.setWeeklyReviewEnabled(enabled)
    suspend fun setSoundEnabled(enabled: Boolean) = preferences.setSoundEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = preferences.setVibrationEnabled(enabled)
    suspend fun setQuietHoursEnabled(enabled: Boolean) = preferences.setQuietHoursEnabled(enabled)
    suspend fun setQuietHoursRange(start: String, end: String) = preferences.setQuietHoursRange(start, end)
    suspend fun setTaskReminderOffset(offset: String) = preferences.setTaskReminderOffset(offset)
    suspend fun setBlockTimes(b1: String, b3: String, b5: String, shutdown: String) = preferences.setBlockTimes(b1, b3, b5, shutdown)
    suspend fun setPermissionPromptShown(shown: Boolean) = preferences.setPermissionPromptShown(shown)
}
