package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

        // Exam Countdown & Mission Target Preferences
        val KEY_TARGET_BOARD = stringPreferencesKey("target_board_name")
        val KEY_TARGET_SCORE = stringPreferencesKey("target_score_percent")
        val KEY_BOARD_EXAM_DATE = stringPreferencesKey("board_exam_date")
        val KEY_PHYSICS_EXAM_DATE = stringPreferencesKey("physics_exam_date")
        val KEY_CHEMISTRY_EXAM_DATE = stringPreferencesKey("chemistry_exam_date")
        val KEY_BIOLOGY_EXAM_DATE = stringPreferencesKey("biology_exam_date")
        val KEY_WEEKLY_CHAPTER_TARGET = intPreferencesKey("weekly_chapter_target")
        val KEY_WEEKLY_LECTURE_TARGET = intPreferencesKey("weekly_lecture_target")
        val KEY_WEEKLY_MOCK_TARGET = intPreferencesKey("weekly_mock_target")

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

        // AI API Integration Preferences
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val KEY_GEMINI_API_STATUS = stringPreferencesKey("gemini_api_status")
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_KEY] ?: ""
    }

    val geminiModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_MODEL] ?: "gemini-2.5-flash"
    }

    val geminiApiStatus: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_GEMINI_API_STATUS] ?: "NOT_CONFIGURED"
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

    // Exam Goals & Countdown Flows
    val targetBoard: Flow<String> = context.dataStore.data.map { it[KEY_TARGET_BOARD] ?: "BSEB Class 12 Board 2027" }
    val targetScore: Flow<String> = context.dataStore.data.map { it[KEY_TARGET_SCORE] ?: "85%+" }
    val boardExamDate: Flow<String> = context.dataStore.data.map { it[KEY_BOARD_EXAM_DATE] ?: "2027-02-15" }
    val physicsExamDate: Flow<String> = context.dataStore.data.map { it[KEY_PHYSICS_EXAM_DATE] ?: "2027-02-18" }
    val chemistryExamDate: Flow<String> = context.dataStore.data.map { it[KEY_CHEMISTRY_EXAM_DATE] ?: "2027-02-22" }
    val biologyExamDate: Flow<String> = context.dataStore.data.map { it[KEY_BIOLOGY_EXAM_DATE] ?: "2027-02-26" }
    val weeklyChapterTarget: Flow<Int> = context.dataStore.data.map { it[KEY_WEEKLY_CHAPTER_TARGET] ?: 2 }
    val weeklyLectureTarget: Flow<Int> = context.dataStore.data.map { it[KEY_WEEKLY_LECTURE_TARGET] ?: 25 }
    val weeklyMockTarget: Flow<Int> = context.dataStore.data.map { it[KEY_WEEKLY_MOCK_TARGET] ?: 2 }

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
    val permissionPromptShown: Flow<Boolean> = context.dataStore.data.map { it[KEY_PERMISSION_PROMPT_SHOWN] ?: false }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences -> preferences[KEY_THEME] = mode }
    }

    suspend fun setLetsStudyMode(mode: String) {
        context.dataStore.edit { preferences -> preferences[KEY_LETS_STUDY_MODE] = mode }
    }

    suspend fun setLowEnergyMode(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[KEY_LOW_ENERGY_MODE] = enabled }
    }

    suspend fun setLastBackupDate(date: String) {
        context.dataStore.edit { preferences -> preferences[KEY_LAST_BACKUP_DATE] = date }
    }

    suspend fun setMissionGoals(board: String, score: String, boardDate: String, phyDate: String, chemDate: String, bioDate: String) {
        context.dataStore.edit {
            it[KEY_TARGET_BOARD] = board
            it[KEY_TARGET_SCORE] = score
            it[KEY_BOARD_EXAM_DATE] = boardDate
            it[KEY_PHYSICS_EXAM_DATE] = phyDate
            it[KEY_CHEMISTRY_EXAM_DATE] = chemDate
            it[KEY_BIOLOGY_EXAM_DATE] = bioDate
        }
    }

    suspend fun setWeeklyTargets(chapters: Int, lectures: Int, mocks: Int) {
        context.dataStore.edit {
            it[KEY_WEEKLY_CHAPTER_TARGET] = chapters
            it[KEY_WEEKLY_LECTURE_TARGET] = lectures
            it[KEY_WEEKLY_MOCK_TARGET] = mocks
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

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = key.trim() }
    }

    suspend fun setGeminiModel(model: String) {
        context.dataStore.edit { it[KEY_GEMINI_MODEL] = model }
    }

    suspend fun setGeminiApiStatus(status: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_STATUS] = status }
    }
}
