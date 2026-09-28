package com.example.ai

import android.util.Log
import com.example.database.repository.ConversationRepository
import com.example.database.repository.MemoryRepository
import com.example.model.JarvisState
import com.example.tools.ToolRegistry
import com.example.tools.ToolResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DebugInfo(
    val lastRecognizedText: String = "",
    val activeProvider: String = "",
    val aiStatus: String = "IDLE",
    val selectedTool: String? = null,
    val toolArguments: String? = null,
    val toolResult: String? = null,
    val latencyMs: Long = 0,
    val errorMessage: String? = null
)

class AiEngine(
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val toolRegistry: ToolRegistry,
    private val geminiProvider: GeminiAiProvider,
    private val localProvider: LocalRuleProvider
) {
    private val _debugInfo = MutableStateFlow(DebugInfo())
    val debugInfo: StateFlow<DebugInfo> = _debugInfo.asStateFlow()

    suspend fun processCommand(
        userText: String,
        conversationId: Long,
        apiKey: String,
        modelName: String,
        customPrompt: String?,
        isOnline: Boolean,
        onStateChanged: (JarvisState) -> Unit,
        onResponseReady: (responseText: String, speakText: String) -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        onStateChanged(JarvisState.THINKING)

        _debugInfo.value = _debugInfo.value.copy(
            lastRecognizedText = userText,
            aiStatus = "PROCESSING",
            selectedTool = null,
            toolArguments = null,
            toolResult = null,
            errorMessage = null
        )

        // 1. Record user message in DB
        conversationRepository.addMessage(
            conversationId = conversationId,
            role = "USER",
            content = userText
        )

        // 2. Fetch context: relevant memories and recent messages
        val relevantMemories = memoryRepository.getRelevantMemories(userText, limit = 5)
        val recentMessages = conversationRepository.getRecentMessagesForContext(conversationId, limit = 8)
        val toolDefs = toolRegistry.getToolDefinitions()

        val fullSystemPrompt = PromptManager.buildSystemInstruction(
            customPrompt = customPrompt,
            memories = relevantMemories,
            tools = toolDefs
        )

        // 3. Choose provider: Gemini if online and valid key, otherwise Local rule provider
        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
        var provider: AiProvider = if (isOnline && hasValidKey) geminiProvider else localProvider
        _debugInfo.value = _debugInfo.value.copy(activeProvider = provider.displayName)

        var aiResponse = try {
            provider.generateResponse(
                messages = recentMessages,
                systemPrompt = fullSystemPrompt,
                tools = toolDefs,
                apiKey = apiKey,
                modelName = modelName
            )
        } catch (e: Exception) {
            Log.w(TAG, "Primary provider failed, falling back to local provider", e)
            AiResponse("Communication failure", isError = true, rawError = e.message)
        }

        // Fallback to local rule engine if Gemini returned an error or was unavailable
        if (aiResponse.isError && provider != localProvider) {
            Log.d(TAG, "Gemini had error: ${aiResponse.rawError}. Falling back to local offline engine.")
            provider = localProvider
            _debugInfo.value = _debugInfo.value.copy(
                activeProvider = "${localProvider.displayName} (Fallback)",
                errorMessage = aiResponse.rawError
            )
            aiResponse = localProvider.generateResponse(
                messages = recentMessages,
                systemPrompt = fullSystemPrompt,
                tools = toolDefs,
                apiKey = apiKey,
                modelName = modelName
            )
        }

        // 4. Handle Tool Calling if requested
        val toolCall = aiResponse.toolCall
        if (toolCall != null) {
            onStateChanged(JarvisState.EXECUTING)
            _debugInfo.value = _debugInfo.value.copy(
                selectedTool = toolCall.name,
                toolArguments = toolCall.arguments.toString(),
                aiStatus = "EXECUTING_TOOL"
            )

            val toolResult: ToolResult = toolRegistry.executeTool(toolCall.name, toolCall.arguments)

            _debugInfo.value = _debugInfo.value.copy(
                toolResult = toolResult.message
            )

            // Save tool execution in database
            conversationRepository.addMessage(
                conversationId = conversationId,
                role = "TOOL",
                content = toolResult.message,
                toolName = toolCall.name,
                toolArguments = toolCall.arguments.toString(),
                toolResult = toolResult.message,
                isError = !toolResult.success
            )

            val finalMessage = if (toolResult.success) {
                toolResult.message
            } else {
                "Unable to complete ${toolCall.name}: ${toolResult.message}"
            }

            conversationRepository.addMessage(
                conversationId = conversationId,
                role = "ASSISTANT",
                content = finalMessage,
                toolName = toolCall.name,
                toolResult = toolResult.message,
                isError = !toolResult.success
            )

            val latency = System.currentTimeMillis() - startTime
            _debugInfo.value = _debugInfo.value.copy(
                aiStatus = "COMPLETED",
                latencyMs = latency
            )

            onResponseReady(finalMessage, finalMessage)

        } else {
            // Normal conversational text response
            val responseText = aiResponse.text
            conversationRepository.addMessage(
                conversationId = conversationId,
                role = "ASSISTANT",
                content = responseText,
                isError = aiResponse.isError
            )

            val latency = System.currentTimeMillis() - startTime
            _debugInfo.value = _debugInfo.value.copy(
                aiStatus = if (aiResponse.isError) "ERROR" else "COMPLETED",
                latencyMs = latency
            )

            onResponseReady(responseText, responseText)
        }
    }

    companion object {
        private const val TAG = "AiEngine"
    }
}
