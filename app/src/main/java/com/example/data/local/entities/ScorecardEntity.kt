package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scorecards")
data class ScorecardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val wokeUpBy630: Boolean = false, // 1 pt (Woke up by 6:30 AM hard cap)
    val completedBlock1: Boolean = false, // 1 pt (Study Block 1)
    val completedBlock3: Boolean = false, // 1 pt (Study Block 3)
    val completedFitness: Boolean = false, // 1 pt (Fitness block min 15m)
    val completedBlock5: Boolean = false, // 1 pt (Study Block 5 revision)
    val didShutdownRitual: Boolean = false, // 1 pt (Shutdown ritual done)
    val noPhoneBlocked: Boolean = false, // 1 pt (No phone during study hours)
    val totalScore: Int = 0, // 0 to 7
    val notes: String = "",
    val isLowEnergyDay: Boolean = false
) {
    fun calculateScore(): Int {
        var score = 0
        if (wokeUpBy630) score++
        if (completedBlock1) score++
        if (completedBlock3) score++
        if (completedFitness) score++
        if (completedBlock5) score++
        if (didShutdownRitual) score++
        if (noPhoneBlocked) score++
        return score
    }

    val scoreStatus: String
        get() = when {
            totalScore >= 5 -> STATUS_GREEN
            totalScore in 3..4 -> STATUS_YELLOW
            else -> STATUS_RED
        }

    companion object {
        const val STATUS_GREEN = "Green Day (System Working)"
        const val STATUS_YELLOW = "Yellow Day (Survived & Logged)"
        const val STATUS_RED = "Red Day (Emergency Protocol)"
    }
}
