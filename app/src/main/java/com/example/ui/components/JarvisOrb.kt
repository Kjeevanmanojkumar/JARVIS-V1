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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.JarvisState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import kotlin.math.cos
import kotlin.math.sin

/**
 * JarvisOrb: Pure abstract holographic sphere matching reference image.
 * No faces, no helmets, no logos.
 * Contains:
 * - Spherical translucent glass body
 * - Dynamic internal energy ribbons/rings
 * - Soft diffuse cyan outer glow
 * - Specular reflections and purple rim illumination
 */
@Composable
fun JarvisOrb(
    state: JarvisState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb_transition")

    // Dynamic rotation durations based on state
    val rotationDuration = when (state) {
        JarvisState.IDLE -> 14000
        JarvisState.LISTENING -> 7000
        JarvisState.THINKING -> 2800
        JarvisState.PROCESSING -> 3200
        JarvisState.EXECUTING -> 2000
        JarvisState.SPEAKING -> 5000
        JarvisState.ERROR -> 10000
    }

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == JarvisState.LISTENING) 800 else 2600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_breathing"
    )

    // Smooth audio reactivity
    val audioAnim = remember { Animatable(0f) }
    LaunchedEffect(audioLevel) {
        audioAnim.animateTo(
            targetValue = audioLevel.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 70)
        )
    }

    // Color accents
    val (primaryGlow, secondaryGlow, rimColor) = when (state) {
        JarvisState.IDLE -> Triple(JarvisPrimaryCyan, JarvisBlue, Color(0xFF9333EA))
        JarvisState.LISTENING -> Triple(JarvisBrightCyan, JarvisPrimaryCyan, Color(0xFFC084FC))
        JarvisState.THINKING, JarvisState.PROCESSING -> Triple(JarvisBrightCyan, JarvisBlue, Color(0xFFA855F7))
        JarvisState.SPEAKING -> Triple(Color(0xFF38BDF8), JarvisBrightCyan, Color(0xFFE879F9))
        JarvisState.EXECUTING -> Triple(JarvisBrightCyan, Color(0xFF0284C7), Color(0xFFA855F7))
        JarvisState.ERROR -> Triple(JarvisAlertRed, Color(0xFFFF6B6B), Color(0xFFF59E0B))
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
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.width / 2f) * 0.72f
            val effectiveScale = (breathingPulse + audioAnim.value * 0.16f).coerceIn(0.9f, 1.25f)
            val orbRadius = baseRadius * effectiveScale

            // 1. Soft atmospheric outer cyan glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.38f + audioAnim.value * 0.28f),
                        secondaryGlow.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 1.6f
                ),
                radius = orbRadius * 1.6f,
                center = center
            )

            // 2. Shockwave ripples when listening or speaking
            if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                val rippleRadius = orbRadius * (1.1f + audioAnim.value * 0.32f)
                drawCircle(
                    color = primaryGlow.copy(alpha = (0.45f * audioAnim.value).coerceIn(0f, 0.45f)),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. Spherical body background with dark deep space / obsidian core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF030A14),
                        Color(0xFF051224),
                        Color(0xFF02060C)
                    ),
                    center = Offset(center.x - orbRadius * 0.2f, center.y - orbRadius * 0.2f),
                    radius = orbRadius
                ),
                radius = orbRadius,
                center = center
            )

            // 4. Subtle internal energy rings rotating
            rotate(rotationAngle, pivot = center) {
                // Diagonal elliptical energy ring
                drawOval(
                    color = primaryGlow.copy(alpha = 0.45f),
                    topLeft = Offset(center.x - orbRadius * 0.82f, center.y - orbRadius * 0.32f),
                    size = Size(orbRadius * 1.64f, orbRadius * 0.64f),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Secondary inner orbital ring
                drawOval(
                    color = secondaryGlow.copy(alpha = 0.3f),
                    topLeft = Offset(center.x - orbRadius * 0.55f, center.y - orbRadius * 0.85f),
                    size = Size(orbRadius * 1.1f, orbRadius * 1.7f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // 5. Main Holographic Energy Arc / Vortex (Exact match to reference crescent filament)
            rotate(rotationAngle * 0.6f, pivot = center) {
                val arcPath = Path().apply {
                    val r = orbRadius * 0.72f
                    moveTo(center.x - r * 0.7f, center.y - r * 0.5f)
                    cubicTo(
                        center.x - r * 0.3f, center.y - r * 0.95f,
                        center.x + r * 0.85f, center.y - r * 0.2f,
                        center.x + r * 0.4f, center.y + r * 0.75f
                    )
                }

                // Glowing energy filament
                drawPath(
                    path = arcPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.95f),
                            primaryGlow.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        start = Offset(center.x - orbRadius, center.y - orbRadius),
                        end = Offset(center.x + orbRadius, center.y + orbRadius)
                    ),
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Glow blur around filament
                drawPath(
                    path = arcPath,
                    color = primaryGlow.copy(alpha = 0.4f),
                    style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 6. Right-side electric purple/magenta rim lighting (exact reference visual)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        rimColor.copy(alpha = 0.65f),
                        rimColor.copy(alpha = 0.85f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = 20f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(center.x - orbRadius, center.y - orbRadius),
                size = Size(orbRadius * 2, orbRadius * 2),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 7. Left/Top bright cyan thin rim reflection
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryGlow.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.85f),
                        Color.Transparent
                    ),
                    center = center
                ),
                startAngle = 180f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(center.x - orbRadius, center.y - orbRadius),
                size = Size(orbRadius * 2, orbRadius * 2),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 8. Glass sphere specular highlight (glossy marble appearance)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.42f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - orbRadius * 0.28f, center.y - orbRadius * 0.42f),
                    radius = orbRadius * 0.45f
                ),
                topLeft = Offset(center.x - orbRadius * 0.48f, center.y - orbRadius * 0.65f),
                size = Size(orbRadius * 0.72f, orbRadius * 0.42f)
            )
        }
    }
}
