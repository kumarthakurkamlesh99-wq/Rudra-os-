package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PcbDatabaseSeeder
import com.example.data.local.entities.*
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RudraRepository(
    private val database: AppDatabase,
    private val preferences: UserPreferences
) {
    companion object {
        private const val TAG = "RudraRepository"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Database error state flow for notifying the UI gracefully
    private val _databaseErrorMessage = MutableStateFlow<String?>(null)
    val databaseErrorMessage: StateFlow<String?> = _databaseErrorMessage.asStateFlow()

    fun clearDatabaseError() {
        _databaseErrorMessage.value = null
    }

    private fun logAndSetError(operation: String, e: Throwable) {
        Log.e(TAG, "Database operation failed during [$operation]: ${e.localizedMessage}", e)
        _databaseErrorMessage.value = "Database action error: ${e.localizedMessage ?: "Operation failed safely"}"
    }

    fun getTodayDateString(): String = dateFormat.format(Date())

    suspend fun seedDatabaseIfEmpty() {
        try {
            PcbDatabaseSeeder.seedDatabaseIfEmpty(database)
        } catch (e: Throwable) {
            logAndSetError("seedDatabaseIfEmpty", e)
        }
    }

    // --- Preferences ---
    val themeMode: Flow<String> = preferences.themeMode
    val letsStudyMode: Flow<String> = preferences.letsStudyMode
    val isLowEnergyMode: Flow<Boolean> = preferences.isLowEnergyMode
    val lastBackupDate: Flow<String> = preferences.lastBackupDate

    // Exam Goals & Countdown Preferences
    val targetBoard: Flow<String> = preferences.targetBoard
    val targetScore: Flow<String> = preferences.targetScore
    val boardExamDate: Flow<String> = preferences.boardExamDate
    val physicsExamDate: Flow<String> = preferences.physicsExamDate
    val chemistryExamDate: Flow<String> = preferences.chemistryExamDate
    val biologyExamDate: Flow<String> = preferences.biologyExamDate
    val weeklyChapterTarget: Flow<Int> = preferences.weeklyChapterTarget
    val weeklyLectureTarget: Flow<Int> = preferences.weeklyLectureTarget
    val weeklyMockTarget: Flow<Int> = preferences.weeklyMockTarget

    suspend fun setThemeMode(mode: String) = preferences.setThemeMode(mode)
    suspend fun setLetsStudyMode(mode: String) = preferences.setLetsStudyMode(mode)
    suspend fun setLowEnergyMode(enabled: Boolean) = preferences.setLowEnergyMode(enabled)
    suspend fun setLastBackupDate(date: String) = preferences.setLastBackupDate(date)

    suspend fun setMissionGoals(board: String, score: String, boardDate: String, phyDate: String, chemDate: String, bioDate: String) {
        preferences.setMissionGoals(board, score, boardDate, phyDate, chemDate, bioDate)
    }

    suspend fun setWeeklyTargets(chapters: Int, lectures: Int, mocks: Int) {
        preferences.setWeeklyTargets(chapters, lectures, mocks)
    }

    // --- Subjects & Chapters ---
    val allSubjects: Flow<List<SubjectEntity>> = database.subjectDao().getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = database.chapterDao().getAllChapters()
    val weakChapters: Flow<List<ChapterEntity>> = database.chapterDao().getWeakChapters()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        database.chapterDao().getChaptersBySubject(subjectId)

    /**
     * Inserts a Subject safely by verifying unique constraints and handling duplicate codes.
     */
    suspend fun insertSubject(subject: SubjectEntity): Long = withContext(Dispatchers.IO) {
        try {
            val safeName = subject.name.trim().ifBlank { "Subject" }
            val safeCode = subject.code.trim().ifBlank {
                safeName.take(4).uppercase()
            }
            // Check if subject with this code already exists
            val existing = database.subjectDao().getSubjectByCodeSync(safeCode)
            if (existing != null) {
                // Update existing subject without triggering constraint or cascade violations
                val updated = existing.copy(
                    name = safeName,
                    colorHex = subject.colorHex.ifBlank { existing.colorHex },
                    iconName = subject.iconName.ifBlank { existing.iconName },
                    description = subject.description.ifBlank { existing.description }
                )
                database.subjectDao().updateSubject(updated)
                Log.d(TAG, "Subject with code [$safeCode] already existed; updated id=${existing.id}")
                return@withContext existing.id
            }
            val sanitized = subject.copy(name = safeName, code = safeCode)
            database.subjectDao().insertSubject(sanitized)
        } catch (e: Throwable) {
            logAndSetError("insertSubject", e)
            -1L
        }
    }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        try {
            database.subjectDao().updateSubject(subject)
        } catch (e: Throwable) {
            logAndSetError("updateSubject", e)
        }
    }

    suspend fun deleteSubject(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.subjectDao().deleteSubjectById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteSubject", e)
        }
    }

    /**
     * Inserts a Chapter with parent Subject foreign key validation and fallback.
     */
    suspend fun insertChapter(chapter: ChapterEntity): Long = withContext(Dispatchers.IO) {
        try {
            var targetSubjectId = chapter.subjectId
            // Validate subject existence
            if (database.subjectDao().countSubjectById(targetSubjectId) == 0) {
                Log.w(TAG, "SubjectId $targetSubjectId does not exist for chapter [${chapter.title}]. Searching fallback.")
                val firstSubjectId = database.subjectDao().getFirstSubjectIdSync()
                if (firstSubjectId != null) {
                    targetSubjectId = firstSubjectId
                } else {
                    // Create default subject if none exist
                    targetSubjectId = database.subjectDao().insertSubject(
                        SubjectEntity(
                            name = "General",
                            code = "GEN",
                            colorHex = "#1E88E5",
                            iconName = "menu_book",
                            description = "General Syllabus"
                        )
                    )
                }
            }
            val safeChapter = chapter.copy(subjectId = targetSubjectId)
            database.chapterDao().insertChapter(safeChapter)
        } catch (e: Throwable) {
            logAndSetError("insertChapter", e)
            -1L
        }
    }

    suspend fun updateChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        try {
            database.chapterDao().updateChapter(chapter)
        } catch (e: Throwable) {
            logAndSetError("updateChapter", e)
        }
    }

    suspend fun updateChapterStatus(id: Long, status: String, progress: Int) = withContext(Dispatchers.IO) {
        try {
            database.chapterDao().updateChapterStatus(id, status, progress)
        } catch (e: Throwable) {
            logAndSetError("updateChapterStatus", e)
        }
    }

    suspend fun updateChapterLectures(id: Long, watched: Int, total: Int) = withContext(Dispatchers.IO) {
        try {
            database.chapterDao().updateLectures(id, watched, total)
        } catch (e: Throwable) {
            logAndSetError("updateChapterLectures", e)
        }
    }

    suspend fun incrementWatchedLecture(chapterId: Long) = withContext(Dispatchers.IO) {
        try {
            val chapter = database.chapterDao().getChapterById(chapterId) ?: return@withContext
            val newWatched = (chapter.watchedLectures + 1).coerceAtMost(chapter.totalLectures)
            val newProgress = if (chapter.totalLectures > 0) ((newWatched.toFloat() / chapter.totalLectures) * 100).toInt() else chapter.progressPercent
            val newStatus = if (newWatched >= chapter.totalLectures && chapter.totalLectures > 0) ChapterEntity.STATUS_COMPLETED else ChapterEntity.STATUS_LEARNING
            database.chapterDao().updateChapter(
                chapter.copy(
                    watchedLectures = newWatched,
                    progressPercent = newProgress,
                    status = if (chapter.status == ChapterEntity.STATUS_NOT_STARTED) newStatus else chapter.status,
                    lastStudiedDate = getTodayDateString()
                )
            )
        } catch (e: Throwable) {
            logAndSetError("incrementWatchedLecture", e)
        }
    }

    suspend fun deleteChapter(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.chapterDao().deleteChapterById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteChapter", e)
        }
    }

    // --- Mock Tests ---
    val allMockTests: Flow<List<MockTestEntity>> = database.mockTestDao().getAllMockTests()
    fun getMockTestsBySubject(subject: String): Flow<List<MockTestEntity>> = database.mockTestDao().getMockTestsBySubject(subject)

    suspend fun insertMockTest(test: MockTestEntity): Long = withContext(Dispatchers.IO) {
        try {
            database.mockTestDao().insertMockTest(test)
        } catch (e: Throwable) {
            logAndSetError("insertMockTest", e)
            -1L
        }
    }

    suspend fun deleteMockTest(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.mockTestDao().deleteMockTestById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteMockTest", e)
        }
    }

    // --- Streaks System ---
    val allStreaks: Flow<List<StreakRecordEntity>> = database.streakDao().getAllStreaks()

    suspend fun toggleStreakCheckIn(streakKey: String) = withContext(Dispatchers.IO) {
        try {
            val todayStr = getTodayDateString()
            val existing = database.streakDao().getStreakByKey(streakKey) ?: return@withContext
            val historyList = existing.historyLog.split(",").filter { it.isNotBlank() }.toMutableList()

            if (historyList.contains(todayStr)) {
                historyList.remove(todayStr)
                val newCurrent = (existing.currentStreak - 1).coerceAtLeast(0)
                database.streakDao().updateStreak(
                    existing.copy(
                        currentStreak = newCurrent,
                        historyLog = historyList.joinToString(",")
                    )
                )
            } else {
                historyList.add(todayStr)
                val newCurrent = existing.currentStreak + 1
                val newBest = maxOf(existing.bestStreak, newCurrent)
                database.streakDao().updateStreak(
                    existing.copy(
                        currentStreak = newCurrent,
                        bestStreak = newBest,
                        lastActiveDate = todayStr,
                        historyLog = historyList.joinToString(",")
                    )
                )
            }
        } catch (e: Throwable) {
            logAndSetError("toggleStreakCheckIn", e)
        }
    }

    // --- Spaced Repetition Engine ---
    val allRevisionLogs: Flow<List<RevisionLogEntity>> = database.revisionLogDao().getAllRevisionLogs()
    val completedRevisions: Flow<List<RevisionLogEntity>> = database.revisionLogDao().getCompletedRevisions()

    fun getDueRevisions(): Flow<List<RevisionLogEntity>> =
        database.revisionLogDao().getDueRevisions(getTodayDateString())

    fun getRevisionsForDate(dateStr: String): Flow<List<RevisionLogEntity>> =
        database.revisionLogDao().getRevisionsByDate(dateStr)

    suspend fun markRevisionCompleted(logId: Long, chapterId: Long, currentInterval: String) = withContext(Dispatchers.IO) {
        try {
            val todayStr = getTodayDateString()
            database.revisionLogDao().markCompleted(logId, todayStr)

            val nextIntervalInfo = getNextRevisionInterval(currentInterval)
            if (nextIntervalInfo != null) {
                val (nextLabel, daysToAdd) = nextIntervalInfo
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                val nextDueDateStr = dateFormat.format(cal.time)

                val chapter = database.chapterDao().getChapterById(chapterId)
                val subjectName = chapter?.title ?: "Subject"

                database.revisionLogDao().insertRevisionLog(
                    RevisionLogEntity(
                        chapterId = chapterId,
                        subjectName = subjectName,
                        chapterTitle = chapter?.title ?: "Chapter",
                        scheduledDate = nextDueDateStr,
                        intervalLabel = nextLabel,
                        notes = "Scheduled after completing $currentInterval"
                    )
                )

                database.chapterDao().recordChapterRevision(chapterId, todayStr, nextDueDateStr)
            } else {
                database.chapterDao().recordChapterRevision(chapterId, todayStr, null)
            }
        } catch (e: Throwable) {
            logAndSetError("markRevisionCompleted", e)
        }
    }

    suspend fun scheduleNewRevision(
        chapterId: Long,
        subjectName: String,
        chapterTitle: String,
        intervalLabel: String,
        daysToAdd: Int,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        try {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val dateStr = dateFormat.format(cal.time)

            database.revisionLogDao().insertRevisionLog(
                RevisionLogEntity(
                    chapterId = chapterId,
                    subjectName = subjectName,
                    chapterTitle = chapterTitle,
                    scheduledDate = dateStr,
                    intervalLabel = intervalLabel,
                    notes = notes
                )
            )
        } catch (e: Throwable) {
            logAndSetError("scheduleNewRevision", e)
            -1L
        }
    }

    private fun getNextRevisionInterval(current: String): Pair<String, Int>? {
        return when (current) {
            "Revision 1 (Same Day)" -> Pair("Revision 2 (+7 Days)", 7)
            "Revision 2 (+7 Days)" -> Pair("Revision 3 (+30 Days)", 23)
            "Same Day" -> Pair("+7 Days", 7)
            "+1 Day" -> Pair("+7 Days", 6)
            "+7 Days" -> Pair("+30 Days", 23)
            "+15 Days" -> Pair("+30 Days", 15)
            else -> null
        }
    }

    // --- Timeline Presets & Blocks ---
    val allPresets: Flow<List<TimelinePresetEntity>> = database.timelineDao().getAllPresets()
    val activePreset: Flow<TimelinePresetEntity?> = database.timelineDao().getActivePreset()

    fun getBlocksForPreset(presetId: Long): Flow<List<TimelineBlockEntity>> =
        database.timelineDao().getBlocksForPreset(presetId)

    suspend fun activatePreset(presetId: Long) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().deactivateAllPresets()
            database.timelineDao().activatePreset(presetId)
        } catch (e: Throwable) {
            logAndSetError("activatePreset", e)
        }
    }

    suspend fun insertPreset(preset: TimelinePresetEntity): Long = withContext(Dispatchers.IO) {
        try {
            val safeName = preset.name.trim().ifBlank { "Daily Routine" }
            database.timelineDao().insertPreset(preset.copy(name = safeName))
        } catch (e: Throwable) {
            logAndSetError("insertPreset", e)
            -1L
        }
    }

    suspend fun updatePreset(preset: TimelinePresetEntity) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().updatePreset(preset)
        } catch (e: Throwable) {
            logAndSetError("updatePreset", e)
        }
    }

    suspend fun deletePreset(presetId: Long) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().deletePresetById(presetId)
        } catch (e: Throwable) {
            logAndSetError("deletePreset", e)
        }
    }

    /**
     * Inserts a Timeline Block safely.
     * Checks if presetId exists in timeline_presets; if missing, falls back to active preset or creates one.
     */
    suspend fun insertBlock(block: TimelineBlockEntity): Long = withContext(Dispatchers.IO) {
        try {
            var targetPresetId = block.presetId

            // Check if foreign key parent preset exists
            if (database.timelineDao().countPresetById(targetPresetId) == 0) {
                Log.w(TAG, "PresetId $targetPresetId does not exist in timeline_presets! Finding fallback parent.")
                val activeId = database.timelineDao().getActivePresetIdSync()
                val firstId = database.timelineDao().getFirstPresetIdSync()

                targetPresetId = when {
                    activeId != null -> activeId
                    firstId != null -> firstId
                    else -> {
                        // Create default preset if table is completely empty
                        Log.i(TAG, "No presets exist; creating default Routine preset.")
                        database.timelineDao().insertPreset(
                            TimelinePresetEntity(
                                name = "Daily Routine",
                                isActive = true,
                                description = "Default daily timetable preset"
                            )
                        )
                    }
                }
            }

            val safeBlock = block.copy(
                presetId = targetPresetId,
                title = block.title.trim().ifBlank { "Study Block" },
                startTime = block.startTime.trim().ifBlank { "06:00" },
                endTime = block.endTime.trim().ifBlank { "08:00" },
                category = block.category.trim().ifBlank { "Study" }
            )
            database.timelineDao().insertBlock(safeBlock)
        } catch (e: Throwable) {
            logAndSetError("insertBlock", e)
            -1L
        }
    }

    suspend fun updateBlock(block: TimelineBlockEntity) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().updateBlock(block)
        } catch (e: Throwable) {
            logAndSetError("updateBlock", e)
        }
    }

    suspend fun updateBlockCompletion(blockId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().updateBlockCompletion(blockId, isCompleted)
        } catch (e: Throwable) {
            logAndSetError("updateBlockCompletion", e)
        }
    }

    suspend fun resetPresetBlockCompletions(presetId: Long) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().resetPresetBlockCompletions(presetId)
        } catch (e: Throwable) {
            logAndSetError("resetPresetBlockCompletions", e)
        }
    }

    suspend fun deleteBlock(blockId: Long) = withContext(Dispatchers.IO) {
        try {
            database.timelineDao().deleteBlockById(blockId)
        } catch (e: Throwable) {
            logAndSetError("deleteBlock", e)
        }
    }

    // --- Tasks ---
    val activeTasks: Flow<List<TaskEntity>> = database.taskDao().getActiveTasks()
    val pendingTasks: Flow<List<TaskEntity>> = database.taskDao().getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = database.taskDao().getCompletedTasks()

    fun getOverdueTasks(): Flow<List<TaskEntity>> = database.taskDao().getOverdueTasks(getTodayDateString())

    /**
     * Inserts a Task with foreign key subject validation and fallback.
     */
    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        try {
            var safeSubjectId = task.subjectId
            if (safeSubjectId != null && database.subjectDao().countSubjectById(safeSubjectId) == 0) {
                Log.w(TAG, "Task subjectId $safeSubjectId not found in subjects table. Setting to null.")
                safeSubjectId = null
            }
            val safeTask = task.copy(
                subjectId = safeSubjectId,
                title = task.title.trim().ifBlank { "New Task" },
                category = task.category.trim().ifBlank { "General" }
            )
            database.taskDao().insertTask(safeTask)
        } catch (e: Throwable) {
            logAndSetError("insertTask", e)
            -1L
        }
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        try {
            database.taskDao().updateTask(task)
        } catch (e: Throwable) {
            logAndSetError("updateTask", e)
        }
    }

    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        try {
            database.taskDao().updateTaskCompletion(id, isCompleted)
        } catch (e: Throwable) {
            logAndSetError("updateTaskCompletion", e)
        }
    }

    suspend fun archiveCompletedTasks() = withContext(Dispatchers.IO) {
        try {
            database.taskDao().archiveCompletedTasks()
        } catch (e: Throwable) {
            logAndSetError("archiveCompletedTasks", e)
        }
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.taskDao().deleteTaskById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteTask", e)
        }
    }

    // --- Study Sessions ---
    val allSessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllSessions()

    fun getSessionsForDate(dateStr: String): Flow<List<StudySessionEntity>> =
        database.studySessionDao().getSessionsForDate(dateStr)

    fun getTotalMinutesForDate(dateStr: String): Flow<Int?> =
        database.studySessionDao().getTotalMinutesForDate(dateStr)

    /**
     * Inserts a Study Session safely with subjectId validation.
     */
    suspend fun insertSession(session: StudySessionEntity): Long = withContext(Dispatchers.IO) {
        try {
            var safeSubjectId = session.subjectId
            if (safeSubjectId != null && database.subjectDao().countSubjectById(safeSubjectId) == 0) {
                Log.w(TAG, "StudySession subjectId $safeSubjectId not found in subjects table. Setting to null.")
                safeSubjectId = null
            }
            val safeSession = session.copy(
                subjectId = safeSubjectId,
                subjectName = session.subjectName.trim().ifBlank { "Study" },
                topic = session.topic.trim().ifBlank { "Deep Work" }
            )
            database.studySessionDao().insertSession(safeSession)
        } catch (e: Throwable) {
            logAndSetError("insertSession", e)
            -1L
        }
    }

    suspend fun deleteSession(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.studySessionDao().deleteSessionById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteSession", e)
        }
    }

    // --- Scorecards ---
    val allScorecards: Flow<List<ScorecardEntity>> = database.scorecardDao().getAllScorecards()
    val last7DaysScorecards: Flow<List<ScorecardEntity>> = database.scorecardDao().getLast7DaysScorecards()
    val last30DaysScorecards: Flow<List<ScorecardEntity>> = database.scorecardDao().getLast30DaysScorecards()

    fun getTodayScorecard(): Flow<ScorecardEntity?> = database.scorecardDao().getScorecardForDate(getTodayDateString())

    suspend fun saveOrUpdateTodayScorecard(
        wokeUpBy630: Boolean,
        completedBlock1: Boolean,
        completedBlock3: Boolean,
        completedFitness: Boolean,
        completedBlock5: Boolean,
        didShutdownRitual: Boolean,
        noPhoneBlocked: Boolean,
        notes: String = "",
        isLowEnergyDay: Boolean = false
    ) = withContext(Dispatchers.IO) {
        try {
            val todayStr = getTodayDateString()
            val existing = database.scorecardDao().getScorecardForDateSync(todayStr)
            val tempScorecard = ScorecardEntity(
                id = existing?.id ?: 0L,
                dateString = todayStr,
                wokeUpBy630 = wokeUpBy630,
                completedBlock1 = completedBlock1,
                completedBlock3 = completedBlock3,
                completedFitness = completedFitness,
                completedBlock5 = completedBlock5,
                didShutdownRitual = didShutdownRitual,
                noPhoneBlocked = noPhoneBlocked,
                totalScore = 0,
                notes = notes,
                isLowEnergyDay = isLowEnergyDay
            )
            val computedScore = tempScorecard.calculateScore()
            val finalScorecard = tempScorecard.copy(totalScore = computedScore)

            if (existing != null) {
                database.scorecardDao().updateScorecard(finalScorecard)
            } else {
                database.scorecardDao().insertScorecard(finalScorecard)
            }
        } catch (e: Throwable) {
            logAndSetError("saveOrUpdateTodayScorecard", e)
        }
    }

    // --- Journal ---
    val allJournalEntries: Flow<List<JournalEntryEntity>> = database.journalDao().getAllEntries()
    val weeklyReviews: Flow<List<JournalEntryEntity>> = database.journalDao().getWeeklyReviews()
    val monthlyReviews: Flow<List<JournalEntryEntity>> = database.journalDao().getMonthlyReviews()

    fun getTodayJournalEntry(): Flow<JournalEntryEntity?> = database.journalDao().getEntryForDate(getTodayDateString())
    fun searchJournal(query: String): Flow<List<JournalEntryEntity>> = database.journalDao().searchJournal(query)

    /**
     * Upserts a Journal Entry safely, checking if dateString already exists to preserve entity ID and prevent constraint collisions.
     */
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long = withContext(Dispatchers.IO) {
        try {
            val safeDate = entry.dateString.trim().ifBlank { getTodayDateString() }
            val existing = database.journalDao().getEntryForDateSync(safeDate)
            val finalEntry = if (existing != null && entry.id == 0L) {
                entry.copy(id = existing.id, dateString = safeDate)
            } else {
                entry.copy(dateString = safeDate)
            }

            if (existing != null) {
                database.journalDao().updateEntry(finalEntry)
                finalEntry.id
            } else {
                database.journalDao().insertEntry(finalEntry)
            }
        } catch (e: Throwable) {
            logAndSetError("insertJournalEntry", e)
            -1L
        }
    }

    suspend fun updateJournalEntry(entry: JournalEntryEntity) = withContext(Dispatchers.IO) {
        try {
            database.journalDao().updateEntry(entry)
        } catch (e: Throwable) {
            logAndSetError("updateJournalEntry", e)
        }
    }

    suspend fun deleteJournalEntry(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.journalDao().deleteEntryById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteJournalEntry", e)
        }
    }

    // --- Brain Dump ---
    val unprocessedBrainDumps: Flow<List<BrainDumpEntity>> = database.brainDumpDao().getUnprocessedNotes()
    val allBrainDumps: Flow<List<BrainDumpEntity>> = database.brainDumpDao().getAllNotes()

    suspend fun insertBrainDump(note: BrainDumpEntity): Long = withContext(Dispatchers.IO) {
        try {
            database.brainDumpDao().insertBrainDump(note)
        } catch (e: Throwable) {
            logAndSetError("insertBrainDump", e)
            -1L
        }
    }

    suspend fun updateBrainDump(note: BrainDumpEntity) = withContext(Dispatchers.IO) {
        try {
            database.brainDumpDao().updateBrainDump(note)
        } catch (e: Throwable) {
            logAndSetError("updateBrainDump", e)
        }
    }

    suspend fun markBrainDumpProcessed(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.brainDumpDao().markProcessed(id)
        } catch (e: Throwable) {
            logAndSetError("markBrainDumpProcessed", e)
        }
    }

    suspend fun deleteBrainDump(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.brainDumpDao().deleteBrainDumpById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteBrainDump", e)
        }
    }

    // --- Resource Vault ---
    val allResources: Flow<List<ResourceEntity>> = database.resourceDao().getAllResources()
    val favoriteResources: Flow<List<ResourceEntity>> = database.resourceDao().getFavoriteResources()

    fun getResourcesForSubject(subjectId: Long): Flow<List<ResourceEntity>> = database.resourceDao().getResourcesBySubject(subjectId)
    fun searchResources(query: String): Flow<List<ResourceEntity>> = database.resourceDao().searchResources(query)

    suspend fun insertResource(resource: ResourceEntity): Long = withContext(Dispatchers.IO) {
        try {
            database.resourceDao().insertResource(resource)
        } catch (e: Throwable) {
            logAndSetError("insertResource", e)
            -1L
        }
    }

    suspend fun updateResource(resource: ResourceEntity) = withContext(Dispatchers.IO) {
        try {
            database.resourceDao().updateResource(resource)
        } catch (e: Throwable) {
            logAndSetError("updateResource", e)
        }
    }

    suspend fun toggleResourceFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        try {
            database.resourceDao().toggleFavorite(id, isFavorite)
        } catch (e: Throwable) {
            logAndSetError("toggleResourceFavorite", e)
        }
    }

    suspend fun deleteResource(id: Long) = withContext(Dispatchers.IO) {
        try {
            database.resourceDao().deleteResourceById(id)
        } catch (e: Throwable) {
            logAndSetError("deleteResource", e)
        }
    }

    fun getDatabaseInstance(): AppDatabase = database
}
