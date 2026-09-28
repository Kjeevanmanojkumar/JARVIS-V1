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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.model.JarvisState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisWarningAmber
import kotlin.math.sin

@Composable
fun HudWaveform(
    state: JarvisState,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val (lineColor, centerGlow) = when (state) {
        JarvisState.IDLE -> Pair(JarvisCyanDark.copy(alpha = 0.5f), JarvisCyan)
        JarvisState.LISTENING -> Pair(JarvisCyan, Color.White)
        JarvisState.THINKING -> Pair(JarvisCyanLight, JarvisCyanDark)
        JarvisState.SPEAKING -> Pair(Color.White, JarvisCyan)
        JarvisState.EXECUTING -> Pair(JarvisWarningAmber, JarvisCyan)
        JarvisState.ERROR -> Pair(JarvisAlertRed, Color.White)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val bars = 32
        val barSpacing = width / (bars + 1)

        val baseAmplitude = when (state) {
            JarvisState.IDLE -> 3.dp.toPx()
            JarvisState.LISTENING -> (8.dp.toPx() + audioLevel * 20.dp.toPx()).coerceAtMost(height / 2f - 4f)
            JarvisState.THINKING -> 8.dp.toPx()
            JarvisState.SPEAKING -> 14.dp.toPx()
            JarvisState.EXECUTING -> 6.dp.toPx()
            JarvisState.ERROR -> 4.dp.toPx()
        }

        // Draw center baseline
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    lineColor.copy(alpha = 0.3f),
                    centerGlow.copy(alpha = 0.7f),
                    lineColor.copy(alpha = 0.3f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw frequency bars
        for (i in 0 until bars) {
            val x = (i + 1) * barSpacing
            val normX = (i.toFloat() / bars) - 0.5f // -0.5 to 0.5
            val envelope = (1f - (normX * 2f) * (normX * 2f)).coerceIn(0.1f, 1f) // bell curve

            val dynamicSine = sin(phase + i * 0.4f)
            val barHeight = baseAmplitude * envelope * (0.4f + 0.6f * kotlin.math.abs(dynamicSine))

            val startY = centerY - barHeight
            val endY = centerY + barHeight

            drawLine(
                color = if (i in (bars / 2 - 4)..(bars / 2 + 4)) centerGlow else lineColor,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
