package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(
    private val context: Context,
    private val onStateChange: (isSpeaking: Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()

    private var speechRate = 1.05f
    private var speechPitch = 0.95f

    // Pending utterance queue in case speak() is invoked before onInit completes
    private val pendingQueue = mutableListOf<String>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech", e)
            _initError.value = "Failed to instantiate TTS service: ${e.message}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale US missing data or not supported, falling back to default")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(speechPitch)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    onStateChange(true)
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onStateChange(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onStateChange(false)
                    Log.e(TAG, "TTS Utterance error for id: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    onStateChange(false)
                    Log.e(TAG, "TTS Utterance error code $errorCode for id: $utteranceId")
                }
            })

            _isInitialized.value = true
            _initError.value = null
            Log.d(TAG, "TextToSpeech successfully initialized")

            // Process any pending utterances
            synchronized(pendingQueue) {
                if (pendingQueue.isNotEmpty()) {
                    val queued = pendingQueue.toList()
                    pendingQueue.clear()
                    for (text in queued) {
                        speak(text, flush = false)
                    }
                }
            }
        } else {
            val errMsg = "TextToSpeech initialization failed with code: $status"
            Log.e(TAG, errMsg)
            _isInitialized.value = false
            _initError.value = errMsg
            synchronized(pendingQueue) {
                pendingQueue.clear()
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        if (_isInitialized.value) {
            tts?.setSpeechRate(speechRate)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        if (_isInitialized.value) {
            tts?.setPitch(speechPitch)
        }
    }

    fun speak(text: String, flush: Boolean = true): Boolean {
        val clean = text.trim()
        if (clean.isEmpty()) return false

        if (!_isInitialized.value) {
            Log.d(TAG, "TTS not initialized yet. Enqueueing utterance for deferred playback.")
            synchronized(pendingQueue) {
                if (flush) {
                    pendingQueue.clear()
                }
                pendingQueue.add(clean)
            }
            return true
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = tts?.speak(clean, queueMode, params, utteranceId)
        val success = result == TextToSpeech.SUCCESS
        if (!success) {
            Log.w(TAG, "TTS speak call returned non-success code: $result")
            _isSpeaking.value = false
            onStateChange(false)
        }
        return success
    }

    fun stop() {
        if (_isInitialized.value) {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping TTS", e)
            }
        }
        synchronized(pendingQueue) {
            pendingQueue.clear()
        }
        _isSpeaking.value = false
        onStateChange(false)
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
