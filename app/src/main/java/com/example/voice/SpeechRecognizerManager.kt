package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRmsChangedCallback: (Float) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
            _isListening.value = true
            onListeningStateChanged(true)
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // rmsdB is usually between -2 and 10 dB
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            onRmsChangedCallback(normalized)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            _isListening.value = false
            onListeningStateChanged(false)
            onRmsChangedCallback(0f)
        }

        override fun onError(error: Int) {
            _isListening.value = false
            onListeningStateChanged(false)
            onRmsChangedCallback(0f)

            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                else -> "Recognition error (code $error)"
            }
            Log.w(TAG, "SpeechRecognizer error: $errorMessage")
            onError(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            onListeningStateChanged(false)
            onRmsChangedCallback(0f)

            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
                Log.d(TAG, "Recognized: $recognizedText")
                onResult(recognizedText)
            } else {
                onError("No speech recognized")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!isAvailable()) {
            onError("Speech recognition service not available on this device")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
            onListeningStateChanged(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _isListening.value = false
            onListeningStateChanged(false)
            onError("Failed to initiate voice listener: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up speech recognizer", e)
        } finally {
            speechRecognizer = null
            _isListening.value = false
            onListeningStateChanged(false)
            onRmsChangedCallback(0f)
        }
    }

    fun destroy() {
        stopListening()
    }

    companion object {
        private const val TAG = "SpeechRecognizerManager"
    }
}
