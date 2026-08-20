package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ScorecardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScorecardDao {
    @Query("SELECT * FROM scorecards ORDER BY dateString DESC")
    fun getAllScorecards(): Flow<List<ScorecardEntity>>

    @Query("SELECT * FROM scorecards WHERE dateString = :dateString LIMIT 1")
    fun getScorecardForDate(dateString: String): Flow<ScorecardEntity?>

    @Query("SELECT * FROM scorecards WHERE dateString = :dateString LIMIT 1")
    suspend fun getScorecardForDateSync(dateString: String): ScorecardEntity?

    @Query("SELECT * FROM scorecards ORDER BY dateString DESC LIMIT 7")
    fun getLast7DaysScorecards(): Flow<List<ScorecardEntity>>

    @Query("SELECT * FROM scorecards ORDER BY dateString DESC LIMIT 30")
    fun getLast30DaysScorecards(): Flow<List<ScorecardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScorecard(scorecard: ScorecardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScorecards(scorecards: List<ScorecardEntity>): List<Long>

    @Update
    suspend fun updateScorecard(scorecard: ScorecardEntity)

    @Query("DELETE FROM scorecards WHERE id = :id")
    suspend fun deleteScorecardById(id: Long)
}
