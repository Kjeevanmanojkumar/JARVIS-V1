package com.example.ui.hud

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiEngine
import com.example.ai.DebugInfo
import com.example.ai.GeminiAiProvider
import com.example.ai.LocalRuleProvider
import com.example.database.JarvisDatabase
import com.example.database.entity.ConversationEntity
import com.example.database.entity.MemoryEntity
import com.example.database.entity.MessageEntity
import com.example.database.repository.ConversationRepository
import com.example.database.repository.MemoryRepository
import com.example.model.JarvisState
import com.example.model.MemoryCategory
import com.example.model.SystemTelemetry
import com.example.telemetry.SystemMonitor
import com.example.tools.ToolRegistry
import com.example.ui.settings.JarvisSettings
import com.example.ui.settings.SettingsRepository
import com.example.voice.TextToSpeechManager
import com.example.voice.whisper.WhisperModelStatus
import com.example.voice.whisper.WhisperSpeechEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getInstance(application)
    val conversationRepository = ConversationRepository(database.conversationDao(), database.messageDao())
    val memoryRepository = MemoryRepository(database.memoryDao())
    val settingsRepository = SettingsRepository(application)
    val toolRegistry = ToolRegistry(application, memoryRepository)

    private val geminiProvider = GeminiAiProvider()
    private val localProvider = LocalRuleProvider()

    val aiEngine = AiEngine(
        conversationRepository = conversationRepository,
        memoryRepository = memoryRepository,
        toolRegistry = toolRegistry,
        geminiProvider = geminiProvider,
        localProvider = localProvider
    )

    private val systemMonitor = SystemMonitor(application, viewModelScope)

    // State flows
    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _latestUserText = MutableStateFlow("")
    val latestUserText: StateFlow<String> = _latestUserText.asStateFlow()

    private val _latestResponseText = MutableStateFlow("All systems calibrated. Tap the core or speak to initiate protocol.")
    val latestResponseText: StateFlow<String> = _latestResponseText.asStateFlow()

    private val _activeConversationId = MutableStateFlow<Long>(1L)
    val activeConversationId: StateFlow<Long> = _activeConversationId.asStateFlow()

    val telemetry: StateFlow<SystemTelemetry> = systemMonitor.telemetry
    val settings: StateFlow<JarvisSettings> = settingsRepository.settings
    val debugInfo: StateFlow<DebugInfo> = aiEngine.debugInfo

    val allConversations: StateFlow<List<ConversationEntity>> = conversationRepository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryEntity>> = memoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMessages: StateFlow<List<MessageEntity>> = conversationRepository.getGlobalRecentMessages(8)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice Engines
    private var whisperSpeechEngine: WhisperSpeechEngine? = null
    private var textToSpeechManager: TextToSpeechManager? = null

    val whisperModelStatus: StateFlow<WhisperModelStatus> get() =
        whisperSpeechEngine?.modelStatus ?: MutableStateFlow(WhisperModelStatus.NotDownloaded).asStateFlow()

    // Guard against duplicate concurrent command execution
    private val isExecutingCommand = AtomicBoolean(false)

    init {
        systemMonitor.start()
        initializeConversation()
        initTextToSpeech()
        initWhisperEngine()
    }

    private fun initializeConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val convId = conversationRepository.getOrCreateActiveConversation()
            _activeConversationId.value = convId
        }
    }

    private fun initTextToSpeech() {
        textToSpeechManager = TextToSpeechManager(getApplication()) { isSpeaking ->
            if (isSpeaking) {
                _jarvisState.value = JarvisState.SPEAKING
            } else {
                if (_jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }.apply {
            setSpeechRate(settings.value.speechRate)
            setSpeechPitch(settings.value.speechPitch)
        }
    }

    private fun initWhisperEngine() {
        whisperSpeechEngine = WhisperSpeechEngine(
            context = getApplication(),
            scope = viewModelScope,
            onResult = { recognizedText ->
                _audioLevel.value = 0f
                submitCommand(recognizedText)
            },
            onError = { errorMsg ->
                _audioLevel.value = 0f
                _jarvisState.value = JarvisState.ERROR
                _latestResponseText.value = "Voice input error: $errorMsg"
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2500)
                    if (_jarvisState.value == JarvisState.ERROR) {
                        _jarvisState.value = JarvisState.IDLE
                    }
                }
            },
            onRmsChangedCallback = { level ->
                _audioLevel.value = level
            },
            onListeningStateChanged = { isListening ->
                if (isListening) {
                    _jarvisState.value = JarvisState.LISTENING
                } else if (_jarvisState.value == JarvisState.LISTENING) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        )
    }

    fun downloadWhisperModel(onProgress: ((Float) -> Unit)? = null, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = whisperSpeechEngine?.modelManager?.downloadModel(onProgress) ?: false
            onComplete?.invoke(success)
        }
    }

    fun toggleListening() {
        triggerHaptic()
        if (_jarvisState.value == JarvisState.LISTENING) {
            whisperSpeechEngine?.stopListening()
            _jarvisState.value = JarvisState.IDLE
        } else {
            // Stop TTS if speaking before listening
            textToSpeechManager?.stop()
            _jarvisState.value = JarvisState.LISTENING
            whisperSpeechEngine?.startListening()
        }
    }

    fun submitCommand(commandText: String) {
        val trimmed = commandText.trim()
        if (trimmed.isBlank()) return

        // Stop any current voice operations
        whisperSpeechEngine?.stopListening()
        textToSpeechManager?.stop()

        // Handle direct stop speaking command
        val cleanCmd = trimmed.trimEnd('.', '!', '?', ',', ';').lowercase()
        if (cleanCmd == "stop speaking" || cleanCmd == "stop talking" || cleanCmd == "silence") {
            stopSpeaking()
            _latestUserText.value = trimmed
            _latestResponseText.value = "Speech transmission halted."
            _jarvisState.value = JarvisState.IDLE
            return
        }

        // Prevent duplicate concurrent command submissions
        if (!isExecutingCommand.compareAndSet(false, true)) {
            Log.w(TAG, "Command execution already in progress, dropping duplicate submit: $trimmed")
            return
        }

        triggerHaptic()
        _latestUserText.value = trimmed
        _jarvisState.value = JarvisState.THINKING

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val convId = _activeConversationId.value
                val currentSettings = settings.value
                val isOnline = telemetry.value.isOnline

                aiEngine.processCommand(
                    userText = trimmed,
                    conversationId = convId,
                    apiKey = currentSettings.apiKey,
                    modelName = currentSettings.modelName,
                    customPrompt = currentSettings.customSystemPrompt,
                    isOnline = isOnline,
                    onStateChanged = { newState ->
                        _jarvisState.value = newState
                    },
                    onResponseReady = { responseText, speakText ->
                        _latestResponseText.value = responseText

                        if (currentSettings.voiceEnabled) {
                            val spoke = textToSpeechManager?.speak(cleanTextForSpeech(speakText)) ?: false
                            if (!spoke) {
                                // Fallback to IDLE if TTS failed to synthesize
                                _jarvisState.value = JarvisState.IDLE
                            }
                        } else {
                            _jarvisState.value = JarvisState.IDLE
                        }
                    }
                )
            } finally {
                isExecutingCommand.set(false)
            }
        }
    }

    fun stopSpeaking() {
        textToSpeechManager?.stop()
        if (_jarvisState.value == JarvisState.SPEAKING) {
            _jarvisState.value = JarvisState.IDLE
        }
    }

    fun startNewConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = conversationRepository.createNewConversation()
            _activeConversationId.value = newId
            _latestResponseText.value = "New session started. System ready."
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            conversationRepository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                initializeConversation()
            }
        }
    }

    fun saveMemoryManual(key: String, value: String, category: MemoryCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryRepository.saveMemory(key, value, category)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryRepository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            memoryRepository.clearAll()
        }
    }

    fun updateApiKey(key: String) = settingsRepository.updateApiKey(key)
    fun updateModelName(model: String) = settingsRepository.updateModelName(model)
    fun updateCustomPrompt(prompt: String) = settingsRepository.updateCustomPrompt(prompt)
    fun setVoiceEnabled(enabled: Boolean) = settingsRepository.setVoiceEnabled(enabled)
    fun setSpeechRate(rate: Float) {
        settingsRepository.setSpeechRate(rate)
        textToSpeechManager?.setSpeechRate(rate)
    }
    fun setSpeechPitch(pitch: Float) {
        settingsRepository.setSpeechPitch(pitch)
        textToSpeechManager?.setSpeechPitch(pitch)
    }
    fun setDebugMode(enabled: Boolean) = settingsRepository.setDebugMode(enabled)
    fun markFirstRunComplete() = settingsRepository.markFirstRunComplete()

    private fun triggerHaptic() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (ignored: Exception) {}
    }

    private fun cleanTextForSpeech(text: String): String {
        return text.replace("•", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("{\"tool\":.*}".toRegex(), "")
            .trim()
    }

    override fun onCleared() {
        super.onCleared()
        whisperSpeechEngine?.destroy()
        textToSpeechManager?.destroy()
        systemMonitor.stop()
    }

    companion object {
        private const val TAG = "JarvisViewModel"
    }
}
