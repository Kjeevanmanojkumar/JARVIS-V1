package com.example.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.database.entity.MessageEntity
import com.example.model.JarvisState
import com.example.ui.components.BottomNavTab
import com.example.ui.components.GlassCard
import com.example.ui.components.JarvisBottomNavigation
import com.example.ui.components.JarvisOrb
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
import com.example.ui.theme.JarvisSurface2
import com.example.ui.theme.JarvisWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateTools: () -> Unit,
    onNavigateProfile: () -> Unit,
    onOpenVoiceFullscreen: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val recentMessages by viewModel.recentMessages.collectAsState()
    val jarvisState by viewModel.jarvisState.collectAsState()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenVoiceFullscreen()
            viewModel.toggleListening()
        }
    }

    LaunchedEffect(recentMessages.size) {
        if (recentMessages.isNotEmpty()) {
            listState.animateScrollToItem(recentMessages.size - 1)
        }
    }

    Scaffold(
        containerColor = JarvisBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisBrightCyan
                    )
                }

                Text(
                    text = "JARVIS",
                    color = JarvisWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = JarvisSecondaryText
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Conversation") },
                            onClick = {
                                viewModel.startNewConversation()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Input Bar: "Ask me anything..."
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(JarvisSurface)
                        .border(1.dp, JarvisBorderCyan, RoundedCornerShape(28.dp))
                        .padding(horizontal = 14.dp, vertical = 2.dp),
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
                                }
                            }
                        )
                    )

                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.submitCommand(textInput)
                                textInput = ""
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
                                    onOpenVoiceFullscreen()
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

                JarvisBottomNavigation(
                    currentTab = BottomNavTab.HOME,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.HOME -> onNavigateHome()
                            BottomNavTab.MEMORY -> onNavigateMemory()
                            BottomNavTab.TOOLS -> onNavigateTools()
                            BottomNavTab.PROFILE -> onNavigateProfile()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // If empty conversation, show representative conversation items matching reference
            if (recentMessages.isEmpty()) {
                item {
                    UserMessageBubble(
                        text = "Open YouTube for me",
                        timestamp = "9:41 AM"
                    )
                }

                item {
                    AssistantMessageBubble(
                        text = "Opening YouTube for you now."
                    )
                }

                item {
                    ExecutionAppCard(
                        appName = "YouTube",
                        status = "App opened successfully",
                        timestamp = "9:41 AM"
                    )
                }

                item {
                    UserMessageBubble(
                        text = "Set a reminder to call Mom at 6 PM",
                        timestamp = "9:42 AM"
                    )
                }

                item {
                    AssistantMessageBubble(
                        text = "Got it! I'll remind you to call Mom at 6:00 PM today."
                    )
                }

                item {
                    ReminderCardItem(
                        title = "Call Mom",
                        time = "Today, 6:00 PM",
                        status = "Reminder set",
                        timestamp = "9:42 AM"
                    )
                }
            } else {
                items(recentMessages) { message ->
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

                    when (message.role.uppercase()) {
                        "USER" -> {
                            UserMessageBubble(
                                text = message.content,
                                timestamp = timeStr
                            )
                        }
                        "TOOL" -> {
                            val toolStr = message.toolName ?: "System"
                            if (toolStr.contains("alarm", ignoreCase = true) ||
                                toolStr.contains("timer", ignoreCase = true) ||
                                message.content.contains("reminder", ignoreCase = true)) {
                                ReminderCardItem(
                                    title = toolStr.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    time = "Scheduled",
                                    status = "Action completed",
                                    timestamp = timeStr
                                )
                            } else {
                                ExecutionAppCard(
                                    appName = toolStr.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    status = message.toolResult ?: message.content,
                                    timestamp = timeStr
                                )
                            }
                        }
                        else -> {
                            AssistantMessageBubble(
                                text = message.content
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
fun UserMessageBubble(text: String, timestamp: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .background(Color(0xFF0C2A47))
                .border(1.dp, JarvisBorderCyan, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                color = JarvisWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = timestamp,
            color = JarvisSecondaryText,
            fontSize = 10.sp
        )
    }
}

@Composable
fun AssistantMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        JarvisOrb(
            state = JarvisState.IDLE,
            size = 32.dp,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(JarvisSurface)
                .border(1.dp, JarvisBorder, RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Text(
                text = text,
                color = JarvisWhite,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun ExecutionAppCard(appName: String, status: String, timestamp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 42.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(0.9f),
            cornerRadius = 16.dp,
            borderColor = JarvisBorderCyan,
            backgroundColor = JarvisSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red YouTube or App icon container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE50914)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = appName,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appName,
                        color = JarvisWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = status,
                        color = JarvisSecondaryText,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = timestamp,
                    color = JarvisSecondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ReminderCardItem(title: String, time: String, status: String, timestamp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 42.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(0.9f),
            cornerRadius = 16.dp,
            borderColor = JarvisBorderCyan,
            backgroundColor = JarvisSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDC2626)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = JarvisWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = time,
                        color = JarvisSecondaryText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = status,
                        color = JarvisOnlineGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = timestamp,
                    color = JarvisSecondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}
