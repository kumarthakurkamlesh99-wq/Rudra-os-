package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.BrainDumpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainDumpDao {
    @Query("SELECT * FROM brain_dumps WHERE isProcessed = 0 ORDER BY id DESC")
    fun getUnprocessedNotes(): Flow<List<BrainDumpEntity>>

    @Query("SELECT * FROM brain_dumps ORDER BY id DESC")
    fun getAllNotes(): Flow<List<BrainDumpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrainDump(note: BrainDumpEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrainDumps(notes: List<BrainDumpEntity>): List<Long>

    @Update
    suspend fun updateBrainDump(note: BrainDumpEntity)

    @Query("UPDATE brain_dumps SET isProcessed = 1 WHERE id = :id")
    suspend fun markProcessed(id: Long)

    @Query("DELETE FROM brain_dumps WHERE id = :id")
    suspend fun deleteBrainDumpById(id: Long)
}
