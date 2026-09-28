package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class OpenAppTool(private val context: Context) : JarvisTool {
    override val name = "open_app"
    override val description = "Launches an installed Android application by name (e.g. YouTube, Chrome, Settings, Camera, Maps, Spotify, Calculator)."
    override val parameters = listOf(
        ToolParameter(
            name = "app_name",
            type = "string",
            description = "The common name of the application to open (e.g., 'YouTube', 'Chrome', 'Settings', 'Camera', 'Maps', 'Calculator').",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val appName = arguments["app_name"]?.toString()?.trim()
            ?: return ToolResult(false, "Application name parameter is required.")

        val pm = context.packageManager
        val query = appName.lowercase()

        // Known direct package shortcuts for speed and accuracy
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "mail" to "com.google.android.gm",
            "camera" to "camera_fallback",
            "settings" to "com.android.settings",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "calendar" to "com.google.android.calendar",
            "photos" to "com.google.android.apps.photos",
            "play store" to "com.android.vending",
            "spotify" to "com.spotify.music"
        )

        // Try direct shortcut if available
        val directPkg = packageMap[query]
        if (directPkg != null && directPkg != "camera_fallback") {
            val intent = pm.getLaunchIntentForPackage(directPkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ToolResult(true, "Launching $appName.")
            }
        }

        // Special handling for camera
        if (query.contains("camera")) {
            val cameraIntent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (cameraIntent.resolveActivity(pm) != null) {
                context.startActivity(cameraIntent)
                return ToolResult(true, "Launching Camera.")
            }
        }

        // Search installed launcher apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            val pkg = info.activityInfo.packageName.lowercase()

            if (label.contains(query) || query.contains(label) || pkg.contains(query)) {
                val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ToolResult(true, "Launching ${info.loadLabel(pm)}.")
                }
            }
        }

        return ToolResult(false, "Application '$appName' could not be found or launched on this device.")
    }
}
