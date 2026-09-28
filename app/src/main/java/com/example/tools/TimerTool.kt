package com.example.tools

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

class TimerTool(private val context: Context) : JarvisTool {
    override val name = "set_timer"
    override val description = "Sets an Android countdown timer for a specified duration in seconds or minutes."
    override val parameters = listOf(
        ToolParameter(
            name = "duration_seconds",
            type = "integer",
            description = "Timer duration in seconds (e.g., 600 for 10 minutes, 300 for 5 minutes).",
            required = true
        ),
        ToolParameter(
            name = "label",
            type = "string",
            description = "Optional label or message for the countdown timer.",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val seconds = arguments["duration_seconds"]?.toString()?.toIntOrNull()
            ?: return ToolResult(false, "duration_seconds parameter is required.")
        val label = arguments["label"]?.toString() ?: "JARVIS Timer"

        if (seconds <= 0) {
            return ToolResult(false, "Timer duration must be greater than 0 seconds.")
        }

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                val minutes = seconds / 60
                val remainingSec = seconds % 60
                val durationText = when {
                    minutes > 0 && remainingSec > 0 -> "$minutes minute(s) and $remainingSec second(s)"
                    minutes > 0 -> "$minutes minute(s)"
                    else -> "$remainingSec second(s)"
                }
                ToolResult(
                    true,
                    "Timer successfully scheduled for $durationText.",
                    mapOf("seconds" to seconds, "label" to label)
                )
            } else {
                ToolResult(false, "No compatible clock/timer application installed on device.")
            }
        } catch (e: Exception) {
            ToolResult(false, "Error setting timer: ${e.message}")
        }
    }
}
