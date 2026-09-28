package com.example.ai

import com.example.database.entity.MemoryEntity
import com.example.database.entity.MessageEntity
import com.example.tools.ToolDefinition

object PromptManager {

    const val DEFAULT_SYSTEM_PROMPT = """You are JARVIS, an advanced mobile AI system operating directly on an Android device.
Your job is to assist the user with precision, intelligence, and a calm, professional demeanor.

GUIDELINES:
1. Always be concise, articulate, and direct. Avoid unnecessary conversational filler.
2. When the user requests an Android device action (such as opening apps, checking device battery, setting alarms or timers, opening settings, media playback, web search, or recalling/saving memories), you MUST trigger the appropriate tool.
3. To trigger a tool, your output MUST begin with a tool call in the exact JSON format:
{"tool": "<tool_name>", "arguments": {<key>: <value>}}
4. If no tool is needed, respond directly with natural, helpful text.
5. Never claim an action succeeded unless you have verified it.
6. Use remembered information from long-term memory when relevant.
7. If a tool fails or is unavailable, report the status truthfully."""

    fun buildSystemInstruction(
        customPrompt: String?,
        memories: List<MemoryEntity>,
        tools: List<ToolDefinition>
    ): String {
        val basePrompt = if (!customPrompt.isNullOrBlank()) customPrompt else DEFAULT_SYSTEM_PROMPT

        val memorySection = if (memories.isNotEmpty()) {
            val listStr = memories.joinToString("\n") { "• [${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
            "\n\n[PERSISTENT LONG-TERM MEMORY]:\n$listStr"
        } else {
            ""
        }

        val toolSection = if (tools.isNotEmpty()) {
            val toolList = tools.joinToString("\n\n") { tool ->
                val paramList = tool.parameters.joinToString(", ") { "${it.name} (${it.type}${if (it.required) ", required" else ""}): ${it.description}" }
                "- Tool '${tool.name}': ${tool.description}\n  Parameters: $paramList"
            }
            "\n\n[AVAILABLE ANDROID TOOLS]:\n$toolList\n\nTo execute a tool, reply ONLY with:\n{\"tool\": \"<tool_name>\", \"arguments\": {\"<arg_name>\": <value>}}"
        } else {
            ""
        }

        return "$basePrompt$memorySection$toolSection"
    }

    fun formatHistoryForPrompt(messages: List<MessageEntity>): String {
        return messages.joinToString("\n") { msg ->
            when (msg.role.uppercase()) {
                "USER" -> "User: ${msg.content}"
                "ASSISTANT" -> "JARVIS: ${msg.content}"
                "TOOL" -> "System [Tool ${msg.toolName}]: ${msg.toolResult ?: msg.content}"
                else -> "${msg.role}: ${msg.content}"
            }
        }
    }
}
