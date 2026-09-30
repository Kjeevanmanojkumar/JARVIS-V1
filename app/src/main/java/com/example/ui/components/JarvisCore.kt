package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.JarvisState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisWarningAmber
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun JarvisCore(
    state: JarvisState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core_infinite")

    // Rotation speeds based on state
    val outerRotationDuration = when (state) {
        JarvisState.IDLE -> 16000
        JarvisState.LISTENING -> 8000
        JarvisState.THINKING, JarvisState.PROCESSING -> 3000
        JarvisState.SPEAKING -> 6000
        JarvisState.EXECUTING -> 2000
        JarvisState.ERROR -> 12000
    }

    val innerRotationDuration = when (state) {
        JarvisState.IDLE -> 12000
        JarvisState.LISTENING -> 6000
        JarvisState.THINKING, JarvisState.PROCESSING -> 2400
        JarvisState.SPEAKING -> 4500
        JarvisState.EXECUTING -> 1800
        JarvisState.ERROR -> 9000
    }

    val outerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(outerRotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    val innerAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(innerRotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == JarvisState.LISTENING) 700 else 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Smooth transition for audio level reaction
    val audioPulse = remember { Animatable(0f) }
    LaunchedEffect(audioLevel) {
        audioPulse.animateTo(
            targetValue = audioLevel.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 60)
        )
    }

    // Dynamic colors based on system state
    val (primaryGlow, secondaryGlow, coreColor) = when (state) {
        JarvisState.IDLE -> Triple(JarvisCyan, JarvisCyanDark, JarvisCyanLight)
        JarvisState.LISTENING -> Triple(JarvisCyanLight, JarvisCyan, Color.White)
        JarvisState.THINKING, JarvisState.PROCESSING -> Triple(JarvisCyan, JarvisCyanLight, JarvisCyanDark)
        JarvisState.SPEAKING -> Triple(JarvisCyanLight, Color.White, JarvisCyan)
        JarvisState.EXECUTING -> Triple(JarvisWarningAmber, JarvisCyan, JarvisWarningAmber)
        JarvisState.ERROR -> Triple(JarvisAlertRed, Color(0xFFFF5252), JarvisAlertRed)
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val maxRadius = min(size.toPx(), size.toPx()) / 2f - 12f

            // Dynamic scale combining breathing and live mic audio
            val effectiveScale = (breathingScale + audioPulse.value * 0.18f).coerceIn(0.85f, 1.25f)
            val scaledRadius = maxRadius * effectiveScale

            // 1. Ambient Background Core Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.22f + audioPulse.value * 0.25f),
                        secondaryGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = scaledRadius * 1.1f
                ),
                radius = scaledRadius * 1.1f,
                center = center
            )

            // 2. Audio reaction shockwave rings (when listening or speaking)
            if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                val waveRadius = scaledRadius * (1.0f + audioPulse.value * 0.35f)
                drawCircle(
                    color = primaryGlow.copy(alpha = 0.35f * audioPulse.value),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Outermost Reticle Ring with 4 cardinal notches
            drawCircle(
                color = primaryGlow.copy(alpha = 0.3f),
                radius = scaledRadius,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )

            // 4 Cardinal HUD Crosshairs
            val crosshairLen = 8.dp.toPx()
            drawLine(
                color = primaryGlow.copy(alpha = 0.8f),
                start = Offset(center.x, center.y - scaledRadius - crosshairLen),
                end = Offset(center.x, center.y - scaledRadius + 2.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = primaryGlow.copy(alpha = 0.8f),
                start = Offset(center.x, center.y + scaledRadius - 2.dp.toPx()),
                end = Offset(center.x, center.y + scaledRadius + crosshairLen),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = primaryGlow.copy(alpha = 0.8f),
                start = Offset(center.x - scaledRadius - crosshairLen, center.y),
                end = Offset(center.x - scaledRadius + 2.dp.toPx(), center.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = primaryGlow.copy(alpha = 0.8f),
                start = Offset(center.x + scaledRadius - 2.dp.toPx(), center.y),
                end = Offset(center.x + scaledRadius + crosshairLen, center.y),
                strokeWidth = 2.dp.toPx()
            )

            // 4. Segmented Outer Technical Ring (Clockwise Rotation)
            rotate(outerAngle, pivot = center) {
                drawSegmentedRing(
                    center = center,
                    radius = scaledRadius * 0.84f,
                    strokeWidth = 2.5.dp.toPx(),
                    segments = 6,
                    color = primaryGlow
                )
            }

            // 5. Middle Technical Ticked Ring (Counter-Clockwise Rotation)
            rotate(innerAngle, pivot = center) {
                drawTechnicalRing(
                    center = center,
                    radius = scaledRadius * 0.65f,
                    strokeWidth = 1.8.dp.toPx(),
                    color = secondaryGlow
                )
            }

            // 6. Arc Reactor Triangular Energy Lattice
            rotate(outerAngle * 0.5f, pivot = center) {
                val triRadius = scaledRadius * 0.44f
                val path = androidx.compose.ui.graphics.Path()
                for (i in 0..2) {
                    val angleRad = Math.toRadians((i * 120.0)).toFloat()
                    val px = center.x + triRadius * cos(angleRad)
                    val py = center.y + triRadius * sin(angleRad)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(
                    path = path,
                    color = secondaryGlow.copy(alpha = 0.5f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 7. Inner Reactor Energy Chamber (Pulsing Gradient)
            val innerCoreRadius = scaledRadius * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        coreColor.copy(alpha = 0.8f),
                        primaryGlow.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = innerCoreRadius
                ),
                radius = innerCoreRadius,
                center = center
            )

            // 8. Intense Center Arc Spark Node
            val centerSparkRadius = 6.dp.toPx() * (1f + audioPulse.value * 0.4f)
            drawCircle(
                color = Color.White,
                radius = centerSparkRadius,
                center = center
            )
        }
    }
}

private fun DrawScope.drawSegmentedRing(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    segments: Int,
    color: Color
) {
    val totalArc = 360f / segments
    val gap = 14f
    val sweep = totalArc - gap

    for (i in 0 until segments) {
        val startAngle = i * totalArc
        drawArc(
            color = color.copy(alpha = if (i % 2 == 0) 0.9f else 0.55f),
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawTechnicalRing(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    color: Color
) {
    // Continuous fine circle
    drawCircle(
        color = color.copy(alpha = 0.4f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )

    // 12 technical tick marks around perimeter
    val ticks = 12
    val tickLength = 5.dp.toPx()
    for (i in 0 until ticks) {
        val angleRad = Math.toRadians((i * (360.0 / ticks))).toFloat()
        val startX = center.x + (radius - tickLength / 2) * cos(angleRad)
        val startY = center.y + (radius - tickLength / 2) * sin(angleRad)
        val endX = center.x + (radius + tickLength / 2) * cos(angleRad)
        val endY = center.y + (radius + tickLength / 2) * sin(angleRad)

        drawLine(
            color = color.copy(alpha = if (i % 3 == 0) 0.95f else 0.5f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
        )
    }
}
