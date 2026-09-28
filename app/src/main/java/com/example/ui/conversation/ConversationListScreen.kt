package com.example.ui.conversation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val conversations by viewModel.allConversations.collectAsState()
    val activeId by viewModel.activeConversationId.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())

    Scaffold(
        containerColor = JarvisObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MISSION ARCHIVES",
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
                actions = {
                    IconButton(onClick = { viewModel.startNewConversation() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "New Session",
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
                .padding(16.dp)
        ) {
            Text(
                text = "${conversations.size} RECORDED SESSIONS",
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO HISTORICAL LOGS RECORDED",
                        color = JarvisTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        val isActive = conv.id == activeId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(JarvisGlassSurface, shape = RoundedCornerShape(4.dp))
                                .border(
                                    width = if (isActive) 1.5.dp else 1.dp,
                                    color = if (isActive) JarvisCyan else JarvisGlassBorder,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = if (isActive) JarvisCyan else JarvisTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = conv.title,
                                            color = JarvisTextPrimary,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[ACTIVE]",
                                                color = JarvisCyan,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormat.format(Date(conv.lastUpdatedAt)),
                                        color = JarvisTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteConversation(conv.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = JarvisAlertRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
