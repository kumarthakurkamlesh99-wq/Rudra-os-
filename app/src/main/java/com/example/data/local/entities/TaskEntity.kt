package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
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
        Index(value = ["isCompleted", "isArchived"]),
        Index(value = ["dueDate"]),
        Index(value = ["priority"]),
        Index(value = ["category"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val category: String = "Study", // School, Numericals, Theory, Revision, Personal
    val subjectId: Long? = null,
    val subjectName: String = "",
    val dueDate: String? = null,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurringPattern: String = "None",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

