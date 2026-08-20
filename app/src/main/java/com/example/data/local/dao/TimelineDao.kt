package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TimelineBlockEntity
import com.example.data.local.entities.TimelinePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline_presets WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllPresets(): Flow<List<TimelinePresetEntity>>

    @Query("SELECT * FROM timeline_presets WHERE isActive = 1 LIMIT 1")
    fun getActivePreset(): Flow<TimelinePresetEntity?>

    @Query("SELECT * FROM timeline_presets WHERE id = :presetId")
    suspend fun getPresetById(presetId: Long): TimelinePresetEntity?

    @Query("SELECT * FROM timeline_blocks WHERE presetId = :presetId ORDER BY orderIndex ASC, startTime ASC")
    fun getBlocksForPreset(presetId: Long): Flow<List<TimelineBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimelinePresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<TimelinePresetEntity>): List<Long>

    @Update
    suspend fun updatePreset(preset: TimelinePresetEntity)

    @Query("UPDATE timeline_presets SET isActive = 0")
    suspend fun deactivateAllPresets()

    @Query("UPDATE timeline_presets SET isActive = 1 WHERE id = :presetId")
    suspend fun activatePreset(presetId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: TimelineBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<TimelineBlockEntity>): List<Long>

    @Update
    suspend fun updateBlock(block: TimelineBlockEntity)

    @Query("UPDATE timeline_blocks SET isCompleted = :isCompleted WHERE id = :blockId")
    suspend fun updateBlockCompletion(blockId: Long, isCompleted: Boolean)

    @Query("UPDATE timeline_blocks SET isCompleted = 0 WHERE presetId = :presetId")
    suspend fun resetPresetBlockCompletions(presetId: Long)

    @Query("DELETE FROM timeline_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: Long)

    @Query("DELETE FROM timeline_presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: Long)
}
