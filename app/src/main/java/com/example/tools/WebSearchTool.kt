package com.example.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

class WebSearchTool(private val context: Context) : JarvisTool {
    override val name = "web_search"
    override val description = "Conducts a web search for a given topic or query via Android search or web browser."
    override val parameters = listOf(
        ToolParameter(
            name = "query",
            type = "string",
            description = "The search query string to look up.",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val query = arguments["query"]?.toString()?.trim()
            ?: return ToolResult(false, "Search query parameter is required.")

        return try {
            // First try system web search action
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (searchIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(searchIntent)
                ToolResult(true, "Initiating web search for '$query'.", mapOf("query" to query))
            } else {
                // Fallback to browser search URL
                val encoded = URLEncoder.encode(query, "UTF-8")
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ToolResult(true, "Opening web search for '$query' in browser.", mapOf("query" to query))
            }
        } catch (e: Exception) {
            ToolResult(false, "Failed to launch web search: ${e.message}")
        }
    }
}
