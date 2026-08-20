package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.RevisionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionLogDao {
    @Query("SELECT * FROM revision_logs ORDER BY scheduledDate ASC, id DESC")
    fun getAllRevisionLogs(): Flow<List<RevisionLogEntity>>

    @Query("SELECT * FROM revision_logs WHERE scheduledDate = :date ORDER BY isCompleted ASC, id ASC")
    fun getRevisionsByDate(date: String): Flow<List<RevisionLogEntity>>

    @Query("SELECT * FROM revision_logs WHERE isCompleted = 0 AND scheduledDate <= :currentDate ORDER BY scheduledDate ASC")
    fun getDueRevisions(currentDate: String): Flow<List<RevisionLogEntity>>

    @Query("SELECT * FROM revision_logs WHERE isCompleted = 0 AND scheduledDate <= :currentDate ORDER BY scheduledDate ASC")
    suspend fun getDueRevisionsList(currentDate: String): List<RevisionLogEntity>

    @Query("SELECT * FROM revision_logs WHERE isCompleted = 0 AND scheduledDate < :currentDate ORDER BY scheduledDate ASC")
    suspend fun getMissedRevisionsList(currentDate: String): List<RevisionLogEntity>

    @Query("SELECT * FROM revision_logs WHERE isCompleted = 1 ORDER BY completedDate DESC LIMIT 50")
    fun getCompletedRevisions(): Flow<List<RevisionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionLog(log: RevisionLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionLogs(logs: List<RevisionLogEntity>): List<Long>

    @Update
    suspend fun updateRevisionLog(log: RevisionLogEntity)

    @Query("UPDATE revision_logs SET isCompleted = 1, completedDate = :completedDate WHERE id = :id")
    suspend fun markCompleted(id: Long, completedDate: String)

    @Query("DELETE FROM revision_logs WHERE id = :id")
    suspend fun deleteRevisionLogById(id: Long)
}
