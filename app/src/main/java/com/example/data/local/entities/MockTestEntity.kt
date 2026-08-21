package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mock_tests",
    indices = [Index(value = ["subject"]), Index(value = ["testDate"])]
)
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String, // Physics, Chemistry, Biology, Full PCB
    val chapter: String = "",
    val testName: String,
    val marksObtained: Double,
    val totalMarks: Double,
    val percentage: Double = if (totalMarks > 0) (marksObtained / totalMarks) * 100.0 else 0.0,
    val testDate: String, // YYYY-MM-DD
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val performanceBadge: String
        get() = when {
            percentage >= 75.0 -> "GREEN"
            percentage >= 50.0 -> "YELLOW"
            else -> "RED"
        }
}
