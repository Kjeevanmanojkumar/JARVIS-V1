package com.example.tools

import com.example.database.repository.MemoryRepository
import com.example.model.MemoryCategory

class ManageMemoryTool(private val memoryRepository: MemoryRepository) : JarvisTool {
    override val name = "manage_memory"
    override val description = "Stores, updates, recalls, or removes persistent long-term memory entries for the user."
    override val parameters = listOf(
        ToolParameter(
            name = "action",
            type = "string",
            description = "The memory operation: 'save', 'recall', 'forget', or 'list'.",
            required = true
        ),
        ToolParameter(
            name = "key",
            type = "string",
            description = "The subject or identifier of the memory (e.g. 'project', 'user_name', 'favorite_color', 'car').",
            required = false
        ),
        ToolParameter(
            name = "value",
            type = "string",
            description = "The information to remember (required when action is 'save').",
            required = false
        ),
        ToolParameter(
            name = "category",
            type = "string",
            description = "Optional category: 'USER_PREFERENCE', 'PROJECT', 'PERSONAL_CONTEXT', 'TASK', or 'GENERAL'.",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val action = arguments["action"]?.toString()?.lowercase() ?: "recall"
        val key = arguments["key"]?.toString()?.trim() ?: ""
        val value = arguments["value"]?.toString()?.trim() ?: ""
        val categoryStr = arguments["category"]?.toString() ?: "GENERAL"
        val category = MemoryCategory.fromString(categoryStr)

        return when (action) {
            "save", "remember", "store" -> {
                if (key.isEmpty() || value.isEmpty()) {
                    ToolResult(false, "Both 'key' and 'value' are required to store a memory.")
                } else {
                    memoryRepository.saveMemory(key, value, category)
                    ToolResult(
                        true,
                        "Memory stored: remembered that '$key' is '$value' under ${category.displayName}.",
                        mapOf("key" to key, "value" to value, "category" to category.name)
                    )
                }
            }
            "recall", "retrieve", "get" -> {
                if (key.isEmpty()) {
                    val all = memoryRepository.getAllMemoriesList()
                    val summary = if (all.isEmpty()) "No memories stored yet." else all.joinToString("; ") { "${it.memoryKey}: ${it.memoryValue}" }
                    ToolResult(true, "Stored memories: $summary", mapOf("count" to all.size))
                } else {
                    val mem = memoryRepository.getMemory(key)
                    if (mem != null) {
                        ToolResult(
                            true,
                            "Recalled memory for '$key': ${mem.memoryValue} (${mem.category}).",
                            mapOf("key" to key, "value" to mem.memoryValue, "category" to mem.category)
                        )
                    } else {
                        // Search partial
                        val related = memoryRepository.getRelevantMemories(key, limit = 3)
                        if (related.isNotEmpty()) {
                            val relSummary = related.joinToString(", ") { "${it.memoryKey}=${it.memoryValue}" }
                            ToolResult(true, "Found related memories: $relSummary", mapOf("matches" to related.size))
                        } else {
                            ToolResult(false, "No record found in memory for '$key'.")
                        }
                    }
                }
            }
            "forget", "delete", "remove" -> {
                if (key.isEmpty()) {
                    ToolResult(false, "Key parameter is required to forget a memory.")
                } else {
                    val deleted = memoryRepository.deleteMemoryByKey(key)
                    if (deleted) {
                        ToolResult(true, "Successfully purged memory for '$key'.", mapOf("key" to key))
                    } else {
                        ToolResult(false, "Could not find any memory matching '$key' to delete.")
                    }
                }
            }
            "list" -> {
                val list = memoryRepository.getAllMemoriesList()
                val summary = if (list.isEmpty()) "Memory bank is currently empty."
                else list.joinToString("\n") { "• [${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
                ToolResult(true, summary, mapOf("count" to list.size))
            }
            else -> ToolResult(false, "Unknown memory action: '$action'. Supported: save, recall, forget, list.")
        }
    }
}
