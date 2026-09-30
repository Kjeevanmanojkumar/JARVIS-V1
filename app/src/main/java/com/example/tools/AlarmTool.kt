package com.example.tools

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import java.util.Locale

class AlarmTool(private val context: Context) : JarvisTool {
    override val name = "set_alarm"
    override val description = "Sets a system alarm for a specific hour and minute."
    override val parameters = listOf(
        ToolParameter(
            name = "hour",
            type = "integer",
            description = "Hour of the day in 24-hour format (0 to 23).",
            required = true
        ),
        ToolParameter(
            name = "minutes",
            type = "integer",
            description = "Minute of the hour (0 to 59).",
            required = false
        ),
        ToolParameter(
            name = "label",
            type = "string",
            description = "Optional label or description for the alarm (e.g. 'Morning workout').",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val hourRaw = arguments["hour"]?.toString()?.toIntOrNull()
            ?: return ToolResult(false, "Hour parameter is required (0-23).")
        val minutesRaw = arguments["minutes"]?.toString()?.toIntOrNull() ?: 0
        val label = arguments["label"]?.toString() ?: "JARVIS Alarm"

        if (hourRaw !in 0..23) {
            return ToolResult(false, "Invalid hour: $hourRaw. Must be between 0 and 23.")
        }
        if (minutesRaw !in 0..59) {
            return ToolResult(false, "Invalid minute: $minutesRaw. Must be between 0 and 59.")
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hourRaw)
            putExtra(AlarmClock.EXTRA_MINUTES, minutesRaw)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            val formattedTime = String.format(Locale.US, "%02d:%02d", hourRaw, minutesRaw)
            ToolResult(
                true,
                "Alarm scheduled for $formattedTime with label '$label'.",
                mapOf("hour" to hourRaw, "minutes" to minutesRaw, "label" to label)
            )
        } catch (e: ActivityNotFoundException) {
            ToolResult(false, "No alarm clock application found on this device to handle alarm scheduling.")
        } catch (e: Exception) {
            ToolResult(false, "Error scheduling alarm: ${e.message}")
        }
    }
}
