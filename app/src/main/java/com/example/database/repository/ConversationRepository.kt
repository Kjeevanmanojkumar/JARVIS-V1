package com.example.database.repository

import com.example.database.dao.ConversationDao
import com.example.database.dao.MessageDao
import com.example.database.entity.ConversationEntity
import com.example.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    fun getGlobalRecentMessages(limit: Int = 10): Flow<List<MessageEntity>> =
        messageDao.getGlobalRecentMessages(limit)

    suspend fun getOrCreateActiveConversation(): Long {
        val latest = conversationDao.getLatestConversation()
        return if (latest != null) {
            latest.id
        } else {
            conversationDao.insertConversation(
                ConversationEntity(title = "Primary Session")
            )
        }
    }

    suspend fun createNewConversation(title: String = "Session ${System.currentTimeMillis() % 10000}"): Long {
        return conversationDao.insertConversation(
            ConversationEntity(title = title)
        )
    }

    suspend fun addMessage(
        conversationId: Long,
        role: String,
        content: String,
        toolName: String? = null,
        toolArguments: String? = null,
        toolResult: String? = null,
        isError: Boolean = false
    ): Long {
        val msgId = messageDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = role,
                content = content,
                toolName = toolName,
                toolArguments = toolArguments,
                toolResult = toolResult,
                isError = isError
            )
        )
        // Update conversation timestamp
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.updateConversation(conv.copy(lastUpdatedAt = System.currentTimeMillis()))
        }
        return msgId
    }

    suspend fun getRecentMessagesForContext(conversationId: Long, limit: Int = 6): List<MessageEntity> {
        return messageDao.getRecentMessages(conversationId, limit).reversed()
    }

    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAll() {
        conversationDao.clearAllConversations()
    }
}
