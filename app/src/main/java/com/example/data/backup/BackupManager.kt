package com.example.data.backup

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun exportToJsonString(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        root.put("appName", "Rudra Life OS")
        root.put("version", 1)
        root.put("exportedAt", dateFormat.format(Date()))

        // Export Subjects
        val subjects = database.subjectDao().getAllSubjects().first()
        val subjectsArray = JSONArray()
        for (s in subjects) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("code", s.code)
            obj.put("colorHex", s.colorHex)
            obj.put("iconName", s.iconName)
            obj.put("description", s.description)
            obj.put("orderIndex", s.orderIndex)
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        // Export Chapters
        val chapters = database.chapterDao().getAllChapters().first()
        val chaptersArray = JSONArray()
        for (c in chapters) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("subjectId", c.subjectId)
            obj.put("chapterNumber", c.chapterNumber)
            obj.put("title", c.title)
            obj.put("status", c.status)
            obj.put("notes", c.notes)
            obj.put("revisionCount", c.revisionCount)
            obj.put("lastRevisionDate", c.lastRevisionDate ?: "")
            obj.put("nextRevisionDueDate", c.nextRevisionDueDate ?: "")
            obj.put("priority", c.priority)
            obj.put("progressPercent", c.progressPercent)
            obj.put("orderIndex", c.orderIndex)
            chaptersArray.put(obj)
        }
        root.put("chapters", chaptersArray)

        // Export Revision Logs
        val logs = database.revisionLogDao().getAllRevisionLogs().first()
        val logsArray = JSONArray()
        for (l in logs) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("chapterId", l.chapterId)
            obj.put("subjectName", l.subjectName)
            obj.put("chapterTitle", l.chapterTitle)
            obj.put("scheduledDate", l.scheduledDate)
            obj.put("intervalLabel", l.intervalLabel)
            obj.put("completedDate", l.completedDate ?: "")
            obj.put("isCompleted", l.isCompleted)
            obj.put("notes", l.notes)
            logsArray.put(obj)
        }
        root.put("revisionLogs", logsArray)

        // Export Tasks
        val tasks = database.taskDao().getActiveTasks().first() + database.taskDao().getCompletedTasks().first()
        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("priority", t.priority)
            obj.put("category", t.category)
            obj.put("subjectId", t.subjectId ?: -1L)
            obj.put("subjectName", t.subjectName)
            obj.put("dueDate", t.dueDate ?: "")
            obj.put("isCompleted", t.isCompleted)
            obj.put("isRecurring", t.isRecurring)
            obj.put("recurringPattern", t.recurringPattern)
            obj.put("isArchived", t.isArchived)
            obj.put("createdAt", t.createdAt)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Export Scorecards
        val scorecards = database.scorecardDao().getAllScorecards().first()
        val scorecardsArray = JSONArray()
        for (sc in scorecards) {
            val obj = JSONObject()
            obj.put("id", sc.id)
            obj.put("dateString", sc.dateString)
            obj.put("wokeUpBy630", sc.wokeUpBy630)
            obj.put("completedBlock1", sc.completedBlock1)
            obj.put("completedBlock3", sc.completedBlock3)
            obj.put("completedFitness", sc.completedFitness)
            obj.put("completedBlock5", sc.completedBlock5)
            obj.put("didShutdownRitual", sc.didShutdownRitual)
            obj.put("noPhoneBlocked", sc.noPhoneBlocked)
            obj.put("totalScore", sc.totalScore)
            obj.put("notes", sc.notes)
            obj.put("isLowEnergyDay", sc.isLowEnergyDay)
            scorecardsArray.put(obj)
        }
        root.put("scorecards", scorecardsArray)

        // Export Journal Entries
        val journalEntries = database.journalDao().getAllEntries().first()
        val journalArray = JSONArray()
        for (j in journalEntries) {
            val obj = JSONObject()
            obj.put("id", j.id)
            obj.put("dateString", j.dateString)
            obj.put("mood", j.mood)
            obj.put("winsDone", j.winsDone)
            obj.put("missedWhat", j.missedWhat)
            obj.put("tomorrowFocusAndBlock1", j.tomorrowFocusAndBlock1)
            obj.put("generalReflection", j.generalReflection)
            obj.put("isWeeklyReview", j.isWeeklyReview)
            obj.put("weeklyReviewStrongDay", j.weeklyReviewStrongDay)
            obj.put("weeklyReviewWeakDayAndTrigger", j.weeklyReviewWeakDayAndTrigger)
            obj.put("weeklyReviewNeglectedSubject", j.weeklyReviewNeglectedSubject)
            obj.put("weeklyReviewOneAdjustment", j.weeklyReviewOneAdjustment)
            obj.put("isMonthlyReview", j.isMonthlyReview)
            obj.put("monthlyReviewNotes", j.monthlyReviewNotes)
            journalArray.put(obj)
        }
        root.put("journalEntries", journalArray)

        // Export Brain Dumps
        val brainDumps = database.brainDumpDao().getAllNotes().first()
        val brainDumpsArray = JSONArray()
        for (bd in brainDumps) {
            val obj = JSONObject()
            obj.put("id", bd.id)
            obj.put("content", bd.content)
            obj.put("category", bd.category)
            obj.put("isProcessed", bd.isProcessed)
            obj.put("createdAt", bd.createdAt)
            brainDumpsArray.put(obj)
        }
        root.put("brainDumps", brainDumpsArray)

        // Export Resources
        val resources = database.resourceDao().getAllResources().first()
        val resourcesArray = JSONArray()
        for (r in resources) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("description", r.description)
            obj.put("resourceType", r.resourceType)
            obj.put("urlOrPath", r.urlOrPath)
            obj.put("subjectName", r.subjectName)
            obj.put("tags", r.tags)
            obj.put("isFavorite", r.isFavorite)
            resourcesArray.put(obj)
        }
        root.put("resources", resourcesArray)

        root.toString(2)
    }

    suspend fun saveBackupToFile(jsonContent: String): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(backupDir, "rudra_backup_$timestamp.json")
        file.writeText(jsonContent)
        file
    }

    suspend fun importFromJsonString(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var count = 0

            // Import Tasks if present
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    database.taskDao().insertTask(
                        TaskEntity(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            priority = obj.optString("priority", "Medium"),
                            category = obj.optString("category", "Study"),
                            subjectName = obj.optString("subjectName", ""),
                            dueDate = obj.optString("dueDate", null),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isRecurring = obj.optBoolean("isRecurring", false)
                        )
                    )
                    count++
                }
            }

            // Import Scorecards if present
            if (root.has("scorecards")) {
                val array = root.getJSONArray("scorecards")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.scorecardDao().insertScorecard(
                        ScorecardEntity(
                            dateString = obj.getString("dateString"),
                            wokeUpBy630 = obj.optBoolean("wokeUpBy630", false),
                            completedBlock1 = obj.optBoolean("completedBlock1", false),
                            completedBlock3 = obj.optBoolean("completedBlock3", false),
                            completedFitness = obj.optBoolean("completedFitness", false),
                            completedBlock5 = obj.optBoolean("completedBlock5", false),
                            didShutdownRitual = obj.optBoolean("didShutdownRitual", false),
                            noPhoneBlocked = obj.optBoolean("noPhoneBlocked", false),
                            totalScore = obj.optInt("totalScore", 0),
                            notes = obj.optString("notes", "")
                        )
                    )
                    count++
                }
            }

            // Import Brain Dumps if present
            if (root.has("brainDumps")) {
                val array = root.getJSONArray("brainDumps")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.brainDumpDao().insertBrainDump(
                        BrainDumpEntity(
                            content = obj.getString("content"),
                            category = obj.optString("category", BrainDumpEntity.CATEGORY_PARKING_LOT),
                            isProcessed = obj.optBoolean("isProcessed", false)
                        )
                    )
                    count++
                }
            }

            // Import Resources if present
            if (root.has("resources")) {
                val array = root.getJSONArray("resources")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    database.resourceDao().insertResource(
                        ResourceEntity(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            resourceType = obj.optString("resourceType", ResourceEntity.TYPE_LINK),
                            urlOrPath = obj.getString("urlOrPath"),
                            subjectName = obj.optString("subjectName", ""),
                            tags = obj.optString("tags", ""),
                            isFavorite = obj.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            Result.success("Successfully restored data records ($count items updated).")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
