package com.example.tools

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeDateTool : JarvisTool {
    override val name = "get_time_date"
    override val description = "Provides the current local device time, date, day of the week, and timezone."
    override val parameters = listOf(
        ToolParameter(
            name = "query_type",
            type = "string",
            description = "Optional filter: 'time', 'date', or 'both' (default).",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val queryType = arguments["query_type"]?.toString()?.lowercase() ?: "both"
        val now = Date()

        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val shortTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val tzFormat = SimpleDateFormat("z", Locale.getDefault())

        val currentTime = timeFormat.format(now)
        val shortTime = shortTimeFormat.format(now)
        val currentDate = dateFormat.format(now)
        val timezone = tzFormat.format(now)

        val message = when (queryType) {
            "time" -> "The current local time is $shortTime ($timezone)."
            "date" -> "Today is $currentDate."
            else -> "The current time is $shortTime ($timezone). Today is $currentDate."
        }

        return ToolResult(
            true,
            message,
            mapOf(
                "time" to currentTime,
                "shortTime" to shortTime,
                "date" to currentDate,
                "timezone" to timezone,
                "timestamp" to now.time
            )
        )
    }
}
