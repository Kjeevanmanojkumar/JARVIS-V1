package com.example.tools

import android.content.Context
import com.example.database.repository.MemoryRepository

class ToolRegistry(
    context: Context,
    memoryRepository: MemoryRepository
) {
    private val tools = mutableMapOf<String, JarvisTool>()

    init {
        registerTool(OpenAppTool(context))
        registerTool(OpenUrlTool(context))
        registerTool(CallTool(context))
        registerTool(AlarmTool(context))
        registerTool(TimerTool(context))
        registerTool(SettingsTool(context))
        registerTool(DeviceStatusTool(context))
        registerTool(TimeDateTool())
        registerTool(MediaControlTool(context))
        registerTool(WebSearchTool(context))
        registerTool(ManageMemoryTool(memoryRepository))
    }

    private fun registerTool(tool: JarvisTool) {
        tools[tool.name] = tool
    }

    fun getAllTools(): List<JarvisTool> = tools.values.toList()

    fun getToolDefinitions(): List<ToolDefinition> = tools.values.map { it.getDefinition() }

    fun getTool(name: String): JarvisTool? = tools[name]

    suspend fun executeTool(name: String, arguments: Map<String, Any?>): ToolResult {
        val tool = tools[name] ?: return ToolResult(false, "Unknown tool: '$name'")
        return try {
            tool.execute(arguments)
        } catch (e: Exception) {
            ToolResult(false, "Exception while executing tool '$name': ${e.message}")
        }
    }
}
