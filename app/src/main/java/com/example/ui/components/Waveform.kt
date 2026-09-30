package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.JarvisState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import kotlin.math.sin

@Composable
fun Waveform(
    state: JarvisState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 36,
    activeColor: Color = JarvisPrimaryCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = this.size.width
        val canvasHeight = this.size.height
        val centerY = canvasHeight / 2f
        val spacing = width / barCount
        val barWidth = (spacing * 0.45f).coerceAtLeast(2f)

        val isLive = state == JarvisState.LISTENING || state == JarvisState.SPEAKING
        val isThinking = state == JarvisState.THINKING || state == JarvisState.PROCESSING || state == JarvisState.EXECUTING
        val isError = state == JarvisState.ERROR

        val baseColor = when {
            isError -> JarvisAlertRed
            else -> activeColor
        }

        for (i in 0 until barCount) {
            val x = i * spacing + spacing / 2f

            // Dynamic amplitude envelope (bell curve centered in middle)
            val normalizedX = (i - barCount / 2f) / (barCount / 2f)
            val envelope = (1f - normalizedX * normalizedX).coerceIn(0.12f, 1f)

            val waveVal = if (isLive) {
                val freq1 = sin(phase + i * 0.35f)
                val freq2 = sin(phase * 1.5f + i * 0.7f)
                val combined = (freq1 * 0.6f + freq2 * 0.4f)
                val micFactor = (audioLevel * 1.8f).coerceIn(0.1f, 1.0f)
                (0.2f + 0.8f * kotlin.math.abs(combined) * micFactor) * envelope
            } else if (isThinking) {
                val travel = sin(phase * 2f + i * 0.4f)
                (0.2f + 0.5f * kotlin.math.abs(travel)) * envelope
            } else {
                // Gentle idle ripple
                val gentle = sin(phase * 0.8f + i * 0.2f)
                (0.15f + 0.18f * kotlin.math.abs(gentle)) * envelope
            }

            val barHeight = (canvasHeight * waveVal).coerceIn(4f, canvasHeight * 0.95f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JarvisBrightCyan.copy(alpha = 0.95f),
                        baseColor.copy(alpha = 0.7f)
                    ),
                    startY = centerY - barHeight / 2f,
                    endY = centerY + barHeight / 2f
                ),
                topLeft = Offset(x - barWidth / 2f, centerY - barHeight / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
