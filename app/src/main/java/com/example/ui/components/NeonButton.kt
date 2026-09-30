package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorderCyan
import com.example.ui.theme.JarvisBrightCyan
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSurface

enum class NeonButtonStyle {
    FILLED_CYAN,
    OUTLINE_GLASS
}

@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeonButtonStyle = NeonButtonStyle.FILLED_CYAN,
    cornerRadius: Dp = 24.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val (bg, border, rippleColor) = when (style) {
        NeonButtonStyle.FILLED_CYAN -> Triple(
            if (enabled) JarvisBrightCyan else JarvisBrightCyan.copy(alpha = 0.5f),
            Color.Transparent,
            JarvisBackground
        )
        NeonButtonStyle.OUTLINE_GLASS -> Triple(
            if (enabled) JarvisSurface else JarvisSurface.copy(alpha = 0.5f),
            if (enabled) JarvisBorderCyan else JarvisBorderCyan.copy(alpha = 0.3f),
            JarvisPrimaryCyan
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .border(if (style == NeonButtonStyle.OUTLINE_GLASS) 1.dp else 0.dp, border, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = rippleColor),
                onClick = onClick
            )
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
