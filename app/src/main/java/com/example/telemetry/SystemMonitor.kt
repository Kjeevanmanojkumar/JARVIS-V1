package com.example.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.model.SystemTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemMonitor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private var timeTickerJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            updateBattery()
        }
    }

    fun start() {
        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Register network listener
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateNetwork(true)
                }

                override fun onLost(network: Network) {
                    updateNetwork(false)
                }
            }
            try {
                cm.registerNetworkCallback(request, networkCallback!!)
            } catch (ignored: Exception) {}
        }

        // Static hardware values
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val androidVer = "Android ${Build.VERSION.RELEASE}"
        var freeStorageMb = 0L
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            freeStorageMb = stat.availableBytes / (1024 * 1024)
        } catch (ignored: Exception) {}

        _telemetry.value = _telemetry.value.copy(
            deviceModel = deviceModel,
            androidVersion = androidVer,
            availableStorageMb = freeStorageMb
        )

        updateBattery()
        updateNetwork(checkNetworkState())

        // Time ticker: updates once every 5 seconds to keep clock accurate without high CPU
        timeTickerJob = scope.launch(Dispatchers.Default) {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            while (isActive) {
                val now = Date()
                _telemetry.value = _telemetry.value.copy(
                    currentTimeString = timeFmt.format(now),
                    currentDateString = dateFmt.format(now)
                )
                delay(5000)
            }
        }
    }

    private fun updateBattery() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val statusIntent = context.registerReceiver(null, filter)
        val level = statusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = statusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

        val status = statusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        _telemetry.value = _telemetry.value.copy(
            batteryPct = pct,
            isCharging = isCharging
        )
    }

    private fun checkNetworkState(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun updateNetwork(isOnline: Boolean) {
        val netType = if (isOnline) "ONLINE" else "OFFLINE"
        _telemetry.value = _telemetry.value.copy(
            isOnline = isOnline,
            networkType = netType
        )
    }

    fun stop() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (ignored: Exception) {}

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkCallback?.let {
            try {
                cm?.unregisterNetworkCallback(it)
            } catch (ignored: Exception) {}
        }
        timeTickerJob?.cancel()
    }
}
