package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
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
import com.example.ui.conversation.ConversationListScreen
import com.example.ui.hud.JarvisHudScreen
import com.example.ui.hud.JarvisViewModel
import com.example.ui.memory.MemoryScreen
import com.example.ui.onboarding.DiagnosticScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisTheme

enum class Screen {
    HUD,
    HISTORY,
    MEMORY,
    SETTINGS,
    DIAGNOSTIC
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
                    color = JarvisObsidian
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
    var currentScreen by remember(settings.firstRunCompleted) {
        mutableStateOf(if (settings.firstRunCompleted) Screen.HUD else Screen.DIAGNOSTIC)
    }

    Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            Screen.DIAGNOSTIC -> {
                DiagnosticScreen(
                    viewModel = viewModel,
                    onComplete = { currentScreen = Screen.HUD }
                )
            }
            Screen.HUD -> {
                JarvisHudScreen(
                    viewModel = viewModel,
                    onNavigateHistory = { currentScreen = Screen.HISTORY },
                    onNavigateMemory = { currentScreen = Screen.MEMORY },
                    onNavigateSettings = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.HISTORY -> {
                ConversationListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.HUD }
                )
            }
            Screen.MEMORY -> {
                MemoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.HUD }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.HUD }
                )
            }
        }
    }
}
