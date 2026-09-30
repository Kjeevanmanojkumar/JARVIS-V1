package com.example.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisOnlineGreen
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSecondaryText
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisWhite

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val allMemories by viewModel.allMemories.collectAsState()

    var activeDialog by remember { mutableStateOf<String?>(null) }

    // Dialog state holders
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

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    Scaffold(
        containerColor = JarvisBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisBrightCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    color = JarvisWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Row 1: AI & App Settings
            SettingsRowCard(
                icon = Icons.Default.AutoAwesome,
                iconBg = Color(0xFF059669),
                title = "AI & App Settings",
                subtitle = "Manage your API key and AI configuration",
                onClick = { activeDialog = "ai" }
            )

            // Row 2: Permissions
            SettingsRowCard(
                icon = Icons.Default.Security,
                iconBg = Color(0xFF7C3AED),
                title = "Permissions",
                subtitle = "Microphone, Notifications, Overlay",
                onClick = { activeDialog = "permissions" }
            )

            // Row 3: Appearance
            SettingsRowCard(
                icon = Icons.Default.Palette,
                iconBg = Color(0xFFEA580C),
                title = "Appearance",
                subtitle = "Theme, Voice rate, Pitch",
                onClick = { activeDialog = "appearance" }
            )

            // Row 4: Data & Privacy
            SettingsRowCard(
                icon = Icons.Default.Lock,
                iconBg = Color(0xFF2563EB),
                title = "Data & Privacy",
                subtitle = "Manage your data (${allMemories.size} memories)",
                onClick = { activeDialog = "data" }
            )

            // Row 5: Backup & Sync
            SettingsRowCard(
                icon = Icons.Default.CloudSync,
                iconBg = Color(0xFF16A34A),
                title = "Backup & Sync",
                subtitle = "Keep your data safe",
                onClick = { activeDialog = "backup" }
            )

            // Row 6: About
            SettingsRowCard(
                icon = Icons.Default.Info,
                iconBg = Color(0xFF0284C7),
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = { activeDialog = "about" }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Dialogs for detailed settings
    when (activeDialog) {
        "ai" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("AI & App Settings", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Gemini API Key", color = JarvisSecondaryText, fontSize = 13.sp)
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                    Icon(
                                        imageVector = if (apiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = JarvisSecondaryText
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = JarvisWhite,
                                unfocusedTextColor = JarvisWhite,
                                focusedBorderColor = JarvisBrightCyan,
                                unfocusedBorderColor = JarvisBorder
                            )
                        )

                        Text("Custom System Directive", color = JarvisSecondaryText, fontSize = 13.sp)
                        OutlinedTextField(
                            value = customPromptInput,
                            onValueChange = { customPromptInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = JarvisWhite,
                                unfocusedTextColor = JarvisWhite,
                                focusedBorderColor = JarvisBrightCyan,
                                unfocusedBorderColor = JarvisBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    NeonButton(
                        onClick = {
                            viewModel.updateApiKey(apiKeyInput.trim())
                            viewModel.updateCustomPrompt(customPromptInput.trim())
                            Toast.makeText(context, "AI Settings saved.", Toast.LENGTH_SHORT).show()
                            activeDialog = null
                        },
                        style = NeonButtonStyle.FILLED_CYAN
                    ) {
                        Text("Save", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    NeonButton(
                        onClick = { activeDialog = null },
                        style = NeonButtonStyle.OUTLINE_GLASS
                    ) {
                        Text("Cancel", color = JarvisWhite)
                    }
                }
            )
        }
        "permissions" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("App Permissions", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Microphone (Audio capture)", color = JarvisWhite, fontSize = 14.sp)
                            Text(
                                text = if (hasMicPermission) "GRANTED" else "DENIED",
                                color = if (hasMicPermission) JarvisOnlineGreen else JarvisAlertRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (!hasMicPermission) {
                            NeonButton(
                                onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                style = NeonButtonStyle.FILLED_CYAN,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Request Mic Permission", color = JarvisBackground, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exact Alarms & Timers", color = JarvisWhite, fontSize = 14.sp)
                            Text("ALLOWED", color = JarvisOnlineGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notifications", color = JarvisWhite, fontSize = 14.sp)
                            Text("CONFIGURED", color = JarvisOnlineGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    NeonButton(onClick = { activeDialog = null }, style = NeonButtonStyle.OUTLINE_GLASS) {
                        Text("Close", color = JarvisWhite)
                    }
                }
            )
        }
        "appearance" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("Appearance & Speech", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Speech Rate: ${"%.1f".format(speechRate)}x", color = JarvisWhite, fontSize = 14.sp)
                        Slider(
                            value = speechRate,
                            onValueChange = {
                                speechRate = it
                                viewModel.setSpeechRate(it)
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisBrightCyan,
                                activeTrackColor = JarvisBrightCyan
                            )
                        )

                        Text("Speech Pitch: ${"%.1f".format(speechPitch)}x", color = JarvisWhite, fontSize = 14.sp)
                        Slider(
                            value = speechPitch,
                            onValueChange = {
                                speechPitch = it
                                viewModel.setSpeechPitch(it)
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisBrightCyan,
                                activeTrackColor = JarvisBrightCyan
                            )
                        )
                    }
                },
                confirmButton = {
                    NeonButton(onClick = { activeDialog = null }, style = NeonButtonStyle.FILLED_CYAN) {
                        Text("Done", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        "data" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("Data & Privacy", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "JARVIS stores memory facts and conversation logs securely inside your local Room SQLite database.",
                            color = JarvisSecondaryText,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Stored Memories: ${allMemories.size}",
                            color = JarvisBrightCyan,
                            fontWeight = FontWeight.Bold
                        )
                        NeonButton(
                            onClick = {
                                viewModel.clearAllMemories()
                                Toast.makeText(context, "All memories wiped.", Toast.LENGTH_SHORT).show()
                                activeDialog = null
                            },
                            style = NeonButtonStyle.OUTLINE_GLASS,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear All Memories", color = JarvisAlertRed)
                        }
                    }
                },
                confirmButton = {
                    NeonButton(onClick = { activeDialog = null }, style = NeonButtonStyle.OUTLINE_GLASS) {
                        Text("Close", color = JarvisWhite)
                    }
                }
            )
        }
        "backup" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("Backup & Sync", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Local backup is synced with Android system auto-backup. Your configuration and memories persist across application restarts.",
                        color = JarvisSecondaryText,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    NeonButton(onClick = { activeDialog = null }, style = NeonButtonStyle.FILLED_CYAN) {
                        Text("OK", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        "about" -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                containerColor = JarvisSurface,
                title = { Text("About JARVIS V1", color = JarvisWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("JARVIS Android Assistant", color = JarvisBrightCyan, fontWeight = FontWeight.Bold)
                        Text("Version: 1.0.0", color = JarvisWhite)
                        Text("Engine: Whisper STT, Gemini AI & Android Automation", color = JarvisSecondaryText, fontSize = 12.sp)
                        Text("Database: SQLite Room v1", color = JarvisSecondaryText, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    NeonButton(onClick = { activeDialog = null }, style = NeonButtonStyle.FILLED_CYAN) {
                        Text("Close", color = JarvisBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsRowCard(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(JarvisSurface)
            .border(1.dp, JarvisBorderCyan, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = JarvisPrimaryCyan),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored square icon container matching reference screen 10
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = JarvisWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = JarvisSecondaryText,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = JarvisSecondaryText.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}
