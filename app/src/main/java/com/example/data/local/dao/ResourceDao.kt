package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY isFavorite DESC, id DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getResourcesBySubject(subjectId: Long): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchResources(query: String): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>): List<Long>

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Query("UPDATE resources SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResourceById(id: Long)
}
