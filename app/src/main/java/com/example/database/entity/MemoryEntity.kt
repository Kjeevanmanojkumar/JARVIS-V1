package com.example.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memories",
    indices = [Index(value = ["memoryKey"], unique = true)]
)
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memoryKey: String,
    val memoryValue: String,
    val category: String = "GENERAL",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
