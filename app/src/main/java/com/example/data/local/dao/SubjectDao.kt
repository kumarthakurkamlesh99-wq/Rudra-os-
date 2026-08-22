package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.SubjectEntity
import com.example.data.local.relations.SubjectWithChapters
import com.example.data.local.relations.SubjectWithStudySessions
import com.example.data.local.relations.SubjectWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC, id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Query("SELECT COUNT(*) FROM subjects WHERE id = :id")
    suspend fun countSubjectById(id: Long): Int

    @Query("SELECT * FROM subjects WHERE code = :code LIMIT 1")
    suspend fun getSubjectByCodeSync(code: String): SubjectEntity?

    @Query("SELECT id FROM subjects ORDER BY orderIndex ASC, id ASC LIMIT 1")
    suspend fun getFirstSubjectIdSync(): Long?

    @Transaction
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjectsWithChapters(): Flow<List<SubjectWithChapters>>

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectWithChaptersById(id: Long): Flow<SubjectWithChapters?>

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectWithTasksById(id: Long): Flow<SubjectWithTasks?>

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectWithStudySessionsById(id: Long): Flow<SubjectWithStudySessions?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>): List<Long>

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int
}

