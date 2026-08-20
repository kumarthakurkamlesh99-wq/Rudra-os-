package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_logs")
data class RevisionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val subjectName: String,
    val chapterTitle: String,
    val scheduledDate: String, // YYYY-MM-DD
    val intervalLabel: String, // "Same Day", "+1 Day", "+3 Days", "+7 Days", "+15 Days", "+30 Days", "Sunday Summary"
    val completedDate: String? = null,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
