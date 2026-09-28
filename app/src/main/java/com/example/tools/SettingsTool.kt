package com.example.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class SettingsTool(private val context: Context) : JarvisTool {
    override val name = "open_settings"
    override val description = "Opens an Android system settings panel (wifi, bluetooth, battery, display, sound, apps, storage, or main settings)."
    override val parameters = listOf(
        ToolParameter(
            name = "setting_type",
            type = "string",
            description = "The specific settings panel to open: 'wifi', 'bluetooth', 'battery', 'display', 'sound', 'apps', 'storage', 'location', or 'system'.",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val type = arguments["setting_type"]?.toString()?.lowercase()?.trim() ?: "system"

        val action = when (type) {
            "wifi", "wi-fi", "internet" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "battery", "power" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            "display", "screen", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "audio", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "apps", "applications" -> Settings.ACTION_APPLICATION_SETTINGS
            "storage" -> Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            "location", "gps" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            "date", "time" -> Settings.ACTION_DATE_SETTINGS
            "security" -> Settings.ACTION_SECURITY_SETTINGS
            "app_info" -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return try {
                    context.startActivity(intent)
                    ToolResult(true, "Opened app details settings.")
                } catch (e: Exception) {
                    ToolResult(false, "Could not open app settings: ${e.message}")
                }
            }
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(true, "Navigating to Android $type settings.", mapOf("setting" to type))
        } catch (e: Exception) {
            ToolResult(false, "Could not open $type settings: ${e.message}")
        }
    }
}
