package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.MockTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_tests ORDER BY testDate DESC, id DESC")
    fun getAllMockTests(): Flow<List<MockTestEntity>>

    @Query("SELECT * FROM mock_tests WHERE subject = :subject ORDER BY testDate DESC")
    fun getMockTestsBySubject(subject: String): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTestEntity): Long

    @Update
    suspend fun updateMockTest(test: MockTestEntity)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteMockTestById(id: Long)
}
