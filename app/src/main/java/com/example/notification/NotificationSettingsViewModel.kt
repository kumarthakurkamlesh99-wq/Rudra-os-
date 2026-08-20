package com.example.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.notification.permission.NotificationPermissionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationPreferencesRepository(application)
    private val scheduler = NotificationScheduler(application)
    private val service = NotificationManagerService(application)
    val permissionManager = NotificationPermissionManager(application)

    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val studyRemindersEnabled: StateFlow<Boolean> = repository.studyRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val revisionRemindersEnabled: StateFlow<Boolean> = repository.revisionRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val taskRemindersEnabled: StateFlow<Boolean> = repository.taskRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shutdownRemindersEnabled: StateFlow<Boolean> = repository.shutdownRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val recoveryRemindersEnabled: StateFlow<Boolean> = repository.recoveryRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val weeklyReviewEnabled: StateFlow<Boolean> = repository.weeklyReviewEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val soundEnabled: StateFlow<Boolean> = repository.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrationEnabled: StateFlow<Boolean> = repository.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val quietHoursEnabled: StateFlow<Boolean> = repository.quietHoursEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val quietHoursStart: StateFlow<String> = repository.quietHoursStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")

    val quietHoursEnd: StateFlow<String> = repository.quietHoursEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "05:45")

    val taskReminderOffset: StateFlow<String> = repository.taskReminderOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "30_MIN")

    val block1Time: StateFlow<String> = repository.block1Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "06:15")

    val block3Time: StateFlow<String> = repository.block3Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "15:30")

    val block5Time: StateFlow<String> = repository.block5Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "20:15")

    val shutdownTime: StateFlow<String> = repository.shutdownTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "21:15")

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            val preferences = UserPreferences(getApplication())
            scheduler.rescheduleAllRoutineAlarms(preferences)
        }
    }

    fun setStudyRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setStudyRemindersEnabled(enabled)
            val preferences = UserPreferences(getApplication())
            scheduler.rescheduleAllRoutineAlarms(preferences)
        }
    }

    fun setRevisionRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setRevisionRemindersEnabled(enabled) }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setTaskRemindersEnabled(enabled) }
    }

    fun setShutdownRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShutdownRemindersEnabled(enabled)
            val preferences = UserPreferences(getApplication())
            scheduler.rescheduleAllRoutineAlarms(preferences)
        }
    }

    fun setRecoveryRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setRecoveryRemindersEnabled(enabled) }
    }

    fun setWeeklyReviewEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setWeeklyReviewEnabled(enabled)
            val preferences = UserPreferences(getApplication())
            scheduler.rescheduleAllRoutineAlarms(preferences)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setQuietHoursEnabled(enabled) }
    }

    fun setQuietHoursRange(start: String, end: String) {
        viewModelScope.launch { repository.setQuietHoursRange(start, end) }
    }

    fun setTaskReminderOffset(offset: String) {
        viewModelScope.launch { repository.setTaskReminderOffset(offset) }
    }

    fun setBlockTimes(block1: String, block3: String, block5: String, shutdown: String) {
        viewModelScope.launch {
            repository.setBlockTimes(block1, block3, block5, shutdown)
            val preferences = UserPreferences(getApplication())
            scheduler.rescheduleAllRoutineAlarms(preferences)
        }
    }

    fun triggerTestNotification() {
        service.showTestNotification()
    }
}
