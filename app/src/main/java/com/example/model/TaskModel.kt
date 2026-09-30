package com.example.model

import androidx.compose.ui.graphics.vector.ImageVector

data class TaskItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val category: String, // "shopping", "work", "call", "travel", "reading", "fitness", "meeting", "reminder"
    val isCompleted: Boolean = false,
    val isReminder: Boolean = false,
    val repeatType: String = "Once"
)
