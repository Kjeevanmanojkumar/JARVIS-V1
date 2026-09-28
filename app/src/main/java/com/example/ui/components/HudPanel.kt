package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun HudPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    accentColor: Color = JarvisCyan,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(JarvisGlassSurface, shape = RoundedCornerShape(4.dp))
            .border(1.dp, JarvisGlassBorder, shape = RoundedCornerShape(4.dp))
            .drawBehind {
                val bracketLen = 10.dp.toPx()
                val strokeW = 1.8.dp.toPx()
                val w = size.width
                val h = size.height

                // Top-Left corner bracket
                drawLine(accentColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
                drawLine(accentColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

                // Top-Right corner bracket
                drawLine(accentColor, Offset(w, 0f), Offset(w - bracketLen, 0f), strokeW)
                drawLine(accentColor, Offset(w, 0f), Offset(w, bracketLen), strokeW)

                // Bottom-Left corner bracket
                drawLine(accentColor, Offset(0f, h), Offset(bracketLen, h), strokeW)
                drawLine(accentColor, Offset(0f, h), Offset(0f, h - bracketLen), strokeW)

                // Bottom-Right corner bracket
                drawLine(accentColor, Offset(w, h), Offset(w - bracketLen, h), strokeW)
                drawLine(accentColor, Offset(w, h), Offset(w, h - bracketLen), strokeW)
            }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accentColor, shape = RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
            content()
        }
    }
}
