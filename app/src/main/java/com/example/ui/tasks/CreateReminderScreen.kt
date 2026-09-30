package com.example.ui.tasks

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemoryCategory
import com.example.notification.ReminderNotificationHelper
import com.example.ui.components.GlassCard
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
import com.example.ui.theme.JarvisWhite

@Composable
fun CreateReminderScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current

    var title by remember { mutableStateOf("Take medicine") }
    var dateText by remember { mutableStateOf("Mon, Aug 12, 2026") }
    var timeText by remember { mutableStateOf("9:00 AM") }
    var repeatText by remember { mutableStateOf("Daily") }
    var useExactAlarm by remember { mutableStateOf(true) }

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
                    text = "Create Reminder",
                    color = JarvisWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                NeonButton(
                    onClick = {
                        // Persist into memory repository
                        viewModel.saveMemoryManual(
                            key = "reminder_${System.currentTimeMillis()}",
                            value = "$title at $timeText ($repeatText)",
                            category = MemoryCategory.TASK
                        )

                        // Issue reminder notification matching reference screen 7
                        ReminderNotificationHelper.showReminderNotification(
                            context = context,
                            title = "Time for your medicine 💊",
                            message = "Stay healthy!"
                        )

                        Toast.makeText(context, "Reminder '$title' created successfully.", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    style = NeonButtonStyle.FILLED_CYAN,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Create Reminder",
                        color = JarvisBackground,
                        fontSize = 15.sp,
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Title Field
            GlassFieldRow(label = "Title") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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

            // Date Field
            GlassFieldRow(
                label = "Date",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = JarvisSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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

            // Time Field
            GlassFieldRow(
                label = "Time",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = JarvisSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            ) {
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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

            // Repeat Dropdown Row
            GlassFieldRow(
                label = "Repeat",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = JarvisSecondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            ) {
                Text(
                    text = repeatText,
                    color = JarvisWhite,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable {
                            repeatText = when (repeatText) {
                                "Once" -> "Daily"
                                "Daily" -> "Weekly"
                                "Weekly" -> "Weekdays"
                                else -> "Once"
                            }
                        }
                        .padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Exact Alarm Switch Row
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                borderColor = JarvisBorderCyan,
                backgroundColor = JarvisSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Use exact alarm (when permitted)",
                        color = JarvisWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = useExactAlarm,
                        onCheckedChange = { useExactAlarm = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisBackground,
                            checkedTrackColor = JarvisBrightCyan,
                            uncheckedThumbColor = JarvisSecondaryText,
                            uncheckedTrackColor = JarvisSurface
                        )
                    )
                }
            }

            // Information Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                borderColor = JarvisBorder,
                backgroundColor = JarvisSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "I'll notify you at $timeText ${repeatText.lowercase()}.\nYou'll get a notification even if the app is closed.",
                        color = JarvisSecondaryText,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GlassFieldRow(
    label: String,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = JarvisBorderCyan,
        backgroundColor = JarvisSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = JarvisSecondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.width(68.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}
