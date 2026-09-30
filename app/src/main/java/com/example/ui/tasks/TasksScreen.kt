package com.example.ui.tasks

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TaskItem
import com.example.ui.components.BottomNavTab
import com.example.ui.components.JarvisBottomNavigation
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
fun TasksScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateCreateReminder: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateMemory: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    BackHandler { onNavigateBack() }

    var selectedTab by remember { mutableStateOf("Tasks") }

    // Seed initial tasks strictly matching reference screen 5
    val taskList = remember {
        mutableStateListOf(
            TaskItem("1", "Buy groceries", "Tomorrow, 10:00 AM", "shopping", isCompleted = false),
            TaskItem("2", "Finish project report", "Today, 3:00 PM", "work", isCompleted = false),
            TaskItem("3", "Call mom", "Today, 6:00 PM", "call", isCompleted = false),
            TaskItem("4", "Plan weekend trip", "Sat, 10:00 AM", "travel", isCompleted = false),
            TaskItem("5", "Read a book", "No due date", "reading", isCompleted = false),
            TaskItem("6", "Morning workout", "Today, 7:00 AM", "fitness", isCompleted = true),
            TaskItem("7", "Team meeting", "Today, 9:00 AM", "meeting", isCompleted = true)
        )
    }

    Scaffold(
        containerColor = JarvisBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisBrightCyan
                    )
                }

                Text(
                    text = "JARVIS",
                    color = JarvisWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = onNavigateCreateReminder) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Reminder",
                        tint = JarvisBrightCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        bottomBar = {
            JarvisBottomNavigation(
                currentTab = BottomNavTab.TOOLS,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> onNavigateHome()
                        BottomNavTab.MEMORY -> onNavigateMemory()
                        BottomNavTab.TOOLS -> { /* Already on tasks/tools */ }
                        BottomNavTab.PROFILE -> onNavigateProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Segmented Tabs: Tasks | Reminders
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(JarvisSurface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == "Tasks") JarvisBrightCyan else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = JarvisPrimaryCyan),
                            onClick = { selectedTab = "Tasks" }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tasks",
                        color = if (selectedTab == "Tasks") JarvisBackground else JarvisSecondaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedTab == "Reminders") JarvisBrightCyan else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = JarvisPrimaryCyan),
                            onClick = { selectedTab = "Reminders" }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reminders",
                        color = if (selectedTab == "Reminders") JarvisBackground else JarvisSecondaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val displayList = if (selectedTab == "Reminders") {
                taskList.filter { it.timeLabel != "No due date" }
            } else {
                taskList
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayList, key = { it.id }) { item ->
                    TaskCardRow(
                        item = item,
                        onToggleComplete = {
                            val idx = taskList.indexOfFirst { it.id == item.id }
                            if (idx != -1) {
                                taskList[idx] = item.copy(isCompleted = !item.isCompleted)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun TaskCardRow(
    item: TaskItem,
    onToggleComplete: () -> Unit
) {
    val categoryIcon: ImageVector = when (item.category) {
        "shopping" -> Icons.Default.ShoppingCart
        "work" -> Icons.Default.Assignment
        "call" -> Icons.Default.Phone
        "travel" -> Icons.Default.Flight
        "reading" -> Icons.Default.Book
        "fitness" -> Icons.Default.FitnessCenter
        "meeting" -> Icons.Default.People
        else -> Icons.Default.Notifications
    }

    val iconBg = if (item.isCompleted) JarvisBrightCyan.copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f)
    val iconTint = if (item.isCompleted) JarvisBrightCyan else JarvisPrimaryCyan

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(JarvisSurface)
            .border(1.dp, if (item.isCompleted) JarvisBorderCyan else JarvisBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = JarvisPrimaryCyan),
                onClick = onToggleComplete
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (item.isCompleted) JarvisBrightCyan else Color.Transparent)
                .border(2.dp, if (item.isCompleted) JarvisBrightCyan else JarvisSecondaryText.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = JarvisBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Category Icon Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Time
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = if (item.isCompleted) JarvisSecondaryText else JarvisWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.timeLabel,
                color = JarvisSecondaryText,
                fontSize = 12.sp
            )
        }

        // Notification Bell Icon
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification",
            tint = if (item.isCompleted) Color.Transparent else JarvisSecondaryText.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}
