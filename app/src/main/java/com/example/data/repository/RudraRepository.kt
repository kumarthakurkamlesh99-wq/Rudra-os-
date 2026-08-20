package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RudraRepository(
    private val database: AppDatabase,
    private val preferences: UserPreferences
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDateString(): String = dateFormat.format(Date())

    // --- Preferences ---
    val themeMode: Flow<String> = preferences.themeMode
    val letsStudyMode: Flow<String> = preferences.letsStudyMode
    val isLowEnergyMode: Flow<Boolean> = preferences.isLowEnergyMode
    val lastBackupDate: Flow<String> = preferences.lastBackupDate

    suspend fun setThemeMode(mode: String) = preferences.setThemeMode(mode)
    suspend fun setLetsStudyMode(mode: String) = preferences.setLetsStudyMode(mode)
    suspend fun setLowEnergyMode(enabled: Boolean) = preferences.setLowEnergyMode(enabled)
    suspend fun setLastBackupDate(date: String) = preferences.setLastBackupDate(date)

    // --- Subjects & Chapters ---
    val allSubjects: Flow<List<SubjectEntity>> = database.subjectDao().getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = database.chapterDao().getAllChapters()
    val weakChapters: Flow<List<ChapterEntity>> = database.chapterDao().getWeakChapters()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        database.chapterDao().getChaptersBySubject(subjectId)

    suspend fun insertSubject(subject: SubjectEntity): Long = database.subjectDao().insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = database.subjectDao().updateSubject(subject)
    suspend fun deleteSubject(id: Long) = database.subjectDao().deleteSubjectById(id)

    suspend fun insertChapter(chapter: ChapterEntity): Long = database.chapterDao().insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = database.chapterDao().updateChapter(chapter)
    suspend fun updateChapterStatus(id: Long, status: String, progress: Int) =
        database.chapterDao().updateChapterStatus(id, status, progress)
    suspend fun deleteChapter(id: Long) = database.chapterDao().deleteChapterById(id)

    // --- Spaced Repetition Engine ---
    val allRevisionLogs: Flow<List<RevisionLogEntity>> = database.revisionLogDao().getAllRevisionLogs()
    val completedRevisions: Flow<List<RevisionLogEntity>> = database.revisionLogDao().getCompletedRevisions()

    fun getDueRevisions(): Flow<List<RevisionLogEntity>> =
        database.revisionLogDao().getDueRevisions(getTodayDateString())

    fun getRevisionsForDate(dateStr: String): Flow<List<RevisionLogEntity>> =
        database.revisionLogDao().getRevisionsByDate(dateStr)

    suspend fun markRevisionCompleted(logId: Long, chapterId: Long, currentInterval: String) {
        val todayStr = getTodayDateString()
        database.revisionLogDao().markCompleted(logId, todayStr)

        // Calculate next spaced repetition interval
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
    }

    suspend fun scheduleNewRevision(chapterId: Long, subjectName: String, chapterTitle: String, intervalLabel: String, daysToAdd: Int, notes: String = "") {
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
    }

    private fun getNextRevisionInterval(current: String): Pair<String, Int>? {
        return when (current) {
            "Same Day" -> Pair("+1 Day", 1)
            "+1 Day" -> Pair("+3 Days", 3)
            "+3 Days" -> Pair("+7 Days", 7)
            "+7 Days" -> Pair("+15 Days", 15)
            "+15 Days" -> Pair("+30 Days", 30)
            else -> null
        }
    }

    // --- Timeline Presets & Blocks ---
    val allPresets: Flow<List<TimelinePresetEntity>> = database.timelineDao().getAllPresets()
    val activePreset: Flow<TimelinePresetEntity?> = database.timelineDao().getActivePreset()

    fun getBlocksForPreset(presetId: Long): Flow<List<TimelineBlockEntity>> =
        database.timelineDao().getBlocksForPreset(presetId)

    suspend fun activatePreset(presetId: Long) {
        database.timelineDao().deactivateAllPresets()
        database.timelineDao().activatePreset(presetId)
    }

    suspend fun insertPreset(preset: TimelinePresetEntity): Long = database.timelineDao().insertPreset(preset)
    suspend fun updatePreset(preset: TimelinePresetEntity) = database.timelineDao().updatePreset(preset)
    suspend fun deletePreset(presetId: Long) = database.timelineDao().deletePresetById(presetId)

    suspend fun insertBlock(block: TimelineBlockEntity): Long = database.timelineDao().insertBlock(block)
    suspend fun updateBlock(block: TimelineBlockEntity) = database.timelineDao().updateBlock(block)
    suspend fun updateBlockCompletion(blockId: Long, isCompleted: Boolean) =
        database.timelineDao().updateBlockCompletion(blockId, isCompleted)
    suspend fun resetPresetBlockCompletions(presetId: Long) =
        database.timelineDao().resetPresetBlockCompletions(presetId)
    suspend fun deleteBlock(blockId: Long) = database.timelineDao().deleteBlockById(blockId)

    // --- Tasks ---
    val activeTasks: Flow<List<TaskEntity>> = database.taskDao().getActiveTasks()
    val pendingTasks: Flow<List<TaskEntity>> = database.taskDao().getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = database.taskDao().getCompletedTasks()

    fun getOverdueTasks(): Flow<List<TaskEntity>> = database.taskDao().getOverdueTasks(getTodayDateString())

    suspend fun insertTask(task: TaskEntity): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = database.taskDao().updateTask(task)
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean) = database.taskDao().updateTaskCompletion(id, isCompleted)
    suspend fun archiveCompletedTasks() = database.taskDao().archiveCompletedTasks()
    suspend fun deleteTask(id: Long) = database.taskDao().deleteTaskById(id)

    // --- Study Sessions ---
    val allSessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllSessions()

    fun getSessionsForDate(dateStr: String): Flow<List<StudySessionEntity>> =
        database.studySessionDao().getSessionsForDate(dateStr)

    fun getTotalMinutesForDate(dateStr: String): Flow<Int?> =
        database.studySessionDao().getTotalMinutesForDate(dateStr)

    suspend fun insertSession(session: StudySessionEntity): Long = database.studySessionDao().insertSession(session)
    suspend fun deleteSession(id: Long) = database.studySessionDao().deleteSessionById(id)

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
    ) {
        val todayStr = getTodayDateString()
        val existing = database.scorecardDao().getScorecardForDateSync(todayStr)
        val tempScorecard = ScorecardEntity(
            id = existing?.id ?: 0,
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
    }

    // --- Journal ---
    val allJournalEntries: Flow<List<JournalEntryEntity>> = database.journalDao().getAllEntries()
    val weeklyReviews: Flow<List<JournalEntryEntity>> = database.journalDao().getWeeklyReviews()
    val monthlyReviews: Flow<List<JournalEntryEntity>> = database.journalDao().getMonthlyReviews()

    fun getTodayJournalEntry(): Flow<JournalEntryEntity?> = database.journalDao().getEntryForDate(getTodayDateString())
    fun searchJournal(query: String): Flow<List<JournalEntryEntity>> = database.journalDao().searchJournal(query)

    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long = database.journalDao().insertEntry(entry)
    suspend fun updateJournalEntry(entry: JournalEntryEntity) = database.journalDao().updateEntry(entry)
    suspend fun deleteJournalEntry(id: Long) = database.journalDao().deleteEntryById(id)

    // --- Brain Dump ---
    val unprocessedBrainDumps: Flow<List<BrainDumpEntity>> = database.brainDumpDao().getUnprocessedNotes()
    val allBrainDumps: Flow<List<BrainDumpEntity>> = database.brainDumpDao().getAllNotes()

    suspend fun insertBrainDump(note: BrainDumpEntity): Long = database.brainDumpDao().insertBrainDump(note)
    suspend fun updateBrainDump(note: BrainDumpEntity) = database.brainDumpDao().updateBrainDump(note)
    suspend fun markBrainDumpProcessed(id: Long) = database.brainDumpDao().markProcessed(id)
    suspend fun deleteBrainDump(id: Long) = database.brainDumpDao().deleteBrainDumpById(id)

    // --- Resource Vault ---
    val allResources: Flow<List<ResourceEntity>> = database.resourceDao().getAllResources()
    val favoriteResources: Flow<List<ResourceEntity>> = database.resourceDao().getFavoriteResources()

    fun getResourcesForSubject(subjectId: Long): Flow<List<ResourceEntity>> = database.resourceDao().getResourcesBySubject(subjectId)
    fun searchResources(query: String): Flow<List<ResourceEntity>> = database.resourceDao().searchResources(query)

    suspend fun insertResource(resource: ResourceEntity): Long = database.resourceDao().insertResource(resource)
    suspend fun updateResource(resource: ResourceEntity) = database.resourceDao().updateResource(resource)
    suspend fun toggleResourceFavorite(id: Long, isFavorite: Boolean) = database.resourceDao().toggleFavorite(id, isFavorite)
    suspend fun deleteResource(id: Long) = database.resourceDao().deleteResourceById(id)

    fun getDatabaseInstance(): AppDatabase = database
}
