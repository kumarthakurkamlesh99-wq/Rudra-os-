package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rudra_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_LETS_STUDY_MODE = stringPreferencesKey("lets_study_mode") // "WEBVIEW", "BROWSER"
        val KEY_LOW_ENERGY_MODE = booleanPreferencesKey("low_energy_mode")
        val KEY_LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")

        // Notification Preferences
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_STUDY_REMINDERS_ENABLED = booleanPreferencesKey("study_reminders_enabled")
        val KEY_REVISION_REMINDERS_ENABLED = booleanPreferencesKey("revision_reminders_enabled")
        val KEY_TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val KEY_SHUTDOWN_REMINDERS_ENABLED = booleanPreferencesKey("shutdown_reminders_enabled")
        val KEY_RECOVERY_REMINDERS_ENABLED = booleanPreferencesKey("recovery_reminders_enabled")
        val KEY_WEEKLY_REVIEW_ENABLED = booleanPreferencesKey("weekly_review_enabled")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val KEY_QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val KEY_QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        val KEY_TASK_REMINDER_OFFSET = stringPreferencesKey("task_reminder_offset")
        val KEY_BLOCK1_TIME = stringPreferencesKey("block1_time")
        val KEY_BLOCK3_TIME = stringPreferencesKey("block3_time")
        val KEY_BLOCK5_TIME = stringPreferencesKey("block5_time")
        val KEY_SHUTDOWN_TIME = stringPreferencesKey("shutdown_time")
        val KEY_PERMISSION_PROMPT_SHOWN = booleanPreferencesKey("permission_prompt_shown")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "DARK"
    }

    val letsStudyMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LETS_STUDY_MODE] ?: "WEBVIEW"
    }

    val isLowEnergyMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_LOW_ENERGY_MODE] ?: false
    }

    val lastBackupDate: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_BACKUP_DATE] ?: "Never"
    }

    // Notification Flows
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }
    val studyRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_STUDY_REMINDERS_ENABLED] ?: true }
    val revisionRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REVISION_REMINDERS_ENABLED] ?: true }
    val taskRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_TASK_REMINDERS_ENABLED] ?: true }
    val shutdownRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHUTDOWN_REMINDERS_ENABLED] ?: true }
    val recoveryRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_RECOVERY_REMINDERS_ENABLED] ?: true }
    val weeklyReviewEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_WEEKLY_REVIEW_ENABLED] ?: true }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND_ENABLED] ?: true }
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_VIBRATION_ENABLED] ?: true }
    val quietHoursEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_QUIET_HOURS_ENABLED] ?: false }
    val quietHoursStart: Flow<String> = context.dataStore.data.map { it[KEY_QUIET_HOURS_START] ?: "22:00" }
    val quietHoursEnd: Flow<String> = context.dataStore.data.map { it[KEY_QUIET_HOURS_END] ?: "05:45" }
    val taskReminderOffset: Flow<String> = context.dataStore.data.map { it[KEY_TASK_REMINDER_OFFSET] ?: "30_MIN" }
    val block1Time: Flow<String> = context.dataStore.data.map { it[KEY_BLOCK1_TIME] ?: "06:15" }
    val block3Time: Flow<String> = context.dataStore.data.map { it[KEY_BLOCK3_TIME] ?: "15:30" }
    val block5Time: Flow<String> = context.dataStore.data.map { it[KEY_BLOCK5_TIME] ?: "20:15" }
    val shutdownTime: Flow<String> = context.dataStore.data.map { it[KEY_SHUTDOWN_TIME] ?: "21:15" }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = mode
        }
    }

    suspend fun setLetsStudyMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LETS_STUDY_MODE] = mode
        }
    }

    suspend fun setLowEnergyMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOW_ENERGY_MODE] = enabled
        }
    }

    suspend fun setLastBackupDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_BACKUP_DATE] = date
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setStudyRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_STUDY_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setRevisionRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REVISION_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_TASK_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setShutdownRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SHUTDOWN_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setRecoveryRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_RECOVERY_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setWeeklyReviewEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WEEKLY_REVIEW_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_QUIET_HOURS_ENABLED] = enabled }
    }

    suspend fun setQuietHoursRange(start: String, end: String) {
        context.dataStore.edit {
            it[KEY_QUIET_HOURS_START] = start
            it[KEY_QUIET_HOURS_END] = end
        }
    }

    suspend fun setTaskReminderOffset(offset: String) {
        context.dataStore.edit { it[KEY_TASK_REMINDER_OFFSET] = offset }
    }

    suspend fun setBlockTimes(block1: String, block3: String, block5: String, shutdown: String) {
        context.dataStore.edit {
            it[KEY_BLOCK1_TIME] = block1
            it[KEY_BLOCK3_TIME] = block3
            it[KEY_BLOCK5_TIME] = block5
            it[KEY_SHUTDOWN_TIME] = shutdown
        }
    }

    suspend fun setPermissionPromptShown(shown: Boolean) {
        context.dataStore.edit { it[KEY_PERMISSION_PROMPT_SHOWN] = shown }
    }
}
