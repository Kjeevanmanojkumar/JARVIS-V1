package com.example.model

data class SystemTelemetry(
    val batteryPct: Int = 100,
    val isCharging: Boolean = false,
    val isOnline: Boolean = true,
    val networkType: String = "CONNECTED",
    val androidVersion: String = "Android",
    val deviceModel: String = "Device",
    val availableStorageMb: Long = 0L,
    val currentTimeString: String = "",
    val currentDateString: String = ""
)
