package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["dateString"]),
        Index(value = ["startTimeMs"]),
        Index(value = ["blockNumber"])
    ]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String,
    val topic: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMinutes: Int,
    val blockNumber: Int? = null, // 1, 2, 3, 4, 5
    val isDeepWork: Boolean = true,
    val notes: String = "",
    val dateString: String // YYYY-MM-DD
)

