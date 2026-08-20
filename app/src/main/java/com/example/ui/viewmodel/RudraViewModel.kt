package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.preferences.UserPreferences
import com.example.data.repository.RudraRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RudraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val preferences = UserPreferences(application)
    val repository = RudraRepository(database, preferences)
    val backupManager = BackupManager(application, database)
    private val geminiService = GeminiAiService()

    // Notification State Flows
    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val studyRemindersEnabled: StateFlow<Boolean> = preferences.studyRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val revisionRemindersEnabled: StateFlow<Boolean> = preferences.revisionRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val taskRemindersEnabled: StateFlow<Boolean> = preferences.taskRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val shutdownRemindersEnabled: StateFlow<Boolean> = preferences.shutdownRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val recoveryRemindersEnabled: StateFlow<Boolean> = preferences.recoveryRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val weeklyReviewEnabled: StateFlow<Boolean> = preferences.weeklyReviewEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val soundEnabled: StateFlow<Boolean> = preferences.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vibrationEnabled: StateFlow<Boolean> = preferences.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val quietHoursEnabled: StateFlow<Boolean> = preferences.quietHoursEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val quietHoursStart: StateFlow<String> = preferences.quietHoursStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")
    val quietHoursEnd: StateFlow<String> = preferences.quietHoursEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "05:45")
    val taskReminderOffset: StateFlow<String> = preferences.taskReminderOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "30_MIN")
    val block1Time: StateFlow<String> = preferences.block1Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "06:15")
    val block3Time: StateFlow<String> = preferences.block3Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "15:30")
    val block5Time: StateFlow<String> = preferences.block5Time
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "20:15")
    val shutdownTime: StateFlow<String> = preferences.shutdownTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "21:15")

    init {
        // Initialize Real Android Notification Channels and WorkManager sync
        com.example.notification.NotificationHelper.createNotificationChannels(application)
        com.example.notification.workers.DailyNotificationSyncWorker.schedulePeriodicWork(application)
        viewModelScope.launch {
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(application, preferences)
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Repository Flows
    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weakChapters: StateFlow<List<ChapterEntity>> = repository.weakChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueRevisions: StateFlow<List<RevisionLogEntity>> = repository.getDueRevisions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedRevisions: StateFlow<List<RevisionLogEntity>> = repository.completedRevisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScorecards: StateFlow<List<ScorecardEntity>> = repository.allScorecards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<TimelinePresetEntity>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePreset: StateFlow<TimelinePresetEntity?> = repository.activePreset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedPresetId = MutableStateFlow<Long?>(null)
    val selectedPresetId: StateFlow<Long?> = _selectedPresetId.asStateFlow()

    fun setSelectedPresetId(presetId: Long) {
        _selectedPresetId.value = presetId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentTimelineBlocks: StateFlow<List<TimelineBlockEntity>> = _selectedPresetId
        .flatMapLatest { presetId ->
            if (presetId != null) {
                repository.getBlocksForPreset(presetId)
            } else {
                activePreset.flatMapLatest { active ->
                    if (active != null) repository.getBlocksForPreset(active.id)
                    else flowOf(emptyList())
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueTasks: StateFlow<List<TaskEntity>> = repository.getOverdueTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayScorecard: StateFlow<ScorecardEntity?> = repository.getTodayScorecard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val last7DaysScorecards: StateFlow<List<ScorecardEntity>> = repository.last7DaysScorecards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val last30DaysScorecards: StateFlow<List<ScorecardEntity>> = repository.last30DaysScorecards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayJournal: StateFlow<JournalEntryEntity?> = repository.getTodayJournalEntry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unprocessedBrainDumps: StateFlow<List<BrainDumpEntity>> = repository.unprocessedBrainDumps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val letsStudyMode: StateFlow<String> = repository.letsStudyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "WEBVIEW")

    val isLowEnergyMode: StateFlow<Boolean> = repository.isLowEnergyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Study Timer State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerSubject = MutableStateFlow("Physics")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private val _timerTopic = MutableStateFlow("")
    val timerTopic: StateFlow<String> = _timerTopic.asStateFlow()

    private var timerJob: Job? = null
    private var timerStartTimeMs: Long = 0

    fun startTimer(subject: String, topic: String) {
        _timerSubject.value = subject
        _timerTopic.value = topic
        if (!_isTimerRunning.value) {
            _isTimerRunning.value = true
            timerStartTimeMs = System.currentTimeMillis() - (_timerSeconds.value * 1000L)
            timerJob = viewModelScope.launch {
                while (_isTimerRunning.value) {
                    delay(1000)
                    _timerSeconds.value += 1
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = 0
    }

    fun stopAndSaveTimer(notes: String = "") {
        val durationMin = (_timerSeconds.value / 60).coerceAtLeast(1)
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    subjectName = _timerSubject.value,
                    topic = _timerTopic.value.ifBlank { "Deep Study" },
                    startTimeMs = timerStartTimeMs,
                    endTimeMs = now,
                    durationMinutes = durationMin,
                    isDeepWork = true,
                    notes = notes,
                    dateString = repository.getTodayDateString()
                )
            )
            resetTimer()
        }
    }

    // AI State
    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun clearAiResult() {
        _aiResult.value = null
    }

    fun requestAiSummary(subject: String, chapter: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = geminiService.generateChapterSummary(subject, chapter)
            _aiResult.value = res.getOrDefault("Summary unavailable")
            _isAiLoading.value = false
        }
    }

    fun requestAiQuiz(subject: String, chapter: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = geminiService.generatePracticeQuiz(subject, chapter)
            _aiResult.value = res.getOrDefault("Quiz unavailable")
            _isAiLoading.value = false
        }
    }

    fun requestAiFlashcards(subject: String, chapter: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = geminiService.generateFlashcards(subject, chapter)
            _aiResult.value = res.getOrDefault("Flashcards unavailable")
            _isAiLoading.value = false
        }
    }

    fun requestAiDoubtSolution(subject: String, doubt: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = geminiService.solveDoubt(subject, doubt)
            _aiResult.value = res.getOrDefault("Doubt solution unavailable")
            _isAiLoading.value = false
        }
    }

    // User Actions
    fun toggleLowEnergyMode() {
        viewModelScope.launch {
            val current = isLowEnergyMode.value
            repository.setLowEnergyMode(!current)
            if (!current) {
                // Find Low Energy preset and activate it
                val presets = allPresets.value
                val lowEnergy = presets.find { it.name.contains("Low Energy", ignoreCase = true) }
                if (lowEnergy != null) {
                    repository.activatePreset(lowEnergy.id)
                }
            } else {
                val schoolDay = allPresets.value.find { it.name.contains("School Day", ignoreCase = true) }
                if (schoolDay != null) {
                    repository.activatePreset(schoolDay.id)
                }
            }
        }
    }

    fun openExternalPWThor() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pwthor.live")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun saveScorecard(
        wokeUpBy630: Boolean,
        completedBlock1: Boolean,
        completedBlock3: Boolean,
        completedFitness: Boolean,
        completedBlock5: Boolean,
        didShutdownRitual: Boolean,
        noPhoneBlocked: Boolean,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.saveOrUpdateTodayScorecard(
                wokeUpBy630 = wokeUpBy630,
                completedBlock1 = completedBlock1,
                completedBlock3 = completedBlock3,
                completedFitness = completedFitness,
                completedBlock5 = completedBlock5,
                didShutdownRitual = didShutdownRitual,
                noPhoneBlocked = noPhoneBlocked,
                notes = notes,
                isLowEnergyDay = isLowEnergyMode.value
            )
        }
    }

    fun markRevisionDone(logId: Long, chapterId: Long, interval: String) {
        viewModelScope.launch {
            repository.markRevisionCompleted(logId, chapterId, interval)
        }
    }

    fun saveBrainDump(content: String, category: String) {
        viewModelScope.launch {
            repository.insertBrainDump(
                BrainDumpEntity(
                    content = content,
                    category = category
                )
            )
        }
    }

    fun convertBrainDumpToTask(brainDump: BrainDumpEntity) {
        viewModelScope.launch {
            val taskId = repository.insertTask(
                TaskEntity(
                    title = brainDump.content,
                    category = "BrainDump",
                    priority = "Normal"
                )
            )
            repository.updateBrainDump(brainDump.copy(isProcessed = true, convertedToTaskId = taskId))
        }
    }

    fun saveEveningJournal(
        mood: String,
        wins: String,
        missed: String,
        tomorrowFocus: String,
        reflection: String
    ) {
        val todayStr = repository.getTodayDateString()
        viewModelScope.launch {
            val existing = todayJournal.value
            val entry = JournalEntryEntity(
                id = existing?.id ?: 0,
                dateString = todayStr,
                mood = mood,
                winsDone = wins,
                missedWhat = missed,
                tomorrowFocusAndBlock1 = tomorrowFocus,
                generalReflection = reflection
            )
            if (existing != null) {
                repository.updateJournalEntry(entry)
            } else {
                repository.insertJournalEntry(entry)
            }
        }
    }

    fun saveSundayWeeklyReview(
        strongDay: String,
        weakDayAndTrigger: String,
        neglectedSubject: String,
        oneAdjustment: String
    ) {
        val todayStr = repository.getTodayDateString()
        viewModelScope.launch {
            val existing = todayJournal.value
            val entry = (existing ?: JournalEntryEntity(dateString = todayStr)).copy(
                isWeeklyReview = true,
                weeklyReviewStrongDay = strongDay,
                weeklyReviewWeakDayAndTrigger = weakDayAndTrigger,
                weeklyReviewNeglectedSubject = neglectedSubject,
                weeklyReviewOneAdjustment = oneAdjustment
            )
            if (existing != null) {
                repository.updateJournalEntry(entry)
            } else {
                repository.insertJournalEntry(entry)
            }
        }
    }

    // Notification Settings Actions
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setStudyRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setStudyRemindersEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setRevisionRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRevisionRemindersEnabled(enabled)
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setTaskRemindersEnabled(enabled)
        }
    }

    fun setShutdownRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setShutdownRemindersEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setRecoveryRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRecoveryRemindersEnabled(enabled)
        }
    }

    fun setWeeklyReviewEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setWeeklyReviewEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setVibrationEnabled(enabled) }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setQuietHoursEnabled(enabled) }
    }

    fun setQuietHoursRange(start: String, end: String) {
        viewModelScope.launch { preferences.setQuietHoursRange(start, end) }
    }

    fun setTaskReminderOffset(offset: String) {
        viewModelScope.launch { preferences.setTaskReminderOffset(offset) }
    }

    fun setBlockTimes(block1: String, block3: String, block5: String, shutdown: String) {
        viewModelScope.launch {
            preferences.setBlockTimes(block1, block3, block5, shutdown)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun triggerTestNotification() {
        com.example.notification.NotificationHelper.showTestNotification(getApplication())
    }
}

sealed class Screen(val title: String, val route: String) {
    data object Dashboard : Screen("Dashboard", "dashboard")
    data object Timeline : Screen("Timeline", "timeline")
    data object LetsStudy : Screen("Let's Study", "lets_study")
    data object Subjects : Screen("Subjects", "subjects")
    data object Revision : Screen("Revision Engine", "revision")
    data object Tasks : Screen("Task Manager", "tasks")
    data object StudySession : Screen("Study Tracker", "study_session")
    data object Journal : Screen("Evening Journal", "journal")
    data object BrainDump : Screen("Brain Dump", "brain_dump")
    data object Resources : Screen("Resource Vault", "resources")
    data object Analytics : Screen("Analytics & KPIs", "analytics")
    data object Scorecard : Screen("Discipline Scorecard", "scorecard")
    data object EmergencyRecovery : Screen("Emergency Recovery", "emergency_recovery")
    data object Settings : Screen("Settings & Backup", "settings")
}
