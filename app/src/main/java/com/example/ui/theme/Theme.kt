package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisObsidian,
    primaryContainer = JarvisGlassSurface,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisCyanDark,
    onSecondary = JarvisObsidian,
    secondaryContainer = JarvisGlassSurface,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisOnlineGreen,
    onTertiary = JarvisObsidian,
    error = JarvisAlertRed,
    onError = Color.White,
    background = JarvisObsidian,
    onBackground = JarvisTextPrimary,
    surface = JarvisGlassSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisDeepSpace,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisGlassBorder
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
