package com.example.ai

import com.example.database.entity.MessageEntity
import com.example.tools.ToolDefinition
import java.util.regex.Pattern

class LocalRuleProvider : AiProvider {
    override val providerId: String = "local"
    override val displayName: String = "JARVIS Local Offline Core"

    override suspend fun generateResponse(
        messages: List<MessageEntity>,
        systemPrompt: String,
        tools: List<ToolDefinition>,
        apiKey: String,
        modelName: String
    ): AiResponse {
        val lastUserMessage = messages.lastOrNull { it.role.equals("USER", ignoreCase = true) }?.content?.trim()
            ?: return AiResponse("JARVIS core online. Awaiting your instructions, sir.")

        val input = lastUserMessage.lowercase()

        // 1. Time / Date queries
        if (input.contains("what time") || input.contains("current time") || input.contains("time is it") || input.contains("what's the time")) {
            return AiResponse(
                text = "Checking system clock...",
                toolCall = AiToolCall("get_time_date", mapOf("query_type" to "time"))
            )
        }
        if (input.contains("what date") || input.contains("what day") || input.contains("today's date") || input.contains("what is today")) {
            return AiResponse(
                text = "Checking system calendar...",
                toolCall = AiToolCall("get_time_date", mapOf("query_type" to "date"))
            )
        }

        // 2. Battery / Device status
        if (input.contains("battery") || input.contains("battery percentage") || input.contains("charge level") || input.contains("power level")) {
            return AiResponse(
                text = "Querying power telemetry...",
                toolCall = AiToolCall("get_device_status", mapOf("metric" to "battery"))
            )
        }
        if (input.contains("device status") || input.contains("system status") || input.contains("diagnostics") || input.contains("hardware status")) {
            return AiResponse(
                text = "Scanning device hardware and telemetry...",
                toolCall = AiToolCall("get_device_status", mapOf("metric" to "all"))
            )
        }
        if (input.contains("storage") || input.contains("free space") || input.contains("memory free") || input.contains("disk space")) {
            return AiResponse(
                text = "Analyzing storage partitions...",
                toolCall = AiToolCall("get_device_status", mapOf("metric" to "storage"))
            )
        }

        // 3. Open Settings
        if (input.contains("setting") || input.contains("wifi") || input.contains("wi-fi") || input.contains("bluetooth")) {
            val settingType = when {
                input.contains("wifi") || input.contains("wi-fi") || input.contains("internet") -> "wifi"
                input.contains("bluetooth") -> "bluetooth"
                input.contains("battery") -> "battery"
                input.contains("display") || input.contains("screen") || input.contains("brightness") -> "display"
                input.contains("sound") || input.contains("audio") || input.contains("volume") -> "sound"
                input.contains("app") -> "apps"
                input.contains("storage") -> "storage"
                else -> "system"
            }
            return AiResponse(
                text = "Accessing Android $settingType configuration...",
                toolCall = AiToolCall("open_settings", mapOf("setting_type" to settingType))
            )
        }

        // 4. Timer
        if (input.contains("timer")) {
            var seconds = 600 // default 10 minutes
            val minPattern = Pattern.compile("(\\d+)\\s*(?:minute|min|m)")
            val secPattern = Pattern.compile("(\\d+)\\s*(?:second|sec|s)")

            val minMatcher = minPattern.matcher(input)
            val secMatcher = secPattern.matcher(input)

            if (minMatcher.find()) {
                val mins = minMatcher.group(1)?.toIntOrNull() ?: 10
                seconds = mins * 60
            } else if (secMatcher.find()) {
                seconds = secMatcher.group(1)?.toIntOrNull() ?: 60
            }

            return AiResponse(
                text = "Configuring timer for ${seconds / 60} minute(s)...",
                toolCall = AiToolCall("set_timer", mapOf("duration_seconds" to seconds, "label" to "JARVIS Timer"))
            )
        }

        // 5. Alarm
        if (input.contains("alarm")) {
            var hour = 6
            var min = 0
            val timeRegex = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
            val matcher = timeRegex.matcher(input)
            if (matcher.find()) {
                val rawHour = matcher.group(1)?.toIntOrNull() ?: 6
                min = matcher.group(2)?.toIntOrNull() ?: 0
                val ampm = matcher.group(3)
                hour = if (ampm == "pm" && rawHour < 12) rawHour + 12 else if (ampm == "am" && rawHour == 12) 0 else rawHour
            }
            return AiResponse(
                text = "Programming alarm for $hour:${if (min < 10) "0$min" else "$min"}...",
                toolCall = AiToolCall("set_alarm", mapOf("hour" to hour, "minutes" to min, "label" to "JARVIS Alarm"))
            )
        }

        // 6. App Launch
        if (input.startsWith("open ") || input.startsWith("launch ") || input.startsWith("start ")) {
            val appTarget = input.removePrefix("open ").removePrefix("launch ").removePrefix("start ").trim()
            if (appTarget.isNotEmpty()) {
                // If it's a URL
                if (appTarget.contains(".com") || appTarget.contains(".org") || appTarget.contains(".io") || appTarget.contains("http")) {
                    return AiResponse(
                        text = "Navigating to $appTarget...",
                        toolCall = AiToolCall("open_url", mapOf("url" to appTarget))
                    )
                }
                return AiResponse(
                    text = "Initiating application launch sequence for $appTarget...",
                    toolCall = AiToolCall("open_app", mapOf("app_name" to appTarget))
                )
            }
        }

        // 7. Make Call
        if (input.startsWith("call ") || input.startsWith("dial ")) {
            val target = input.removePrefix("call ").removePrefix("dial ").trim()
            return AiResponse(
                text = "Preparing telecommunication link to $target...",
                toolCall = AiToolCall("make_call", mapOf("phone_number" to target, "direct_call" to false))
            )
        }

        // 8. Media control
        if (input.contains("play music") || input.contains("pause music") || input.contains("next song") || input.contains("previous song") || input.contains("stop music")) {
            val action = when {
                input.contains("pause") || input.contains("stop") -> "pause"
                input.contains("next") || input.contains("skip") -> "next"
                input.contains("previous") || input.contains("back") -> "previous"
                else -> "play"
            }
            return AiResponse(
                text = "Dispatching media command '$action'...",
                toolCall = AiToolCall("media_control", mapOf("action" to action))
            )
        }

        // 9. Web Search
        if (input.startsWith("search ") || input.contains("search the web") || input.startsWith("google ")) {
            val query = input.removePrefix("search the web for ")
                .removePrefix("search the web ")
                .removePrefix("search for ")
                .removePrefix("search ")
                .removePrefix("google ")
                .trim()
            return AiResponse(
                text = "Searching the global network for '$query'...",
                toolCall = AiToolCall("web_search", mapOf("query" to query))
            )
        }

        // 10. Memory commands
        if (input.startsWith("remember that ") || input.startsWith("remember ") || input.startsWith("store ")) {
            val memContent = input.removePrefix("remember that ").removePrefix("remember ").removePrefix("store ").trim()
            // e.g. "my project is called Myraa" or "my name is Bob"
            val isCalledMatcher = Pattern.compile("my (\\w+) is called (.+)", Pattern.CASE_INSENSITIVE).matcher(memContent)
            val isMatcher = Pattern.compile("my (\\w+) is (.+)", Pattern.CASE_INSENSITIVE).matcher(memContent)

            val (k, v, cat) = when {
                isCalledMatcher.find() -> Triple(isCalledMatcher.group(1) ?: "item", isCalledMatcher.group(2) ?: memContent, "PROJECT")
                isMatcher.find() -> Triple(isMatcher.group(1) ?: "item", isMatcher.group(2) ?: memContent, "PERSONAL_CONTEXT")
                else -> Triple("note", memContent, "GENERAL")
            }

            return AiResponse(
                text = "Committing to long-term memory...",
                toolCall = AiToolCall(
                    "manage_memory",
                    mapOf("action" to "save", "key" to k, "value" to v, "category" to cat)
                )
            )
        }

        if (input.contains("what did i tell you about") || input.contains("what is my") || input.contains("what's my") || input.contains("recall")) {
            val key = when {
                input.contains("project") -> "project"
                input.contains("name") -> "name"
                input.contains("car") -> "car"
                else -> ""
            }
            return AiResponse(
                text = "Searching neural memory records...",
                toolCall = AiToolCall("manage_memory", mapOf("action" to "recall", "key" to key))
            )
        }

        if (input.startsWith("forget ") || input.contains("delete memory") || input.contains("purge memory")) {
            val key = when {
                input.contains("project") -> "project"
                input.contains("name") -> "name"
                else -> input.removePrefix("forget ").removePrefix("delete memory ").trim()
            }
            return AiResponse(
                text = "Purging memory entry...",
                toolCall = AiToolCall("manage_memory", mapOf("action" to "forget", "key" to key))
            )
        }

        // Conversational responses
        if (input.contains("hello") || input.contains("hi jarvis") || input.contains("hey jarvis")) {
            return AiResponse("Greetings, sir. JARVIS core is online and all mobile telemetry subroutines are fully operational. How may I be of assistance?")
        }

        if (input.contains("who are you") || input.contains("what is your name")) {
            return AiResponse("I am JARVIS, your Just A Rather Very Intelligent System. I operate as your personal AI command center directly on Android.")
        }

        if (input.contains("what can you do") || input.contains("help") || input.contains("capabilities") || input.contains("features")) {
            return AiResponse("I can execute 10 Android system protocols:\n" +
                    "• Open installed apps ('Open YouTube')\n" +
                    "• Browse web URLs ('Open GitHub')\n" +
                    "• Place phone calls ('Call Mom')\n" +
                    "• Set alarms & timers ('Set a timer for 10 minutes')\n" +
                    "• Open system settings ('Open Wi-Fi settings')\n" +
                    "• Report live battery and device status\n" +
                    "• Report accurate local time & date\n" +
                    "• Control media playback\n" +
                    "• Perform web searches\n" +
                    "• Store & recall long-term memories ('Remember my project is called Myraa')")
        }

        if (input.contains("thank you") || input.contains("thanks")) {
            return AiResponse("Always at your service, sir.")
        }

        return AiResponse("Command acknowledged: '$lastUserMessage'. I am currently operating in local mode. You can connect a Gemini API key in Settings for expanded natural language capabilities.")
    }
}
