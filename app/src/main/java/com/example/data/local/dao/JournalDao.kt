package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateString DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE dateString = :dateString LIMIT 1")
    fun getEntryForDate(dateString: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE isWeeklyReview = 1 ORDER BY dateString DESC")
    fun getWeeklyReviews(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isMonthlyReview = 1 ORDER BY dateString DESC")
    fun getMonthlyReviews(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE winsDone LIKE '%' || :query || '%' OR missedWhat LIKE '%' || :query || '%' OR generalReflection LIKE '%' || :query || '%' ORDER BY dateString DESC")
    fun searchJournal(query: String): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<JournalEntryEntity>): List<Long>

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
