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
    indices = [Index(value = ["subjectId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val chapterNumber: Int,
    val title: String,
    val status: String = STATUS_NOT_STARTED, // NOT_STARTED, IN_PROGRESS, COMPLETED, REVISED
    val notes: String = "",
    val revisionCount: Int = 0,
    val lastRevisionDate: String? = null,
    val nextRevisionDueDate: String? = null,
    val priority: String = "Normal", // High, Normal, Weak Area
    val progressPercent: Int = 0,
    val orderIndex: Int = 0
) {
    companion object {
        const val STATUS_NOT_STARTED = "Not Started"
        const val STATUS_IN_PROGRESS = "In Progress"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_REVISED = "Revised"
    }
}
