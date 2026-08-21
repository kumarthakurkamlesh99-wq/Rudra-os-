package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.model.*
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
    val permissionPromptShown: StateFlow<Boolean> = preferences.permissionPromptShown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Mission & Exam Countdown Flows
    val targetBoard: StateFlow<String> = repository.targetBoard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BSEB Class 12 Board 2027")
    val targetScore: StateFlow<String> = repository.targetScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "85%+")
    val boardExamDate: StateFlow<String> = repository.boardExamDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2027-02-15")
    val physicsExamDate: StateFlow<String> = repository.physicsExamDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2027-02-18")
    val chemistryExamDate: StateFlow<String> = repository.chemistryExamDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2027-02-22")
    val biologyExamDate: StateFlow<String> = repository.biologyExamDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2027-02-26")
    val weeklyChapterTarget: StateFlow<Int> = repository.weeklyChapterTarget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)
    val weeklyLectureTarget: StateFlow<Int> = repository.weeklyLectureTarget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)
    val weeklyMockTarget: StateFlow<Int> = repository.weeklyMockTarget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
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

    val allMockTests: StateFlow<List<MockTestEntity>> = repository.allMockTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStreaks: StateFlow<List<StreakRecordEntity>> = repository.allStreaks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueRevisions: StateFlow<List<RevisionLogEntity>> = repository.getDueRevisions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedRevisions: StateFlow<List<RevisionLogEntity>> = repository.completedRevisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScorecards: StateFlow<List<ScorecardEntity>> = repository.allScorecards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val last7DaysScorecards: StateFlow<List<ScorecardEntity>> = repository.last7DaysScorecards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val last30DaysScorecards: StateFlow<List<ScorecardEntity>> = repository.last30DaysScorecards
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
                    if (active != null) repository.getBlocksForPreset(active.id) else flowOf(emptyList())
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

    val allSessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayScorecard: StateFlow<ScorecardEntity?> = repository.getTodayScorecard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayJournal: StateFlow<JournalEntryEntity?> = repository.getTodayJournalEntry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unprocessedBrainDumps: StateFlow<List<BrainDumpEntity>> = repository.unprocessedBrainDumps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBrainDumps: StateFlow<List<BrainDumpEntity>> = repository.allBrainDumps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val letsStudyMode: StateFlow<String> = repository.letsStudyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "WEBVIEW")

    val isLowEnergyMode: StateFlow<Boolean> = repository.isLowEnergyMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastBackupDate: StateFlow<String> = repository.lastBackupDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Never")

    // AI Coach State
    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    private val _aiCoachAdvice = MutableStateFlow<String?>(null)
    val aiCoachAdvice: StateFlow<String?> = _aiCoachAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // 1. AI Test Generator States
    private val _generatedTest = MutableStateFlow<GeneratedTest?>(null)
    val generatedTest: StateFlow<GeneratedTest?> = _generatedTest.asStateFlow()

    private val _isTestGenerating = MutableStateFlow(false)
    val isTestGenerating: StateFlow<Boolean> = _isTestGenerating.asStateFlow()

    private val _isTestSubmitting = MutableStateFlow(false)
    val isTestSubmitting: StateFlow<Boolean> = _isTestSubmitting.asStateFlow()

    private val _testUserAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val testUserAnswers: StateFlow<Map<Int, String>> = _testUserAnswers.asStateFlow()

    // 2. AI Oral Viva States
    private val _vivaSession = MutableStateFlow<VivaSession?>(null)
    val vivaSession: StateFlow<VivaSession?> = _vivaSession.asStateFlow()

    private val _isVivaLoading = MutableStateFlow(false)
    val isVivaLoading: StateFlow<Boolean> = _isVivaLoading.asStateFlow()

    // 3. AI Multimodal Doubt Solver States
    private val _doubtResult = MutableStateFlow<AiDoubtResult?>(null)
    val doubtResult: StateFlow<AiDoubtResult?> = _doubtResult.asStateFlow()

    private val _selectedDoubtBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedDoubtBitmap: StateFlow<Bitmap?> = _selectedDoubtBitmap.asStateFlow()

    private val _isDoubtLoading = MutableStateFlow(false)
    val isDoubtLoading: StateFlow<Boolean> = _isDoubtLoading.asStateFlow()

    // 4. AI Weakness Analysis States
    private val _weaknessReport = MutableStateFlow<WeaknessReport?>(null)
    val weaknessReport: StateFlow<WeaknessReport?> = _weaknessReport.asStateFlow()

    private val _isWeaknessLoading = MutableStateFlow(false)
    val isWeaknessLoading: StateFlow<Boolean> = _isWeaknessLoading.asStateFlow()

    // 5. AI Board Prediction & Daily Plan States
    private val _boardPrediction = MutableStateFlow<AiBoardPrediction?>(null)
    val boardPrediction: StateFlow<AiBoardPrediction?> = _boardPrediction.asStateFlow()

    private val _aiDailyPlan = MutableStateFlow<AiDailyPlan?>(null)
    val aiDailyPlan: StateFlow<AiDailyPlan?> = _aiDailyPlan.asStateFlow()

    private val _isDailyPlanLoading = MutableStateFlow(false)
    val isDailyPlanLoading: StateFlow<Boolean> = _isDailyPlanLoading.asStateFlow()

    // Study Timer State
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _currentSessionSubject = MutableStateFlow("Physics")
    val timerSubject: StateFlow<String> = _currentSessionSubject.asStateFlow()

    private val _currentSessionChapter = MutableStateFlow("")
    val timerTopic: StateFlow<String> = _currentSessionChapter.asStateFlow()

    private val _currentSessionTargetMinutes = MutableStateFlow(45)
    val currentSessionTargetMinutes: StateFlow<Int> = _currentSessionTargetMinutes.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(subject: String, chapter: String = "", targetMinutes: Int = 45) {
        _currentSessionSubject.value = subject
        _currentSessionChapter.value = chapter
        _currentSessionTargetMinutes.value = targetMinutes
        _isTimerRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timerSeconds.value = 0
    }

    fun stopAndSaveTimer(notes: String = "") {
        val durationMinutes = (_timerSeconds.value / 60).coerceAtLeast(1)
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    subjectName = _currentSessionSubject.value,
                    topic = _currentSessionChapter.value.ifBlank { "Deep Study" },
                    startTimeMs = now - (durationMinutes * 60 * 1000L),
                    endTimeMs = now,
                    durationMinutes = durationMinutes,
                    dateString = repository.getTodayDateString(),
                    notes = notes
                )
            )
            resetTimer()
        }
    }

    // Chapter Actions
    fun updateChapter(chapter: ChapterEntity) {
        viewModelScope.launch { repository.updateChapter(chapter) }
    }

    fun incrementWatchedLecture(chapterId: Long) {
        viewModelScope.launch { repository.incrementWatchedLecture(chapterId) }
    }

    fun updateChapterStatus(chapterId: Long, status: String, progress: Int) {
        viewModelScope.launch { repository.updateChapterStatus(chapterId, status, progress) }
    }

    fun addChapterToTodayTasks(chapter: ChapterEntity, subjectName: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    subjectId = chapter.subjectId,
                    title = "Fix: ${chapter.title} ($subjectName)",
                    description = "Watch pending lectures / solve 10 PYQs / NCERT line-by-line",
                    priority = "High",
                    category = "Study",
                    dueDate = repository.getTodayDateString()
                )
            )
        }
    }

    // Task Actions
    fun insertTask(task: TaskEntity) {
        viewModelScope.launch { repository.insertTask(task) }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task.id) }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch { repository.updateTaskCompletion(task.id, !task.isCompleted) }
    }

    // Scorecard Actions
    fun saveScorecard(
        woke630: Boolean,
        block1: Boolean,
        block3: Boolean,
        fitness: Boolean,
        block5: Boolean,
        shutdown: Boolean,
        noPhone: Boolean,
        notes: String = "",
        isLowEnergy: Boolean = false
    ) {
        viewModelScope.launch {
            repository.saveOrUpdateTodayScorecard(
                wokeUpBy630 = woke630,
                completedBlock1 = block1,
                completedBlock3 = block3,
                completedFitness = fitness,
                completedBlock5 = block5,
                didShutdownRitual = shutdown,
                noPhoneBlocked = noPhone,
                notes = notes,
                isLowEnergyDay = isLowEnergy
            )
        }
    }

    fun updateTodayScorecard(
        woke630: Boolean,
        block1: Boolean,
        block3: Boolean,
        fitness: Boolean,
        block5: Boolean,
        shutdown: Boolean,
        noPhone: Boolean,
        notes: String = ""
    ) {
        saveScorecard(woke630, block1, block3, fitness, block5, shutdown, noPhone, notes)
    }

    // Journal Actions
    fun saveEveningJournal(
        mood: String,
        winsDone: String,
        missedWhat: String,
        tomorrowFocus: String,
        reflection: String
    ) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.insertJournalEntry(
                JournalEntryEntity(
                    dateString = todayStr,
                    mood = mood,
                    winsDone = winsDone,
                    missedWhat = missedWhat,
                    tomorrowFocusAndBlock1 = tomorrowFocus,
                    generalReflection = reflection,
                    isWeeklyReview = false
                )
            )
        }
    }

    fun saveSundayWeeklyReview(
        strongDay: String,
        weakDayAndTrigger: String,
        neglectedSubject: String,
        oneAdjustment: String
    ) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            repository.insertJournalEntry(
                JournalEntryEntity(
                    dateString = todayStr,
                    mood = "Weekly Review",
                    isWeeklyReview = true,
                    weeklyReviewStrongDay = strongDay,
                    weeklyReviewWeakDayAndTrigger = weakDayAndTrigger,
                    weeklyReviewNeglectedSubject = neglectedSubject,
                    weeklyReviewOneAdjustment = oneAdjustment
                )
            )
        }
    }

    // Brain Dump Actions
    fun saveBrainDump(content: String, category: String = BrainDumpEntity.CATEGORY_PARKING_LOT) {
        viewModelScope.launch {
            repository.insertBrainDump(
                BrainDumpEntity(
                    content = content,
                    category = category
                )
            )
        }
    }

    fun insertBrainDump(content: String, category: String = BrainDumpEntity.CATEGORY_PARKING_LOT) {
        saveBrainDump(content, category)
    }

    fun convertBrainDumpToTask(dump: BrainDumpEntity) {
        viewModelScope.launch {
            val taskId = repository.insertTask(
                TaskEntity(
                    title = dump.content,
                    category = "Personal",
                    priority = "Medium",
                    dueDate = repository.getTodayDateString()
                )
            )
            repository.updateBrainDump(dump.copy(isProcessed = true, convertedToTaskId = taskId))
        }
    }

    fun deleteBrainDump(dump: BrainDumpEntity) {
        viewModelScope.launch {
            repository.deleteBrainDump(dump.id)
        }
    }

    fun deleteBrainDump(id: Long) {
        viewModelScope.launch {
            repository.deleteBrainDump(id)
        }
    }

    // Resource Actions
    fun insertResource(resource: ResourceEntity) {
        viewModelScope.launch { repository.insertResource(resource) }
    }

    fun updateResource(resource: ResourceEntity) {
        viewModelScope.launch { repository.updateResource(resource) }
    }

    fun deleteResource(resource: ResourceEntity) {
        viewModelScope.launch { repository.deleteResource(resource.id) }
    }

    // Timeline Actions
    fun createTimelinePreset(name: String, description: String) {
        viewModelScope.launch {
            repository.insertPreset(
                TimelinePresetEntity(
                    name = name,
                    description = description
                )
            )
        }
    }

    fun activatePreset(presetId: Long) {
        viewModelScope.launch {
            repository.activatePreset(presetId)
        }
    }

    fun addTimelineBlock(block: TimelineBlockEntity) {
        viewModelScope.launch { repository.insertBlock(block) }
    }

    fun updateTimelineBlock(block: TimelineBlockEntity) {
        viewModelScope.launch { repository.updateBlock(block) }
    }

    fun deleteTimelineBlock(block: TimelineBlockEntity) {
        viewModelScope.launch { repository.deleteBlock(block.id) }
    }

    // Revision Actions
    fun scheduleNewRevision(
        chapterId: Long,
        subjectName: String,
        chapterTitle: String,
        intervalLabel: String,
        daysToAdd: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.scheduleNewRevision(chapterId, subjectName, chapterTitle, intervalLabel, daysToAdd, notes)
        }
    }

    fun markRevisionDone(logId: Long, chapterId: Long, currentInterval: String) {
        viewModelScope.launch {
            repository.markRevisionCompleted(logId, chapterId, currentInterval)
        }
    }

    fun markRevisionCompleted(revision: RevisionLogEntity, confidenceScore: Int, notes: String) {
        viewModelScope.launch {
            repository.markRevisionCompleted(revision.id, revision.chapterId, revision.intervalLabel)
        }
    }

    // Mock Tests Actions
    fun addMockTest(subject: String, chapter: String, testName: String, marks: Double, totalMarks: Double, date: String, notes: String) {
        viewModelScope.launch {
            repository.insertMockTest(
                MockTestEntity(
                    subject = subject,
                    chapter = chapter,
                    testName = testName,
                    marksObtained = marks,
                    totalMarks = totalMarks,
                    testDate = date,
                    notes = notes
                )
            )
        }
    }

    fun deleteMockTest(id: Long) {
        viewModelScope.launch { repository.deleteMockTest(id) }
    }

    // Streaks Actions
    fun toggleStreakCheckIn(streakKey: String) {
        viewModelScope.launch { repository.toggleStreakCheckIn(streakKey) }
    }

    // Mission Goals Actions
    fun saveMissionGoals(board: String, score: String, boardDate: String, phyDate: String, chemDate: String, bioDate: String) {
        viewModelScope.launch {
            repository.setMissionGoals(board, score, boardDate, phyDate, chemDate, bioDate)
        }
    }

    fun saveWeeklyTargets(chapters: Int, lectures: Int, mocks: Int) {
        viewModelScope.launch {
            repository.setWeeklyTargets(chapters, lectures, mocks)
        }
    }

    // AI Features
    fun requestAiSummary(subject: String, chapterTitle: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResult.value = null
            val result = geminiService.generateChapterSummary(subject, chapterTitle)
            _isAiLoading.value = false
            _aiResult.value = result.getOrElse { it.localizedMessage ?: "Failed to generate summary." }
        }
    }

    fun requestAiQuiz(subject: String, chapterTitle: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResult.value = null
            val result = geminiService.generatePracticeQuiz(subject, chapterTitle)
            _isAiLoading.value = false
            _aiResult.value = result.getOrElse { it.localizedMessage ?: "Failed to generate quiz." }
        }
    }

    fun requestAiFlashcards(subject: String, chapterTitle: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResult.value = null
            val result = geminiService.generateFlashcards(subject, chapterTitle)
            _isAiLoading.value = false
            _aiResult.value = result.getOrElse { it.localizedMessage ?: "Failed to generate flashcards." }
        }
    }

    fun requestAiDoubtSolution(subject: String, doubtText: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResult.value = null
            val result = geminiService.solveDoubt(subject, doubtText)
            _isAiLoading.value = false
            _aiResult.value = result.getOrElse { it.localizedMessage ?: "Failed to solve doubt." }
        }
    }

    fun requestAiStudyCoachAdvice() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiCoachAdvice.value = null

            val chapters = allChapters.value
            val weak = chapters.filter { it.isWeak }.take(3).map { it.title }
            val revisions = dueRevisions.value.take(2).map { it.chapterTitle }
            val mockScores = allMockTests.value.take(3).map { "${it.subject}: ${it.percentage.toInt()}%" }

            val prompt = """
                You are Rudra's personal AI Study Coach for Class 12 Science PCB (Board 2027, Target 85%+).
                Current Status:
                - Weak Chapters: ${if (weak.isEmpty()) "None marked" else weak.joinToString(", ")}
                - Due Revisions Today: ${if (revisions.isEmpty()) "All caught up" else revisions.joinToString(", ")}
                - Recent Mock Scores: ${if (mockScores.isEmpty()) "No recent mocks" else mockScores.joinToString(", ")}
                - Total Chapters Completed: ${chapters.count { it.status == ChapterEntity.STATUS_COMPLETED }} / 46
                
                Please provide a focused daily coaching blueprint:
                1. 🎯 Top 3 High-Impact Priority Tasks for Today (Specific Chapter + Activity: e.g. NCERT reading, Lecture 4-6, or 15 PYQs)
                2. 🔄 Urgent Revision Target (Spaced repetition warning)
                3. 🧪 Mock Test Recommendation (Which subject/chapter to test this weekend)
                4. ⚡ High-Performance Mindset Quote / Strict Discipline Advice.
                Keep it concise, direct, inspiring, and strictly tailored to Class 12 PCB.
            """.trimIndent()

            val result = geminiService.generateChapterSummary("PCB Board Preparation", prompt)
            _isAiLoading.value = false
            _aiCoachAdvice.value = result.getOrElse {
                """
                🎯 Daily Study Blueprint (Offline Mode):
                1. Physics Block: Focus on Electric Charges & Fields - Solve 10 PYQs + NCERT Examples.
                2. Chemistry Block: Revise Electrochemistry Nernst Equation formulas + 5 numericals.
                3. Biology Block: Complete Sexual Reproduction in Flowering Plants line-by-line NCERT diagram practice.
                
                🔄 Urgent Revision: Review yesterday's notes for 20 mins before starting new topics.
                ⚡ Mindset: "Consistency beats intensity. Protect your study blocks with zero phone scroll."
                """.trimIndent()
            }
        }
    }

    // ==========================================
    // 1. AI TEST GENERATOR & EXAM ENGINE
    // ==========================================
    fun generateTestPaper(
        subject: String,
        chapters: List<String>,
        mode: String,
        difficulty: String,
        questionTypes: List<QuestionType>,
        questionCount: Int = 5
    ) {
        viewModelScope.launch {
            _isTestGenerating.value = true
            _testUserAnswers.value = emptyMap()
            val result = geminiService.generateTestPaper(
                subject = subject,
                chapters = chapters,
                mode = mode,
                difficulty = difficulty,
                questionTypes = questionTypes,
                questionCount = questionCount
            )
            _isTestGenerating.value = false
            _generatedTest.value = result.getOrNull()
        }
    }

    fun updateTestAnswer(questionId: Int, answer: String) {
        val current = _testUserAnswers.value.toMutableMap()
        current[questionId] = answer
        _testUserAnswers.value = current
    }

    fun submitTestPaper() {
        val test = _generatedTest.value ?: return
        viewModelScope.launch {
            _isTestSubmitting.value = true
            val evaluated = geminiService.evaluateTestSubmission(test, _testUserAnswers.value)
            _isTestSubmitting.value = false
            _generatedTest.value = evaluated.getOrNull() ?: test
        }
    }

    fun saveTestResultToMockHistory() {
        val test = _generatedTest.value ?: return
        viewModelScope.launch {
            repository.insertMockTest(
                MockTestEntity(
                    subject = test.subject,
                    chapter = test.chapters.joinToString(", ").ifBlank { test.mode },
                    testName = test.title,
                    marksObtained = test.totalObtainedMarks,
                    totalMarks = test.totalMarks,
                    testDate = repository.getTodayDateString(),
                    notes = "AI Test (${test.difficulty}). ${test.feedback}"
                )
            )
        }
    }

    fun clearActiveTest() {
        _generatedTest.value = null
        _testUserAnswers.value = emptyMap()
    }

    // ==========================================
    // 2. AI ORAL VIVA MODE
    // ==========================================
    fun startVivaSession(subject: String, chapter: String) {
        viewModelScope.launch {
            _isVivaLoading.value = true
            val firstQ = geminiService.generateVivaQuestion(subject, chapter, 1, emptyList())
            _isVivaLoading.value = false
            if (firstQ.isSuccess) {
                val q = firstQ.getOrNull()!!
                _vivaSession.value = VivaSession(
                    subject = subject,
                    chapter = chapter,
                    questions = listOf(q),
                    currentQuestionIndex = 0,
                    maxScore = 5
                )
            }
        }
    }

    fun submitVivaAnswer(userResponse: String) {
        val session = _vivaSession.value ?: return
        val currentQ = session.questions.getOrNull(session.currentQuestionIndex) ?: return
        viewModelScope.launch {
            _isVivaLoading.value = true
            val evaluatedQ = geminiService.evaluateVivaAnswer(currentQ, userResponse)
            _isVivaLoading.value = false
            val eval = evaluatedQ.getOrElse { currentQ.copy(userResponse = userResponse, isAnswered = true, correctnessScore = 3) }

            val updatedQuestions = session.questions.toMutableList()
            updatedQuestions[session.currentQuestionIndex] = eval
            val newScore = updatedQuestions.sumOf { it.correctnessScore }

            _vivaSession.value = session.copy(
                questions = updatedQuestions,
                overallScore = newScore
            )
        }
    }

    fun nextVivaQuestion() {
        val session = _vivaSession.value ?: return
        val nextIdx = session.currentQuestionIndex + 1
        if (nextIdx >= 5) {
            // Completed 5 questions viva
            val totalScore = session.questions.sumOf { it.correctnessScore }
            _vivaSession.value = session.copy(
                isCompleted = true,
                overallScore = totalScore,
                maxScore = session.questions.size * 5,
                overallFeedback = if (totalScore >= 20) "Outstanding viva! Board Topper Standard." else "Good effort! Revise missing technical terms before practicals."
            )
            return
        }

        viewModelScope.launch {
            _isVivaLoading.value = true
            val asked = session.questions.map { it.question }
            val nextQ = geminiService.generateVivaQuestion(session.subject, session.chapter, nextIdx + 1, asked)
            _isVivaLoading.value = false
            if (nextQ.isSuccess) {
                val q = nextQ.getOrNull()!!
                val updatedQuestions = session.questions.toMutableList().apply { add(q) }
                _vivaSession.value = session.copy(
                    questions = updatedQuestions,
                    currentQuestionIndex = nextIdx,
                    maxScore = updatedQuestions.size * 5
                )
            }
        }
    }

    fun resetVivaSession() {
        _vivaSession.value = null
    }

    // ==========================================
    // 3. AI MULTIMODAL DOUBT SOLVER
    // ==========================================
    fun setSelectedDoubtBitmap(bitmap: Bitmap?) {
        _selectedDoubtBitmap.value = bitmap
    }

    fun solveDoubt(subject: String, doubtText: String) {
        viewModelScope.launch {
            _isDoubtLoading.value = true
            _doubtResult.value = null
            val result = geminiService.solveDoubtWithImage(subject, doubtText, _selectedDoubtBitmap.value)
            _isDoubtLoading.value = false
            _doubtResult.value = result.getOrNull()
        }
    }

    fun clearDoubtResult() {
        _doubtResult.value = null
        _selectedDoubtBitmap.value = null
    }

    // ==========================================
    // 4. AI WEAKNESS ANALYSIS & ADAPTIVE ENGINE
    // ==========================================
    fun generateWeaknessAnalysis() {
        viewModelScope.launch {
            _isWeaknessLoading.value = true
            val chapters = allChapters.value
            val weak = chapters.filter { it.isWeak }.map { it.title }
            val strong = chapters.filter { it.status == ChapterEntity.STATUS_COMPLETED && it.confidenceRating >= 4 }.map { it.title }
            val mocks = allMockTests.value
            val mockAvg = if (mocks.isNotEmpty()) mocks.map { it.percentage }.average() else 72.0
            val revCount = completedRevisions.value.size

            val report = geminiService.generateWeaknessAnalysis(weak, strong, mockAvg, revCount)
            _isWeaknessLoading.value = false
            _weaknessReport.value = report.getOrNull()
        }
    }

    fun generateAdaptiveRemediationTest() {
        val report = _weaknessReport.value
        val weakChapters = report?.recommendedChapters ?: emptyList()
        val subject = if (weakChapters.any { it.contains("Physics", true) || it.contains("Electric", true) }) "Physics" else "Chemistry"
        val difficulty = if (report?.adaptiveTier?.contains("Level 3") == true) "Hard" else if (report?.adaptiveTier?.contains("Level 1") == true) "Easy" else "Board Level"

        generateTestPaper(
            subject = subject,
            chapters = weakChapters,
            mode = "Adaptive Remediation Test",
            difficulty = difficulty,
            questionTypes = listOf(QuestionType.MCQ, QuestionType.ASSERTION_REASON, QuestionType.SHORT_ANSWER, QuestionType.NUMERICAL),
            questionCount = 5
        )
    }

    // ==========================================
    // 5. AI BOARD PREDICTOR & DAILY PLANNER
    // ==========================================
    fun generateAiDailyPlan() {
        viewModelScope.launch {
            _isDailyPlanLoading.value = true
            _aiDailyPlan.value = null

            val chapters = allChapters.value
            val weak = chapters.filter { it.isWeak }.take(3).map { it.title }
            val revisions = dueRevisions.value.take(3).map { it.chapterTitle }
            val completed = chapters.count { it.status == ChapterEntity.STATUS_COMPLETED }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = try { sdf.parse(boardExamDate.value) } catch (e: Exception) { null }
            val remainingDays = if (targetDate != null) {
                val diff = targetDate.time - System.currentTimeMillis()
                (diff / (1000 * 60 * 60 * 24L)).coerceAtLeast(0).toInt()
            } else {
                300
            }

            val plan = geminiService.generateAiDailyPlan(
                remainingDays = remainingDays,
                weakChapters = weak,
                dueRevisions = revisions,
                completedCount = completed,
                totalChapters = 46
            )
            _isDailyPlanLoading.value = false
            _aiDailyPlan.value = plan.getOrNull()
        }
    }

    fun addDailyPlanTasksToTaskManager() {
        val plan = _aiDailyPlan.value ?: return
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            plan.todayChapters.forEach { ch ->
                repository.insertTask(
                    TaskEntity(
                        title = ch,
                        category = "Study",
                        priority = "High",
                        dueDate = todayStr
                    )
                )
            }
            plan.revisionTasks.forEach { rev ->
                repository.insertTask(
                    TaskEntity(
                        title = rev,
                        category = "Revision",
                        priority = "Medium",
                        dueDate = todayStr
                    )
                )
            }
            if (plan.mockTestTask.isNotBlank()) {
                repository.insertTask(
                    TaskEntity(
                        title = plan.mockTestTask,
                        category = "Mock Test",
                        priority = "High",
                        dueDate = todayStr
                    )
                )
            }
        }
    }

    fun refreshBoardPrediction() {
        viewModelScope.launch {
            val chapters = allChapters.value
            val completed = chapters.count { it.status == ChapterEntity.STATUS_COMPLETED }
            val revisions = completedRevisions.value.size
            val mocks = allMockTests.value
            val mockAvg = if (mocks.isNotEmpty()) mocks.map { it.percentage }.average() else 72.0

            _boardPrediction.value = geminiService.predictBoardReadiness(
                completedChapters = completed,
                totalChapters = 46,
                completedRevisions = revisions,
                mockAverage = mockAvg
            )
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }

    // App Preferences Actions
    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setLetsStudyMode(mode: String) {
        viewModelScope.launch { repository.setLetsStudyMode(mode) }
    }

    fun toggleLowEnergyMode() {
        viewModelScope.launch {
            repository.setLowEnergyMode(!isLowEnergyMode.value)
        }
    }

    fun setPermissionPromptShown(shown: Boolean) {
        viewModelScope.launch { preferences.setPermissionPromptShown(shown) }
    }

    fun openPwBatchInBrowser() {
        val url = "https://www.physicswallah.live/study/batches/my-batches"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    // Notification Actions
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
            if (enabled) {
                com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
            } else {
                com.example.notification.RudraAlarmScheduler.cancelAllRoutineAlarms(getApplication())
            }
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
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setTaskRemindersEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setShutdownRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setShutdownRemindersEnabled(enabled)
            com.example.notification.RudraAlarmScheduler.rescheduleAllRoutineAlarms(getApplication(), preferences)
        }
    }

    fun setRecoveryRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setRecoveryRemindersEnabled(enabled) }
    }

    fun setWeeklyReviewEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setWeeklyReviewEnabled(enabled) }
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
    data object Subjects : Screen("PCB Syllabus", "subjects")
    data object WeakChapters : Screen("Weak Chapters", "weak_chapters")
    data object MockTests : Screen("Mock Tests", "mock_tests")
    data object LectureTracker : Screen("Lecture Tracker", "lecture_tracker")
    data object Revision : Screen("Revision Engine", "revision")
    data object AiCoach : Screen("AI Study Coach", "ai_coach")
    data object MissionBoard : Screen("Mission Board", "mission_board")
    data object Streaks : Screen("Streak Tracker", "streaks")
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
