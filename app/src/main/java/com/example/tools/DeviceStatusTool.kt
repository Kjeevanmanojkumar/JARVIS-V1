package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.util.Locale

class DeviceStatusTool(private val context: Context) : JarvisTool {
    override val name = "get_device_status"
    override val description = "Retrieves real-time Android device diagnostics: battery percentage, charging state, device model, Android OS version, storage capacity, and network connectivity."
    override val parameters = listOf(
        ToolParameter(
            name = "metric",
            type = "string",
            description = "Optional filter: 'battery', 'device', 'storage', 'network', or 'all' (default).",
            required = false
        )
    )

    override suspend fun execute(arguments: Map<String, Any?>): ToolResult {
        val metric = arguments["metric"]?.toString()?.lowercase() ?: "all"

        // 1. Battery metrics
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, batteryFilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Int = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Power Source" else "Discharging"
        }

        // 2. Storage metrics
        var freeStorageGb = 0.0
        var totalStorageGb = 0.0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBytes
            val bytesTotal = stat.totalBytes
            freeStorageGb = bytesAvailable.toDouble() / (1024 * 1024 * 1024)
            totalStorageGb = bytesTotal.toDouble() / (1024 * 1024 * 1024)
        } catch (ignored: Exception) {}

        // 3. Network connectivity
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)
        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val netType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular LTE/5G"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> if (isOnline) "Connected" else "Offline"
        }

        // 4. Hardware and OS
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val dataMap = mapOf<String, Any?>(
            "batteryPct" to batteryPct,
            "isCharging" to isCharging,
            "powerSource" to powerSource,
            "deviceModel" to deviceModel,
            "androidVersion" to androidVersion,
            "storageFreeGb" to String.format(Locale.US, "%.1f", freeStorageGb),
            "storageTotalGb" to String.format(Locale.US, "%.1f", totalStorageGb),
            "network" to netType,
            "isOnline" to isOnline
        )

        val message = when (metric) {
            "battery" -> {
                val chargeMsg = if (isCharging) "charging via $powerSource" else "discharging"
                "Battery is at $batteryPct%, currently $chargeMsg."
            }
            "storage" -> {
                "Internal storage: ${String.format(Locale.US, "%.1f", freeStorageGb)} GB free of ${String.format(Locale.US, "%.1f", totalStorageGb)} GB."
            }
            "network" -> {
                "Network status: $netType (${if (isOnline) "Online" else "Disconnected"})."
            }
            "device" -> {
                "Device: $deviceModel running $androidVersion."
            }
            else -> {
                val chargeMsg = if (isCharging) "charging ($powerSource)" else "not charging"
                "Device Telemetry: $deviceModel | $androidVersion | Battery: $batteryPct% ($chargeMsg) | Storage: ${String.format(Locale.US, "%.1f", freeStorageGb)}GB/${String.format(Locale.US, "%.1f", totalStorageGb)}GB free | Network: $netType."
            }
        }

        return ToolResult(true, message, dataMap)
    }
}
