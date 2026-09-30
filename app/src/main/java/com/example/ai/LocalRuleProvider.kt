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

        val rawClean = lastUserMessage.trimEnd('.', '!', '?', ',', ';').trim()
        val input = rawClean.lowercase()

        // 0. Stop speaking command
        if (input == "stop speaking" || input == "stop talking" || input == "silence" || input == "quiet") {
            return AiResponse("Speech synthesis halted.")
        }

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
        if (input.contains("battery") || input.contains("charge level") || input.contains("power level") || input.contains("battery percentage")) {
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

        // 3. Settings specific commands (Wi-Fi, Bluetooth, etc.)
        if (input.contains("wi-fi") || input.contains("wifi") || input.contains("bluetooth") || (input.contains("setting") && !input.contains("remember"))) {
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

        // 4. Timer commands
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

        // 5. Alarm commands
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

        // 6. Media control commands (Pause, Play, Next, Previous, etc.)
        if (input == "pause" || input == "pause music" || input == "stop music" ||
            input == "play" || input == "play music" || input == "play my music" ||
            input == "next" || input == "next song" || input == "skip" ||
            input == "previous" || input == "previous song" || input == "back"
        ) {
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

        // 7. Web Search commands
        if (input.startsWith("search ") || input.contains("search the web") || input.startsWith("google ")) {
            val prefixRegex = "(?i)^(?:search\\s+the\\s+web\\s+for\\s+|search\\s+the\\s+web\\s+|search\\s+for\\s+|search\\s+|google\\s+)".toRegex()
            val query = rawClean.replace(prefixRegex, "").trim()
            return AiResponse(
                text = "Searching the web for '$query'...",
                toolCall = AiToolCall("web_search", mapOf("query" to query))
            )
        }

        // 8. Memory: Store / Remember
        if (input.startsWith("remember that ") || input.startsWith("remember ") || input.startsWith("store ")) {
            val prefixRegex = "(?i)^(?:remember\\s+that\\s+|remember\\s+|store\\s+)".toRegex()
            val rawMemContent = rawClean.replace(prefixRegex, "").trim()

            // Handle patterns like:
            // "my android project is called JARVIS"
            // "my project is called JARVIS"
            // "my name is Bob"
            val isCalledMatcher = Pattern.compile("my (.+?) is called (.+)", Pattern.CASE_INSENSITIVE).matcher(rawMemContent)
            val isNamedMatcher = Pattern.compile("my (.+?) is named (.+)", Pattern.CASE_INSENSITIVE).matcher(rawMemContent)
            val isMatcher = Pattern.compile("my (.+?) is (.+)", Pattern.CASE_INSENSITIVE).matcher(rawMemContent)

            val (k, v, cat) = when {
                isCalledMatcher.find() -> Triple(isCalledMatcher.group(1)?.trim() ?: "item", isCalledMatcher.group(2)?.trim() ?: rawMemContent, "PROJECT")
                isNamedMatcher.find() -> Triple(isNamedMatcher.group(1)?.trim() ?: "item", isNamedMatcher.group(2)?.trim() ?: rawMemContent, "PROJECT")
                isMatcher.find() -> Triple(isMatcher.group(1)?.trim() ?: "item", isMatcher.group(2)?.trim() ?: rawMemContent, "PERSONAL_CONTEXT")
                else -> Triple("note", rawMemContent, "GENERAL")
            }

            return AiResponse(
                text = "Committing to long-term memory...",
                toolCall = AiToolCall(
                    "manage_memory",
                    mapOf("action" to "save", "key" to k, "value" to v, "category" to cat)
                )
            )
        }

        // 9. Memory: Recall
        if (input.contains("what did i tell you about") || input.contains("what is my") || input.contains("what's my") || input.startsWith("recall ")) {
            val key = when {
                input.contains("android project") -> "Android project"
                input.contains("project") -> "project"
                input.contains("name") -> "name"
                input.contains("car") -> "car"
                else -> input.removePrefix("what is my ").removePrefix("what's my ").removePrefix("recall ").replace(" called", "").replace(" name", "").trim()
            }
            return AiResponse(
                text = "Searching neural memory records...",
                toolCall = AiToolCall("manage_memory", mapOf("action" to "recall", "key" to key))
            )
        }

        // 10. Memory: Forget / Delete
        if (input.startsWith("forget ") || input.contains("delete memory") || input.contains("purge memory")) {
            val key = when {
                input.contains("android project") -> "Android project"
                input.contains("project") -> "project"
                input.contains("name") -> "name"
                else -> input.removePrefix("forget ").removePrefix("delete memory ").replace(" name", "").trim()
            }
            return AiResponse(
                text = "Purging memory entry...",
                toolCall = AiToolCall("manage_memory", mapOf("action" to "forget", "key" to key))
            )
        }

        // 11. App Launch & URLs
        if (input.startsWith("open ") || input.startsWith("launch ") || input.startsWith("start ")) {
            val appTarget = input.removePrefix("open ").removePrefix("launch ").removePrefix("start ").trim()
            if (appTarget.isNotEmpty()) {
                // If it's a known URL or web target
                if (appTarget == "google" || appTarget == "github" || appTarget.contains(".com") || appTarget.contains(".org") || appTarget.contains(".io") || appTarget.contains("http")) {
                    val url = when (appTarget) {
                        "google" -> "https://google.com"
                        "github" -> "https://github.com"
                        else -> appTarget
                    }
                    return AiResponse(
                        text = "Navigating to $url...",
                        toolCall = AiToolCall("open_url", mapOf("url" to url))
                    )
                }
                return AiResponse(
                    text = "Initiating application launch sequence for $appTarget...",
                    toolCall = AiToolCall("open_app", mapOf("app_name" to appTarget))
                )
            }
        }

        // 12. Make Call
        if (input.startsWith("call ") || input.startsWith("dial ")) {
            val target = input.removePrefix("call ").removePrefix("dial ").trim()
            return AiResponse(
                text = "Preparing telecommunication link to $target...",
                toolCall = AiToolCall("make_call", mapOf("phone_number" to target, "direct_call" to false))
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
                    "• Open installed apps ('Open WhatsApp', 'Open YouTube', 'Open Chrome')\n" +
                    "• Browse web URLs ('Open GitHub')\n" +
                    "• Place phone calls ('Call Mom')\n" +
                    "• Set alarms & timers ('Set a timer for 10 minutes', 'Set an alarm for 6 AM')\n" +
                    "• Open system settings ('Open Wi-Fi settings', 'Open Bluetooth settings')\n" +
                    "• Report live battery and device status\n" +
                    "• Report accurate local time & date\n" +
                    "• Control media playback ('Pause', 'Play', 'Next')\n" +
                    "• Perform web searches ('Search the web for Kotlin coroutines')\n" +
                    "• Store & recall long-term memories ('Remember my Android project is called JARVIS')")
        }

        if (input.contains("thank you") || input.contains("thanks")) {
            return AiResponse("Always at your service, sir.")
        }

        return AiResponse("Command acknowledged: '$lastUserMessage'. I am currently operating in local mode. You can connect a Gemini API key in Settings for expanded natural language capabilities.")
    }
}
