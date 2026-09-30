package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.chat.ChatScreen
import com.example.ui.components.VoiceOverlay
import com.example.ui.create.CreateImageScreen
import com.example.ui.home.HomeScreen
import com.example.ui.hud.JarvisHudScreen
import com.example.ui.hud.JarvisViewModel
import com.example.ui.memory.MemoryScreen
import com.example.ui.onboarding.DiagnosticScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.tasks.CreateReminderScreen
import com.example.ui.tasks.TasksScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisTheme
import com.example.ui.vision.VisionScreen
import com.example.ui.voice.VoiceFullscreenScreen

enum class Screen {
    HOME,
    CHAT,
    VISION,
    CREATE,
    TASKS,
    CREATE_REMINDER,
    VOICE_FULLSCREEN,
    SETTINGS,
    MEMORY,
    DIAGNOSTIC,
    HUD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    color = JarvisBackground
                ) {
                    JarvisAppContent()
                }
            }
        }
    }
}

@Composable
fun JarvisAppContent(viewModel: JarvisViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val jarvisState by viewModel.jarvisState.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()

    var currentScreen by remember(settings.firstRunCompleted) {
        mutableStateOf(if (settings.firstRunCompleted) Screen.HOME else Screen.DIAGNOSTIC)
    }

    var showVoiceOverlay by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
            when (screen) {
                Screen.DIAGNOSTIC -> {
                    DiagnosticScreen(
                        viewModel = viewModel,
                        onComplete = { currentScreen = Screen.HOME }
                    )
                }
                Screen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateChat = { currentScreen = Screen.CHAT },
                        onNavigateTasks = { currentScreen = Screen.TASKS },
                        onNavigateVision = { currentScreen = Screen.VISION },
                        onNavigateCreate = { currentScreen = Screen.CREATE },
                        onNavigateSettings = { currentScreen = Screen.SETTINGS },
                        onNavigateMemory = { currentScreen = Screen.MEMORY },
                        onOpenVoiceFullscreen = { currentScreen = Screen.VOICE_FULLSCREEN },
                        onOpenVoiceOverlay = { showVoiceOverlay = true }
                    )
                }
                Screen.CHAT -> {
                    ChatScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateHome = { currentScreen = Screen.HOME },
                        onNavigateMemory = { currentScreen = Screen.MEMORY },
                        onNavigateTools = { currentScreen = Screen.TASKS },
                        onNavigateProfile = { currentScreen = Screen.SETTINGS },
                        onOpenVoiceFullscreen = { currentScreen = Screen.VOICE_FULLSCREEN }
                    )
                }
                Screen.VISION -> {
                    VisionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onOpenVoiceFullscreen = { currentScreen = Screen.VOICE_FULLSCREEN }
                    )
                }
                Screen.CREATE -> {
                    CreateImageScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
                Screen.TASKS -> {
                    TasksScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateCreateReminder = { currentScreen = Screen.CREATE_REMINDER },
                        onNavigateHome = { currentScreen = Screen.HOME },
                        onNavigateMemory = { currentScreen = Screen.MEMORY },
                        onNavigateProfile = { currentScreen = Screen.SETTINGS }
                    )
                }
                Screen.CREATE_REMINDER -> {
                    CreateReminderScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.TASKS }
                    )
                }
                Screen.VOICE_FULLSCREEN -> {
                    VoiceFullscreenScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
                Screen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
                Screen.MEMORY -> {
                    MemoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
                Screen.HUD -> {
                    JarvisHudScreen(
                        viewModel = viewModel,
                        onNavigateHistory = { currentScreen = Screen.CHAT },
                        onNavigateMemory = { currentScreen = Screen.MEMORY },
                        onNavigateSettings = { currentScreen = Screen.SETTINGS }
                    )
                }
            }
        }

        // Floating Voice Overlay (Reference Screen 8)
        VoiceOverlay(
            visible = showVoiceOverlay,
            state = jarvisState,
            audioLevel = audioLevel,
            onDismiss = { showVoiceOverlay = false },
            onToggleMic = { viewModel.toggleListening() },
            onOpenKeyboard = {
                showVoiceOverlay = false
                currentScreen = Screen.CHAT
            }
        )
    }
}
