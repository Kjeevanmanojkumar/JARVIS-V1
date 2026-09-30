package com.example.ui.home

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.BottomNavTab
import com.example.ui.components.GlassCard
import com.example.ui.components.JarvisBottomNavigation
import com.example.ui.components.JarvisOrb
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSecondaryText
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisWhite
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onNavigateChat: () -> Unit,
    onNavigateTasks: () -> Unit,
    onNavigateVision: () -> Unit,
    onNavigateCreate: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateMemory: () -> Unit,
    onOpenVoiceFullscreen: () -> Unit,
    onOpenVoiceOverlay: () -> Unit
) {
    val context = LocalContext.current
    val jarvisState by viewModel.jarvisState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()

    var textInput by remember { mutableStateOf("") }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenVoiceFullscreen()
            viewModel.toggleListening()
        }
    }

    // Dynamic Greeting matching reference ("Good Morning!" etc.)
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 4..11 -> "Good Morning!"
        in 12..16 -> "Good Afternoon!"
        in 17..22 -> "Good Evening!"
        else -> "Good Night!"
    }

    Scaffold(
        containerColor = JarvisBackground,
        bottomBar = {
            JarvisBottomNavigation(
                currentTab = BottomNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> { /* already on home */ }
                        BottomNavTab.MEMORY -> onNavigateMemory()
                        BottomNavTab.TOOLS -> onNavigateTasks()
                        BottomNavTab.PROFILE -> onNavigateSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Status / Utility icon & Settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateVision,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurface)
                        .border(1.dp, JarvisBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = JarvisBrightCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JarvisSurface)
                        .border(1.dp, JarvisBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = JarvisBrightCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center: Large Holographic Orb
            JarvisOrb(
                state = jarvisState,
                audioLevel = audioLevel,
                size = 230.dp,
                onClick = {
                    val hasMic = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasMic) {
                        onOpenVoiceFullscreen()
                        viewModel.toggleListening()
                    } else {
                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Greeting & Subtitle
            Text(
                text = greeting,
                color = JarvisWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "How can I help you today?",
                color = JarvisSecondaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 2 x 2 Feature Cards Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    title = "Chat",
                    subtitle = "Talk with AI",
                    icon = Icons.Default.ChatBubble,
                    iconBgColor = Color(0xFF0284C7).copy(alpha = 0.25f),
                    iconTint = JarvisBrightCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateChat
                )

                FeatureCard(
                    title = "Tasks",
                    subtitle = "Manage your day",
                    icon = Icons.Default.Assignment,
                    iconBgColor = Color(0xFFEA580C).copy(alpha = 0.25f),
                    iconTint = Color(0xFFFB923C),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateTasks
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    title = "Vision",
                    subtitle = "Analyze images",
                    icon = Icons.Default.Image,
                    iconBgColor = Color(0xFF7C3AED).copy(alpha = 0.25f),
                    iconTint = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateVision
                )

                FeatureCard(
                    title = "Create",
                    subtitle = "Generate images",
                    icon = Icons.Default.AutoAwesome,
                    iconBgColor = Color(0xFFE11D48).copy(alpha = 0.25f),
                    iconTint = Color(0xFFFB7185),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateCreate
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom Input Pill: "Ask me anything..."
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(28.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask me anything...",
                            color = JarvisSecondaryText.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = JarvisWhite,
                        unfocusedTextColor = JarvisWhite
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.submitCommand(textInput)
                                textInput = ""
                                onNavigateChat()
                            }
                        }
                    )
                )

                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.submitCommand(textInput)
                            textInput = ""
                            onNavigateChat()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = JarvisBrightCyan
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMic) {
                                onOpenVoiceOverlay()
                                viewModel.toggleListening()
                            } else {
                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = JarvisBrightCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.height(118.dp),
        cornerRadius = 20.dp,
        borderColor = JarvisBorderCyan,
        backgroundColor = JarvisSurface,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = JarvisSecondaryText.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = JarvisWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = JarvisSecondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
