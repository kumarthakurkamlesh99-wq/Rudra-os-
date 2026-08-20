package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline_presets")
data class TimelinePresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = false,
    val isArchived: Boolean = false
)
