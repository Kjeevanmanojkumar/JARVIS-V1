package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DebugInfo
import com.example.model.JarvisState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@Composable
fun DebugOverlay(
    state: JarvisState,
    debugInfo: DebugInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisGlassSurface.copy(alpha = 0.95f), shape = RoundedCornerShape(4.dp))
            .border(1.dp, JarvisWarningAmber.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Row {
            Text(
                text = "⚡ TELEMETRY DEBUGGER",
                color = JarvisWarningAmber,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${debugInfo.latencyMs}ms",
                color = JarvisCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        DebugRow(label = "STATE", value = state.name, color = JarvisCyan)
        DebugRow(label = "PROVIDER", value = debugInfo.activeProvider.ifEmpty { "Default" })
        DebugRow(label = "SPEECH IN", value = debugInfo.lastRecognizedText.ifEmpty { "(none)" })
        DebugRow(label = "AI STATUS", value = debugInfo.aiStatus)

        if (debugInfo.selectedTool != null) {
            DebugRow(label = "TOOL", value = debugInfo.selectedTool, color = JarvisWarningAmber)
        }
        if (debugInfo.toolArguments != null) {
            DebugRow(label = "ARGS", value = debugInfo.toolArguments)
        }
        if (debugInfo.toolResult != null) {
            DebugRow(label = "RESULT", value = debugInfo.toolResult)
        }
        if (debugInfo.errorMessage != null) {
            DebugRow(label = "ERROR", value = debugInfo.errorMessage, color = JarvisAlertRed)
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String, color: Color = JarvisTextPrimary) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        Text(
            text = "$label: ",
            color = JarvisTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(68.dp)
        )
        Text(
            text = value,
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 2
        )
    }
}
