package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brain_dumps")
data class BrainDumpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: String = CATEGORY_PARKING_LOT, // Parking Lot, Idea, Thought, Quick Link, Note
    val isProcessed: Boolean = false,
    val convertedToTaskId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORY_PARKING_LOT = "Parking Lot (Study Distraction)"
        const val CATEGORY_IDEA = "Idea"
        const val CATEGORY_THOUGHT = "Thought"
        const val CATEGORY_LINK = "Quick Link"
        const val CATEGORY_NOTE = "Random Note"
    }
}
