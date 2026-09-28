package com.example.ai

import com.example.database.entity.MessageEntity
import com.example.tools.ToolDefinition

data class AiToolCall(
    val name: String,
    val arguments: Map<String, Any?>
)

data class AiResponse(
    val text: String,
    val toolCall: AiToolCall? = null,
    val isError: Boolean = false,
    val rawError: String? = null
)

interface AiProvider {
    val providerId: String
    val displayName: String

    suspend fun generateResponse(
        messages: List<MessageEntity>,
        systemPrompt: String,
        tools: List<ToolDefinition>,
        apiKey: String,
        modelName: String
    ): AiResponse
}
