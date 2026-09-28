package com.example.tools

data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>
)

data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any?>? = null
)

interface JarvisTool {
    val name: String
    val description: String
    val parameters: List<ToolParameter>

    suspend fun execute(arguments: Map<String, Any?>): ToolResult

    fun getDefinition(): ToolDefinition = ToolDefinition(name, description, parameters)
}
