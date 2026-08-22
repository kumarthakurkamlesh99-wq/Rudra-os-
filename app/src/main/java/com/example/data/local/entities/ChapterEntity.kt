package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"]), Index(value = ["chapterNumber"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val chapterNumber: Int = 1,
    val title: String,
    val status: String = STATUS_NOT_STARTED, // Not Started, Learning, Completed, Revised
    val progressPercent: Int = 0, // 0-100%
    val totalLectures: Int = 8,
    val watchedLectures: Int = 0,
    val ncertRead: Boolean = false,
    val ncertRevised: Boolean = false,
    val notesStatus: String = NOTES_NOT_STARTED, // Not Started, In Progress, Completed
    val pyqStatus: String = PYQ_PENDING, // Pending, Completed
    val mockTestStatus: String = MOCK_NOT_ATTEMPTED, // Not Attempted, Attempted
    val revision1Done: Boolean = false,
    val revision1Date: String? = null,
    val revision2Done: Boolean = false,
    val revision2Date: String? = null,
    val revision3Done: Boolean = false,
    val revision3Date: String? = null,
    val confidenceRating: Int = 3, // 1 to 5
    val difficultyRating: Int = 3, // 1 to 5
    val totalStudyHours: Double = 0.0,
    val lastStudiedDate: String? = null,
    val notes: String = "",
    val revisionCount: Int = 0,
    val lastRevisionDate: String? = null,
    val nextRevisionDueDate: String? = null,
    val priority: String = "Normal", // High, Normal, Weak Area
    val orderIndex: Int = 0
) {
    val remainingLectures: Int
        get() = (totalLectures - watchedLectures).coerceAtLeast(0)

    val lectureProgressPercent: Int
        get() = if (totalLectures > 0) ((watchedLectures.toFloat() / totalLectures) * 100).toInt().coerceIn(0, 100) else 0

    // Heatmap / Weakness evaluation
    // Weak = low confidence (<=2), or difficulty >= 4, or progress < 50%, or not revised in 14 days
    val isWeak: Boolean
        get() = confidenceRating <= 2 || difficultyRating >= 4 || (status == STATUS_NOT_STARTED && priority == "High")

    val heatmapColorType: String
        get() = when {
            status == STATUS_COMPLETED && confidenceRating >= 4 && revision1Done -> "GREEN" // Strong
            status == STATUS_NOT_STARTED || confidenceRating <= 2 || priority == "Weak Area" -> "RED" // Weak
            else -> "YELLOW" // Moderate
        }

    val weaknessScore: Int
        get() {
            var score = 0
            if (confidenceRating <= 2) score += 30
            if (difficultyRating >= 4) score += 20
            if (progressPercent < 40) score += 20
            if (!revision1Done && status == STATUS_COMPLETED) score += 15
            if (pyqStatus == PYQ_PENDING) score += 10
            if (ncertRead.not()) score += 5
            return score.coerceIn(0, 100)
        }

    companion object {
        const val STATUS_NOT_STARTED = "Not Started"
        const val STATUS_LEARNING = "Learning"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_REVISED = "Revised"

        const val NOTES_NOT_STARTED = "Not Started"
        const val NOTES_IN_PROGRESS = "In Progress"
        const val NOTES_COMPLETED = "Completed"

        const val PYQ_PENDING = "Pending"
        const val PYQ_COMPLETED = "Completed"

        const val MOCK_NOT_ATTEMPTED = "Not Attempted"
        const val MOCK_ATTEMPTED = "Attempted"
    }
}
