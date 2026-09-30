package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

class OpenAppTool(private val context: Context) : JarvisTool {
    override val name = "open_app"
    override val description = "Launches an installed Android application by name (e.g. WhatsApp, YouTube, Chrome, Instagram, Spotify, Google Maps, Camera, Settings, Calculator)."
    override val parameters = listOf(
        ToolParameter(
            name = "app_name",
            type = "string",
            description = "The common name of the application to open (e.g., 'WhatsApp', 'YouTube', 'Chrome', 'Instagram', 'Spotify', 'Google Maps', 'Camera', 'Settings', 'Calculator').",
            required = true
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val rawInput = arguments["app_name"]?.toString()?.trim()
            ?: return ToolResult(false, "Application name parameter is required.")

        // Clean query: strip punctuation like trailing periods, exclamation marks, etc.
        val query = rawInput.trimEnd('.', '!', '?', ',', ';').trim().lowercase()
        if (query.isEmpty()) {
            return ToolResult(false, "Application name parameter is empty.")
        }

        val pm = context.packageManager

        // 1. WhatsApp & WhatsApp Business specific handling
        if (query == "whatsapp" || query == "what's app" || query.contains("whatsapp") || query == "wa") {
            // Check consumer WhatsApp first, then WhatsApp Business
            val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            for (pkg in whatsAppPackages) {
                if (isPackageInstalled(pm, pkg)) {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        return try {
                            context.startActivity(launchIntent)
                            val variant = if (pkg == "com.whatsapp.w4b") "WhatsApp Business" else "WhatsApp"
                            ToolResult(true, "Opening $variant.", mapOf("package" to pkg, "app" to variant))
                        } catch (e: Exception) {
                            ToolResult(false, "Failed to launch WhatsApp: ${e.message}")
                        }
                    }
                }
            }
            return ToolResult(false, "WhatsApp is not installed on this device.")
        }

        // 2. Camera special intent handling
        if (query == "camera" || query.contains("camera")) {
            val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(cameraIntent)
                return ToolResult(true, "Opening Camera.", mapOf("action" to "camera"))
            } catch (e: Exception) {
                Log.w(TAG, "Direct camera intent failed, searching installed apps", e)
            }
        }

        // 3. Settings shortcut
        if (query == "settings" || query == "system settings") {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(settingsIntent)
                ToolResult(true, "Opening Settings.", mapOf("action" to "settings"))
            } catch (e: Exception) {
                ToolResult(false, "Failed to launch Settings: ${e.message}")
            }
        }

        // 4. Known aliases & direct packages
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "google chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "instagram" to "com.instagram.android",
            "spotify" to "com.spotify.music",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "calculator" to "com.google.android.calculator",
            "gmail" to "com.google.android.gm",
            "mail" to "com.google.android.gm",
            "photos" to "com.google.android.apps.photos",
            "google photos" to "com.google.android.apps.photos",
            "clock" to "com.google.android.deskclock",
            "calendar" to "com.google.android.calendar",
            "play store" to "com.android.vending",
            "google play" to "com.android.vending",
            "telegram" to "org.telegram.messenger",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android"
        )

        val targetPkg = knownPackages[query]
        if (targetPkg != null && isPackageInstalled(pm, targetPkg)) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(launchIntent)
                    val label = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(targetPkg, 0)).toString()
                    } catch (e: Exception) {
                        rawInput
                    }
                    ToolResult(true, "Opening $label.", mapOf("package" to targetPkg, "app" to label))
                } catch (e: Exception) {
                    ToolResult(false, "Failed to launch $rawInput: ${e.message}")
                }
            }
        }

        // 5. Dynamic search through installed launcher applications
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        // Find best match: exact label match > label starts with > label contains > package contains
        var bestPkg: String? = null
        var bestLabel: String? = null

        for (info in resolveInfos) {
            val appLabel = info.loadLabel(pm).toString()
            val labelLower = appLabel.lowercase()
            val pkgLower = info.activityInfo.packageName.lowercase()

            if (labelLower == query) {
                bestPkg = info.activityInfo.packageName
                bestLabel = appLabel
                break
            }
            if (bestPkg == null && (labelLower.startsWith(query) || labelLower.contains(query))) {
                bestPkg = info.activityInfo.packageName
                bestLabel = appLabel
            }
            if (bestPkg == null && pkgLower.contains(query)) {
                bestPkg = info.activityInfo.packageName
                bestLabel = appLabel
            }
        }

        if (bestPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(bestPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(launchIntent)
                    ToolResult(true, "Opening ${bestLabel ?: rawInput}.", mapOf("package" to bestPkg, "app" to bestLabel))
                } catch (e: Exception) {
                    ToolResult(false, "Failed to launch ${bestLabel ?: rawInput}: ${e.message}")
                }
            }
        }

        return ToolResult(
            false,
            "Application '$rawInput' is not installed or cannot be launched on this device."
        )
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "OpenAppTool"
    }
}
