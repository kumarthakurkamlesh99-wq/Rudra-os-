package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakRecordEntity(
    @PrimaryKey
    val streakKey: String, // STUDY, RUNNING, NO_PORN, NO_PROCRASTINATION, REVISION
    val title: String,
    val description: String,
    val iconName: String,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDate: String? = null,
    val historyLog: String = "" // comma separated YYYY-MM-DD
) {
    companion object {
        const val KEY_STUDY = "STUDY"
        const val KEY_RUNNING = "RUNNING"
        const val KEY_NO_PORN = "NO_PORN"
        const val KEY_NO_PROCRASTINATION = "NO_PROCRASTINATION"
        const val KEY_REVISION = "REVISION"
    }
}
