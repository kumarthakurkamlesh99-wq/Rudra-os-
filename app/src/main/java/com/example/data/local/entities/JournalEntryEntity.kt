package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["dateString"], unique = true),
        Index(value = ["isWeeklyReview"]),
        Index(value = ["isMonthlyReview"]),
        Index(value = ["timestamp"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val mood: String = "Normal", // Great, Normal, Tired, Stressed, Low Energy
    val winsDone: String = "", // Aaj kya kiya
    val missedWhat: String = "", // Kya miss hua
    val tomorrowFocusAndBlock1: String = "", // Kal ka ek focus & Block 1 topic
    val generalReflection: String = "",
    val isWeeklyReview: Boolean = false,
    val weeklyReviewStrongDay: String = "",
    val weeklyReviewWeakDayAndTrigger: String = "",
    val weeklyReviewNeglectedSubject: String = "",
    val weeklyReviewOneAdjustment: String = "",
    val isMonthlyReview: Boolean = false,
    val monthlyReviewNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

