package com.example.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.HudPanel
import com.example.ui.hud.JarvisViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val allMemories by viewModel.allMemories.collectAsState()

    var apiKeyInput by remember(settings.apiKey) { mutableStateOf(settings.apiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var customPromptInput by remember(settings.customSystemPrompt) { mutableStateOf(settings.customSystemPrompt) }
    var speechRate by remember(settings.speechRate) { mutableFloatStateOf(settings.speechRate) }
    var speechPitch by remember(settings.speechPitch) { mutableFloatStateOf(settings.speechPitch) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCallPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCallPermission = granted
    }

    Scaffold(
        containerColor = JarvisObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SYSTEM CONFIGURATION",
                        color = JarvisCyan,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JarvisCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisGlassSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. AI BRAIN SECTION
            HudPanel(title = "AI Brain Configuration") {
                Text(
                    text = "PROVIDER: Google Gemini REST API (Primary) + Local Offline Engine (Fallback)",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))

                // API Key field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        viewModel.updateApiKey(it)
                    },
                    label = { Text("Gemini API Key", color = JarvisTextSecondary, fontSize = 12.sp) },
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = JarvisCyan
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisGlassBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configured via Secrets panel in AI Studio or override directly here. Keys are persisted in private app storage.",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Model Selection
                Text(
                    text = "ACTIVE MODEL: ${settings.modelName}",
                    color = JarvisCyanLight,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-flash-latest").forEach { model ->
                        val isSelected = settings.modelName == model
                        Button(
                            onClick = { viewModel.updateModelName(model) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) JarvisCyan else JarvisGlassSurface
                            ),
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier
                                .border(1.dp, if (isSelected) JarvisCyan else JarvisGlassBorder, RoundedCornerShape(2.dp))
                        ) {
                            Text(
                                text = model.replace("gemini-", "").replace("-preview", ""),
                                color = if (isSelected) JarvisObsidian else JarvisCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. VOICE SYNTHESIS SECTION
            HudPanel(title = "Audio & Voice Pipeline") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VOICE OUTPUT (TTS)",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Speak AI answers aloud",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = settings.voiceEnabled,
                        onCheckedChange = { viewModel.setVoiceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = JarvisTextSecondary,
                            uncheckedTrackColor = JarvisGlassSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SPEECH RATE: ${(speechRate * 100).toInt()}%",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        viewModel.setSpeechRate(it)
                    },
                    valueRange = 0.7f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisGlassBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SPEECH PITCH: ${(speechPitch * 100).toInt()}%",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = speechPitch,
                    onValueChange = {
                        speechPitch = it
                        viewModel.setSpeechPitch(it)
                    },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisGlassBorder
                    )
                )
            }

            // 3. PERSISTENT MEMORY SECTION
            HudPanel(title = "Neural Memory Management") {
                Text(
                    text = "Stored Records in Local Room Database: ${allMemories.size}",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.clearAllMemories() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisAlertRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.border(1.dp, JarvisAlertRed, RoundedCornerShape(2.dp))
                ) {
                    Text(
                        "PURGE ENTIRE MEMORY BANK",
                        color = JarvisAlertRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. PRIVACY & PERMISSIONS SECTION
            HudPanel(title = "System Security & Permissions") {
                PermissionRow(
                    name = "Microphone (RECORD_AUDIO)",
                    purpose = "Real-time speech-to-text voice recognition",
                    isGranted = hasMicPermission,
                    onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                PermissionRow(
                    name = "Telephone (CALL_PHONE)",
                    purpose = "Direct outbound calling tool (dialer fallback used if not granted)",
                    isGranted = hasCallPermission,
                    onRequest = { callLauncher.launch(Manifest.permission.CALL_PHONE) }
                )
            }

            // 5. DEVELOPER / DEBUG SECTION
            HudPanel(title = "Developer Telemetry") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HUD DEBUGGER OVERLAY",
                            color = JarvisWarningAmber,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Show live state, tool arguments & response telemetry",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = settings.debugModeEnabled,
                        onCheckedChange = { viewModel.setDebugMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisWarningAmber,
                            checkedTrackColor = JarvisWarningAmber.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // 6. ABOUT SECTION
            HudPanel(title = "About JARVIS V1") {
                Text(
                    text = "JARVIS V1 — Just A Rather Very Intelligent System",
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version: 1.0.0 (Production Release)\nArchitecture: Clean Architecture + MVVM + Room + Jetpack Compose\nSubsystems: SpeechRecognizer, TextToSpeech, 10 Android Tools, Gemini REST, Local Intent Engine",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    purpose: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisGlassSurface, shape = RoundedCornerShape(2.dp))
            .border(1.dp, JarvisGlassBorder, shape = RoundedCornerShape(2.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isGranted) JarvisOnlineGreen else JarvisWarningAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = purpose,
                color = JarvisTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (!isGranted) {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("GRANT", color = JarvisObsidian, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .background(JarvisOnlineGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "ACTIVE",
                    color = JarvisOnlineGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
