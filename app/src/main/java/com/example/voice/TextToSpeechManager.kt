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
    private var isInitialized = false
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate = 1.05f
    private var speechPitch = 0.95f // Slightly deeper, more technical robotic timbre

    init {
        tts = TextToSpeech(context.applicationContext, this)
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

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onStateChange(false)
                    Log.e(TAG, "TTS Utterance error for id: $utteranceId")
                }
            })

            isInitialized = true
            Log.d(TAG, "TTS successfully initialized")
        } else {
            Log.e(TAG, "TTS initialization failed with code: $status")
            isInitialized = false
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(speechRate)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setPitch(speechPitch)
        }
    }

    fun speak(text: String, flush: Boolean = true): Boolean {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "Cannot speak: TTS not initialized yet")
            return false
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = tts?.speak(text, queueMode, params, utteranceId)
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
        _isSpeaking.value = false
        onStateChange(false)
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
