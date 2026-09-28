package com.example.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // "USER", "ASSISTANT", "TOOL"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolName: String? = null,
    val toolArguments: String? = null,
    val toolResult: String? = null,
    val isError: Boolean = false
)
