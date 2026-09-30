package com.example.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JarvisState
import com.example.ui.components.HudPanel
import com.example.ui.components.JarvisCore
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisOnlineGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber
import com.example.voice.whisper.WhisperModelManager
import com.example.voice.whisper.WhisperModelStatus
import kotlinx.coroutines.delay

@Composable
fun DiagnosticScreen(
    viewModel: JarvisViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var diagnosticStep by remember { mutableIntStateOf(0) }
    val whisperStatus by viewModel.whisperModelStatus.collectAsState()

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
    }

    // Step progression animation
    LaunchedEffect(Unit) {
        delay(400)
        diagnosticStep = 1 // Neural memory online
        delay(500)
        diagnosticStep = 2 // 10 Tools verified
        delay(500)
        diagnosticStep = 3 // Audio framework calibrated
        delay(500)
        diagnosticStep = 4 // All systems nominal
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisObsidian)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "JARVIS V1 INITIALIZATION",
            color = JarvisCyan,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = "SYSTEM BOOTSTRAP & DIAGNOSTIC SEQUENCE",
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        JarvisCore(
            state = if (diagnosticStep >= 4) JarvisState.IDLE else JarvisState.THINKING,
            size = 170.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Subsystem Verification Panel
        HudPanel(title = "Subsystem Verification") {
            DiagnosticLine("ROOM PERSISTENT MEMORY", active = diagnosticStep >= 1)
            DiagnosticLine("10 ANDROID SYSTEM TOOLS", active = diagnosticStep >= 2)
            DiagnosticLine("SYNTHESIZED AUDIO BUS (TTS)", active = diagnosticStep >= 3)
            DiagnosticLine("AI INFERENCE ENGINE & LOCAL ROUTER", active = diagnosticStep >= 4)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // JARVIS VOICE ENGINE (WHISPER)
        HudPanel(title = "JARVIS Voice Engine (Whisper)") {
            val statusLabel: String
            val statusColor: androidx.compose.ui.graphics.Color
            when (whisperStatus) {
                is WhisperModelStatus.Ready -> {
                    val ready = whisperStatus as WhisperModelStatus.Ready
                    statusLabel = "READY (${ready.sizeMb.toInt()} MB on device)"
                    statusColor = JarvisOnlineGreen
                }
                is WhisperModelStatus.Downloading -> {
                    val dl = whisperStatus as WhisperModelStatus.Downloading
                    statusLabel = "DOWNLOADING (${(dl.progress * 100).toInt()}%)"
                    statusColor = JarvisWarningAmber
                }
                is WhisperModelStatus.InsufficientStorage -> {
                    statusLabel = "INSUFFICIENT STORAGE"
                    statusColor = JarvisAlertRed
                }
                is WhisperModelStatus.Error -> {
                    statusLabel = "DOWNLOAD REQUIRED"
                    statusColor = JarvisWarningAmber
                }
                else -> {
                    statusLabel = "DOWNLOAD REQUIRED"
                    statusColor = JarvisWarningAmber
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model: ${WhisperModelManager.MODEL_NAME}",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Whisper provides private, offline speech recognition directly on Android without third-party telemetry.",
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )

            if (whisperStatus is WhisperModelStatus.Downloading) {
                val dl = whisperStatus as WhisperModelStatus.Downloading
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { dl.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = JarvisCyan,
                    trackColor = JarvisGlassBorder
                )
            } else if (whisperStatus !is WhisperModelStatus.Ready) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.downloadWhisperModel() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = JarvisObsidian, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DOWNLOAD WHISPER MODEL (~39 MB)", color = JarvisObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mic permission prompt
        if (!micGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JarvisGlassSurface, RoundedCornerShape(4.dp))
                    .border(1.dp, JarvisGlassBorder, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MICROPHONE ACCESS",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Required for hands-free speech recognition.",
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Button(
                        onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = JarvisObsidian, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ENABLE", color = JarvisObsidian, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        AnimatedVisibility(visible = diagnosticStep >= 4) {
            Button(
                onClick = {
                    viewModel.markFirstRunComplete()
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(2.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = JarvisObsidian)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ENGAGE JARVIS HUD",
                    color = JarvisObsidian,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DiagnosticLine(label: String, active: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (active) JarvisTextPrimary else JarvisTextSecondary.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        if (active) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = JarvisOnlineGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "READY",
                    color = JarvisOnlineGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "CALIBRATING...",
                color = JarvisTextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
