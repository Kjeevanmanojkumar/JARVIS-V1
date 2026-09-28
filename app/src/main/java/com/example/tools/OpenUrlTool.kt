package com.example.tools

import android.content.Context
import android.content.Intent
import android.net.Uri

class OpenUrlTool(private val context: Context) : JarvisTool {
    override val name = "open_url"
    override val description = "Opens a web URL in the default Android browser (e.g. https://google.com, https://github.com)."
    override val parameters = listOf(
        ToolParameter(
            name = "url",
            type = "string",
            description = "The destination URL or domain name (e.g. 'https://github.com', 'google.com').",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        var url = arguments["url"]?.toString()?.trim()
            ?: return ToolResult(false, "URL parameter is required.")

        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Navigating to $url.", mapOf("url" to url))
        } catch (e: Exception) {
            ToolResult(false, "Failed to open URL: ${e.message}")
        }
    }
}
