package com.example.ui.voice

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisState
import com.example.ui.components.JarvisOrb
import com.example.ui.components.Waveform
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSecondaryText
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisWhite

@Composable
fun VoiceFullscreenScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        viewModel.stopSpeaking()
        onNavigateBack()
    }

    val jarvisState by viewModel.jarvisState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val latestUserText by viewModel.latestUserText.collectAsState()
    val latestResponse by viewModel.latestResponseText.collectAsState()

    val stateText = when (jarvisState) {
        JarvisState.LISTENING -> "Listening..."
        JarvisState.THINKING -> "Thinking..."
        JarvisState.PROCESSING -> "Processing..."
        JarvisState.EXECUTING -> "Executing..."
        JarvisState.SPEAKING -> "Speaking..."
        JarvisState.ERROR -> "Error detected"
        JarvisState.IDLE -> "Standing by"
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
                IconButton(onClick = {
                    viewModel.stopSpeaking()
                    onNavigateBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisBrightCyan
                    )
                }
            }
        },
        bottomBar = {
            // Bottom Action Pill: "Tap to stop"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(JarvisSurface)
                        .border(1.dp, JarvisBorderCyan, RoundedCornerShape(28.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = JarvisPrimaryCyan),
                            onClick = {
                                if (jarvisState == JarvisState.LISTENING) {
                                    viewModel.toggleListening()
                                } else if (jarvisState == JarvisState.SPEAKING) {
                                    viewModel.stopSpeaking()
                                } else {
                                    onNavigateBack()
                                }
                            }
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(JarvisWhite)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tap to stop",
                            color = JarvisWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // Large Abstract Holographic Orb
            JarvisOrb(
                state = jarvisState,
                audioLevel = audioLevel,
                size = 260.dp,
                onClick = {
                    viewModel.toggleListening()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // State Label ("Listening...")
            Text(
                text = stateText,
                color = JarvisWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Animated Waveform spanning width
            Waveform(
                state = jarvisState,
                audioLevel = audioLevel,
                height = 54.dp,
                barCount = 38,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            if (latestUserText.isNotEmpty() && jarvisState != JarvisState.IDLE) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "“$latestUserText”",
                    color = JarvisSecondaryText,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
