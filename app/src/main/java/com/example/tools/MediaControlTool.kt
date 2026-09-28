package com.example.tools

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class MediaControlTool(private val context: Context) : JarvisTool {
    override val name = "media_control"
    override val description = "Controls system media playback: play, pause, play_pause, next track, or previous track."
    override val parameters = listOf(
        ToolParameter(
            name = "action",
            type = "string",
            description = "Media action to execute: 'play', 'pause', 'play_pause', 'next', or 'previous'.",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val action = arguments["action"]?.toString()?.lowercase()?.trim() ?: "play_pause"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ToolResult(false, "System audio service is not available.")

        val keyCode = when (action) {
            "play" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "pause", "stop" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            "play_pause", "toggle" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            "next", "skip" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "previous", "prev", "back" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }

        return try {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)

            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)

            ToolResult(
                true,
                "Media command '$action' dispatched to Android media subsystem.",
                mapOf("action" to action, "keyCode" to keyCode)
            )
        } catch (e: Exception) {
            ToolResult(false, "Failed to dispatch media command: ${e.message}")
        }
    }
}
