package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timeline_blocks",
    foreignKeys = [
        ForeignKey(
            entity = TimelinePresetEntity::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["presetId"])]
)
data class TimelineBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val presetId: Long,
    val title: String,
    val startTime: String,
    val endTime: String,
    val description: String = "",
    val category: String = "Study", // Study, Routine, School, Rest, Fitness, Shutdown
    val triggerAction: String = "", // e.g., "Water peene ke turant baad"
    val backupPlan: String = "",
    val failureRecovery: String = "",
    val colorHex: String = "#38BDF8",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
