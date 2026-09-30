package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisState
import com.example.model.SystemTelemetry
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisOnlineGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@Composable
fun HudStatusBar(
    telemetry: SystemTelemetry,
    state: JarvisState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_dot_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    val onlineColor = if (telemetry.isOnline) JarvisOnlineGreen else JarvisAlertRed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Top Row: JARVIS Brand & Online State, Clock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "JARVIS",
                    color = JarvisTextPrimary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Glowing status dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(onlineColor.copy(alpha = dotAlpha), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (telemetry.isOnline) "ONLINE" else "OFFLINE",
                    color = onlineColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Right side: Local Time & Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = telemetry.currentTimeString.ifEmpty { "--:--" },
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                if (telemetry.currentDateString.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${telemetry.currentDateString}",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Bottom compact status line: Battery & Network & Core State
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (telemetry.batteryPct < 20) JarvisAlertRed else JarvisCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "BAT: ${telemetry.batteryPct}%",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = if (telemetry.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Network",
                    tint = if (telemetry.isOnline) JarvisCyan else JarvisAlertRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NET: ${telemetry.networkType}",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Current Core State Badge
            val stateColor = when (state) {
                JarvisState.IDLE -> JarvisCyan
                JarvisState.LISTENING -> Color.White
                JarvisState.THINKING, JarvisState.PROCESSING -> JarvisCyan
                JarvisState.SPEAKING -> JarvisOnlineGreen
                JarvisState.EXECUTING -> JarvisWarningAmber
                JarvisState.ERROR -> JarvisAlertRed
            }
            Text(
                text = "[${state.name}]",
                color = stateColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Thin tech separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .size(height = 1.dp, width = 100.dp)
                .background(JarvisGlassBorder)
        )
    }
}
