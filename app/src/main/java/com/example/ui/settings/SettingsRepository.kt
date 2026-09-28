package com.example.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisSettings(
    val apiKey: String,
    val modelName: String,
    val customSystemPrompt: String,
    val voiceEnabled: Boolean,
    val speechRate: Float,
    val speechPitch: Float,
    val debugModeEnabled: Boolean,
    val firstRunCompleted: Boolean
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<JarvisSettings> = _settings.asStateFlow()

    private fun loadSettings(): JarvisSettings {
        val defaultKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null)?.toString() ?: ""
        } catch (ignored: Exception) {
            ""
        }
        val storedKey = prefs.getString(KEY_API_KEY, null)
        val resolvedKey = if (!storedKey.isNullOrBlank()) storedKey else defaultKey

        return JarvisSettings(
            apiKey = resolvedKey,
            modelName = prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash",
            customSystemPrompt = prefs.getString(KEY_PROMPT, "") ?: "",
            voiceEnabled = prefs.getBoolean(KEY_VOICE_ENABLED, true),
            speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.05f),
            speechPitch = prefs.getFloat(KEY_SPEECH_PITCH, 0.95f),
            debugModeEnabled = prefs.getBoolean(KEY_DEBUG_MODE, false),
            firstRunCompleted = prefs.getBoolean(KEY_FIRST_RUN, false)
        )
    }

    fun updateApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _settings.value = _settings.value.copy(apiKey = key.trim())
    }

    fun updateModelName(model: String) {
        prefs.edit().putString(KEY_MODEL, model.trim()).apply()
        _settings.value = _settings.value.copy(modelName = model.trim())
    }

    fun updateCustomPrompt(prompt: String) {
        prefs.edit().putString(KEY_PROMPT, prompt).apply()
        _settings.value = _settings.value.copy(customSystemPrompt = prompt)
    }

    fun setVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(voiceEnabled = enabled)
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _settings.value = _settings.value.copy(speechRate = rate)
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
        _settings.value = _settings.value.copy(speechPitch = pitch)
    }

    fun setDebugMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
        _settings.value = _settings.value.copy(debugModeEnabled = enabled)
    }

    fun markFirstRunComplete() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
        _settings.value = _settings.value.copy(firstRunCompleted = true)
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model_name"
        private const val KEY_PROMPT = "system_prompt"
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_DEBUG_MODE = "debug_mode"
        private const val KEY_FIRST_RUN = "first_run"
    }
}
