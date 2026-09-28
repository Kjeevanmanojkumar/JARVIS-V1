package com.example.database.repository

import com.example.database.dao.MemoryDao
import com.example.database.entity.MemoryEntity
import com.example.model.MemoryCategory
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {

    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val memoryCount: Flow<Int> = memoryDao.getMemoryCount()

    suspend fun saveMemory(
        key: String,
        value: String,
        category: MemoryCategory = MemoryCategory.GENERAL
    ) {
        val cleanKey = key.trim().lowercase()
        val existing = memoryDao.getMemoryByKey(cleanKey)
        val entity = MemoryEntity(
            id = existing?.id ?: 0L,
            memoryKey = cleanKey,
            memoryValue = value.trim(),
            category = category.name,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        memoryDao.insertOrUpdateMemory(entity)
    }

    suspend fun getMemory(key: String): MemoryEntity? {
        return memoryDao.getMemoryByKey(key.trim().lowercase())
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun deleteMemoryByKey(key: String): Boolean {
        val deletedCount = memoryDao.deleteMemoryByKey(key.trim().lowercase())
        return deletedCount > 0
    }

    suspend fun clearAll() {
        memoryDao.clearAllMemories()
    }

    suspend fun getAllMemoriesList(): List<MemoryEntity> {
        return memoryDao.getAllMemoriesList()
    }

    suspend fun getRelevantMemories(query: String, limit: Int = 5): List<MemoryEntity> {
        val all = memoryDao.getAllMemoriesList()
        if (all.isEmpty()) return emptyList()

        val tokens = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        if (tokens.isEmpty()) return all.take(limit)

        val scored = all.map { memory ->
            var score = 0
            val keyLower = memory.memoryKey.lowercase()
            val valLower = memory.memoryValue.lowercase()

            for (token in tokens) {
                if (keyLower.contains(token)) score += 3
                if (valLower.contains(token)) score += 1
            }
            Pair(memory, score)
        }

        val matches = scored.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return if (matches.isNotEmpty()) {
            matches.take(limit)
        } else {
            all.take(limit)
        }
    }
}
