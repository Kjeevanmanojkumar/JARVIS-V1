package com.example.ui.hud

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JarvisState
import com.example.ui.components.DebugOverlay
import com.example.ui.components.HudPanel
import com.example.ui.components.HudStatusBar
import com.example.ui.components.HudWaveform
import com.example.ui.components.JarvisCore
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisOnlineGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@Composable
fun JarvisHudScreen(
    viewModel: JarvisViewModel,
    onNavigateHistory: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val context = LocalContext.current
    val jarvisState by viewModel.jarvisState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val latestUserText by viewModel.latestUserText.collectAsState()
    val latestResponseText by viewModel.latestResponseText.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val debugInfo by viewModel.debugInfo.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showTextInputDialog by remember { mutableStateOf(false) }

    // Audio Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            viewModel.submitCommand("Status report.")
        }
    }

    val quickCommands = listOf(
        "Battery status",
        "What time is it?",
        "Open Settings",
        "Set timer for 5 minutes",
        "What is my project called?",
        "Play music",
        "Search Kotlin news"
    )

    Scaffold(
        containerColor = JarvisObsidian,
        topBar = {
            HudStatusBar(telemetry = telemetry, state = jarvisState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // State Title Ticker
            val stateColor = when (jarvisState) {
                JarvisState.IDLE -> JarvisCyan
                JarvisState.LISTENING -> Color.White
                JarvisState.THINKING -> JarvisCyanLight
                JarvisState.SPEAKING -> JarvisOnlineGreen
                JarvisState.EXECUTING -> JarvisWarningAmber
                JarvisState.ERROR -> JarvisAlertRed
            }

            Text(
                text = jarvisState.label,
                color = stateColor,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = jarvisState.subtitle,
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Central Arc Reactor / AI Core
            JarvisCore(
                state = jarvisState,
                audioLevel = audioLevel,
                size = 230.dp,
                onClick = {
                    val hasMic = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasMic) {
                        viewModel.toggleListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.testTag("jarvis_central_core")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Waveform Visualizer
            HudWaveform(
                state = jarvisState,
                audioLevel = audioLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Output Feed HUD Panel
            HudPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                title = "Live Subsystem Feed",
                accentColor = stateColor
            ) {
                if (latestUserText.isNotEmpty()) {
                    Text(
                        text = "› $latestUserText",
                        color = JarvisCyanLight,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Text(
                    text = latestResponseText,
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Command Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickCommands) { cmd ->
                    Box(
                        modifier = Modifier
                            .background(JarvisGlassSurface, shape = RoundedCornerShape(2.dp))
                            .border(1.dp, JarvisGlassBorder, shape = RoundedCornerShape(2.dp))
                            .clickable { viewModel.submitCommand(cmd) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cmd,
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Input Field for accessibility and silent commands
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Transmit instruction...",
                            color = JarvisTextSecondary.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = JarvisGlassSurface,
                        unfocusedContainerColor = JarvisGlassSurface,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisGlassBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.submitCommand(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(JarvisCyan, shape = RoundedCornerShape(4.dp))
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Command",
                        tint = JarvisObsidian
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Control & Navigation Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // New Session Button
                IconButton(
                    onClick = { viewModel.startNewConversation() },
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, JarvisGlassBorder, CircleShape)
                        .testTag("new_session_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Session", tint = JarvisCyan)
                }

                // History Button
                IconButton(
                    onClick = onNavigateHistory,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, JarvisGlassBorder, CircleShape)
                        .testTag("nav_history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = JarvisCyan)
                }

                // Main Mic Button
                val isListening = jarvisState == JarvisState.LISTENING
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(if (isListening) JarvisAlertRed else JarvisCyan)
                        .clickable {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMic) {
                                viewModel.toggleListening()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("main_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = JarvisObsidian,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Memory Bank Button
                IconButton(
                    onClick = onNavigateMemory,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, JarvisGlassBorder, CircleShape)
                        .testTag("nav_memory_button")
                ) {
                    Icon(Icons.Default.Memory, contentDescription = "Memory Bank", tint = JarvisCyan)
                }

                // Settings Button
                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, JarvisGlassBorder, CircleShape)
                        .testTag("nav_settings_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = JarvisCyan)
                }
            }

            // Stop Speaking button if active
            AnimatedVisibility(visible = jarvisState == JarvisState.SPEAKING) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable { viewModel.stopSpeaking() }
                        .background(JarvisAlertRed.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp))
                        .border(1.dp, JarvisAlertRed, shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = null, tint = JarvisAlertRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STOP TRANSMISSION",
                            color = JarvisAlertRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Developer Debug Overlay (if enabled in settings)
            if (settings.debugModeEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                DebugOverlay(
                    state = jarvisState,
                    debugInfo = debugInfo,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
