package com.example.ui.memory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MemoryCategory
import com.example.ui.hud.JarvisViewModel
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGlassSurface
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisOnlineGreen
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisWarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val memories by viewModel.allMemories.collectAsState()
    var selectedCategory by remember { mutableStateOf<MemoryCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredMemories = remember(memories, selectedCategory) {
        if (selectedCategory == null) memories
        else memories.filter { it.category == selectedCategory!!.name }
    }

    Scaffold(
        containerColor = JarvisObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEURAL MEMORY BANK",
                        color = JarvisCyan,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JarvisCyan
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Memory", tint = JarvisCyan)
                    }
                    if (memories.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllMemories() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = JarvisAlertRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisGlassSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Category filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = "ALL (${memories.size})",
                        isSelected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                }
                items(MemoryCategory.entries) { cat ->
                    val count = memories.count { it.category == cat.name }
                    CategoryChip(
                        label = "${cat.code} ($count)",
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            if (filteredMemories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = JarvisTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO MEMORY ENTRIES STORED",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Say 'Remember that my project is called Myraa'",
                            color = JarvisCyanLight.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMemories, key = { it.id }) { memory ->
                        val cat = MemoryCategory.fromString(memory.category)
                        val catColor = when (cat) {
                            MemoryCategory.PROJECT -> JarvisCyan
                            MemoryCategory.USER_PREFERENCE -> JarvisOnlineGreen
                            MemoryCategory.PERSONAL_CONTEXT -> JarvisWarningAmber
                            MemoryCategory.TASK -> Color(0xFFFF80AB)
                            MemoryCategory.GENERAL -> JarvisTextSecondary
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(JarvisGlassSurface, shape = RoundedCornerShape(4.dp))
                                .border(1.dp, JarvisGlassBorder, shape = RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(catColor.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp))
                                                .border(1.dp, catColor, shape = RoundedCornerShape(2.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = cat.displayName.uppercase(),
                                                color = catColor,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = memory.memoryKey.uppercase(),
                                            color = JarvisTextPrimary,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = memory.memoryValue,
                                        color = JarvisCyanLight,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteMemory(memory.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Memory",
                                        tint = JarvisAlertRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var keyInput by remember { mutableStateOf("") }
        var valueInput by remember { mutableStateOf("") }
        var categoryChoice by remember { mutableStateOf(MemoryCategory.GENERAL) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = JarvisGlassSurface,
            title = {
                Text(
                    "COMMIT MEMORY ENTRY",
                    color = JarvisCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Subject / Key (e.g. project)", color = JarvisTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisGlassBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("Information to Remember", color = JarvisTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisGlassBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                            viewModel.saveMemoryManual(keyInput, valueInput, categoryChoice)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                ) {
                    Text("STORE", color = JarvisObsidian, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(if (isSelected) JarvisCyan else JarvisGlassSurface, shape = RoundedCornerShape(2.dp))
            .border(1.dp, if (isSelected) JarvisCyan else JarvisGlassBorder, shape = RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) JarvisObsidian else JarvisCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
