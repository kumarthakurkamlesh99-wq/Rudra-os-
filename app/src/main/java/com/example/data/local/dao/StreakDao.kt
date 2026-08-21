package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.StreakRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<StreakRecordEntity>>

    @Query("SELECT * FROM streaks WHERE streakKey = :key")
    suspend fun getStreakByKey(key: String): StreakRecordEntity?

    @Query("SELECT * FROM streaks WHERE streakKey = :key")
    fun getStreakFlowByKey(key: String): Flow<StreakRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreaks(streaks: List<StreakRecordEntity>)

    @Update
    suspend fun updateStreak(streak: StreakRecordEntity)
}
