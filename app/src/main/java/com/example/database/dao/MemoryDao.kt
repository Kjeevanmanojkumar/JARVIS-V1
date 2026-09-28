package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    suspend fun getAllMemoriesList(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE memoryKey LIKE '%' || :query || '%' OR memoryValue LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE memoryKey = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memories WHERE memoryKey = :key")
    suspend fun deleteMemoryByKey(key: String): Int

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    @Query("SELECT COUNT(*) FROM memories")
    fun getMemoryCount(): Flow<Int>
}
