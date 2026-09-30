package com.example.ui.create

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonButtonStyle
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSecondaryText
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurface2
import com.example.ui.theme.JarvisWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateImageScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptText by remember {
        mutableStateOf("A serene mountain landscape at sunset, digital art, cinematic")
    }

    val styles = listOf("Realistic", "Artistic", "Anime", "Minimal")
    var selectedStyle by remember { mutableStateOf("Realistic") }
    var isGenerating by remember { mutableStateOf(false) }

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
                    text = "Create",
                    color = JarvisWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            // Bottom Action Buttons: Generate Again & Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NeonButton(
                    onClick = {
                        scope.launch {
                            isGenerating = true
                            delay(1200)
                            isGenerating = false
                            Toast.makeText(context, "Regenerated creative concept.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    style = NeonButtonStyle.OUTLINE_GLASS,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = JarvisBrightCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Generate Again",
                        color = JarvisWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                NeonButton(
                    onClick = {
                        Toast.makeText(context, "Image artwork saved to gallery.", Toast.LENGTH_SHORT).show()
                    },
                    style = NeonButtonStyle.FILLED_CYAN,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = JarvisBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save",
                        color = JarvisBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Prompt Field Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = {
                        Text(
                            text = "Describe image prompt...",
                            color = JarvisSecondaryText.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = JarvisWhite,
                        unfocusedTextColor = JarvisWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Style Selector Section
            Text(
                text = "Style",
                color = JarvisWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(styles) { style ->
                    val isSelected = style == selectedStyle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) JarvisBrightCyan else JarvisSurface)
                            .border(1.dp, if (isSelected) JarvisBrightCyan else JarvisBorder, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = JarvisPrimaryCyan),
                                onClick = { selectedStyle = style }
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = style,
                            color = if (isSelected) JarvisBackground else JarvisSecondaryText,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Generated-Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, JarvisBorderCyan, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(JarvisSurface2),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = JarvisBrightCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Synthesizing visual canvas...",
                                color = JarvisSecondaryText,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Alpine sunset mountain canvas matching reference screen 4
                    AlpineSunsetCanvas()
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AlpineSunsetCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Sunset sky gradient: deep indigo -> rich magenta/crimson -> warm orange/gold
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E1B4B),
                    Color(0xFF4C1D95),
                    Color(0xFFBE185D),
                    Color(0xFFF97316),
                    Color(0xFFFDE047)
                ),
                startY = 0f,
                endY = h * 0.55f
            )
        )

        // Sun disc setting
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFBEB),
                    Color(0xFFFDE047),
                    Color(0xFFF97316).copy(alpha = 0f)
                ),
                center = Offset(w * 0.72f, h * 0.42f),
                radius = 70f
            ),
            radius = 70f,
            center = Offset(w * 0.72f, h * 0.42f)
        )

        // Jagged mountain peaks (layered silhouette)
        val mountainPathFar = Path().apply {
            moveTo(0f, h * 0.52f)
            lineTo(w * 0.25f, h * 0.32f)
            lineTo(w * 0.48f, h * 0.42f)
            lineTo(w * 0.68f, h * 0.24f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w, h * 0.34f)
            lineTo(w, h * 0.6f)
            lineTo(0f, h * 0.6f)
            close()
        }
        drawPath(mountainPathFar, color = Color(0xFF312E81).copy(alpha = 0.7f))

        val mountainPathMid = Path().apply {
            moveTo(0f, h * 0.58f)
            lineTo(w * 0.18f, h * 0.44f)
            lineTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.82f, h * 0.32f)
            lineTo(w, h * 0.46f)
            lineTo(w, h * 0.65f)
            lineTo(0f, h * 0.65f)
            close()
        }
        drawPath(mountainPathMid, color = Color(0xFF1E1B4B))

        // Lake surface with reflective ripple gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBE185D).copy(alpha = 0.8f),
                    Color(0xFFF97316).copy(alpha = 0.6f),
                    Color(0xFF1E1B4B)
                ),
                startY = h * 0.6f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.6f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.4f)
        )

        // Shoreline pine forest silhouettes
        val forestPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.78f)
            for (i in 0..12) {
                val step = w / 12f
                val px = i * step
                lineTo(px + step * 0.3f, h * 0.74f)
                lineTo(px + step * 0.5f, h * 0.69f)
                lineTo(px + step * 0.7f, h * 0.75f)
            }
            lineTo(w, h * 0.82f)
            lineTo(w, h)
            close()
        }
        drawPath(forestPath, color = Color(0xFF0F172A))
    }
}
