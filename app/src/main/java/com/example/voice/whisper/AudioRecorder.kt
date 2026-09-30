package com.example.voice.whisper

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecorder(
    private val context: Context,
    private val onAudioLevelChanged: (Float) -> Unit,
    private val onSilenceTimeout: () -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val isRecording = AtomicBoolean(false)

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    fun isCurrentlyRecording(): Boolean = isRecording.get()

    @SuppressLint("MissingPermission")
    fun startRecording(
        scope: CoroutineScope,
        onAudioCaptured: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (!isRecording.compareAndSet(false, true)) {
            Log.w(TAG, "Audio recording already in progress, ignoring duplicate start request")
            return false
        }

        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            isRecording.set(false)
            onError("Unsupported audio hardware buffer")
            return false
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            } catch (e: Exception) {
                Log.d(TAG, "VOICE_RECOGNITION source unavailable, falling back to MIC")
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                isRecording.set(false)
                onError("Failed to initialize microphone hardware")
                return false
            }

            audioRecord?.startRecording()
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating audio recording", e)
            releaseAudioRecord()
            isRecording.set(false)
            onError("Microphone error: ${e.message}")
            return false
        }

        recordingJob = scope.launch(Dispatchers.IO) {
            val audioStream = ByteArrayOutputStream()
            val buffer = ShortArray(bufferSize / 2)
            var hasDetectedSpeech = false
            var silenceStartTime = 0L
            val recordingStartTime = System.currentTimeMillis()

            try {
                while (isActive && isRecording.get()) {
                    val readSamples = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readSamples <= 0) continue

                    // Convert ShortArray to ByteArray (little-endian 16-bit PCM)
                    val byteBuffer = ByteBuffer.allocate(readSamples * 2).order(ByteOrder.LITTLE_ENDIAN)
                    var sumSquare = 0.0
                    for (i in 0 until readSamples) {
                        val sample = buffer[i]
                        byteBuffer.putShort(sample)
                        sumSquare += sample * sample
                    }
                    audioStream.write(byteBuffer.array())

                    // Calculate RMS dB and normalized audio level (0f..1f)
                    val rms = sqrt(sumSquare / readSamples)
                    val rmsDb = if (rms > 0) 20 * log10(rms / 32767.0) else -100.0
                    val normalizedLevel = ((rmsDb + 50.0) / 50.0).coerceIn(0.0, 1.0).toFloat()
                    onAudioLevelChanged(normalizedLevel)

                    val now = System.currentTimeMillis()

                    // Voice Activity Detection (VAD)
                    if (normalizedLevel > 0.15f) {
                        hasDetectedSpeech = true
                        silenceStartTime = 0L
                    } else if (hasDetectedSpeech) {
                        if (silenceStartTime == 0L) {
                            silenceStartTime = now
                        } else if (now - silenceStartTime > 1800) { // 1.8 seconds silence after speech
                            Log.d(TAG, "Silence detected after speech, finalizing utterance")
                            break
                        }
                    } else {
                        // Max initial wait for speech: 7 seconds
                        if (now - recordingStartTime > 7000) {
                            Log.d(TAG, "No speech detected within 7 seconds")
                            onSilenceTimeout()
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during audio capture loop", e)
            } finally {
                releaseAudioRecord()
                isRecording.set(false)
                onAudioLevelChanged(0f)

                val capturedData = audioStream.toByteArray()
                audioStream.close()
                onAudioCaptured(capturedData)
            }
        }

        return true
    }

    fun stopRecording() {
        if (isRecording.compareAndSet(true, false)) {
            recordingJob?.cancel()
            releaseAudioRecord()
            onAudioLevelChanged(0f)
        }
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio recorder", e)
        } finally {
            audioRecord = null
        }
    }

    companion object {
        private const val TAG = "AudioRecorder"

        /**
         * Helper to wrap raw 16kHz 16-bit mono PCM into standard WAV header
         */
        fun pcmToWav(pcmData: ByteArray, sampleRate: Int = 16000, channels: Short = 1, bitsPerSample: Short = 16): ByteArray {
            val totalAudioLen = pcmData.size
            val totalDataLen = totalAudioLen + 36
            val byteRate = sampleRate * channels * bitsPerSample / 8

            val header = ByteArray(44)
            // RIFF chunk descriptor
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            // fmt sub-chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 16 for PCM
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // AudioFormat 1 = PCM
            header[21] = 0
            header[22] = (channels.toInt() and 0xff).toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = ((channels * bitsPerSample / 8) and 0xff).toByte() // block align
            header[33] = 0
            header[34] = (bitsPerSample.toInt() and 0xff).toByte()
            header[35] = 0
            // data sub-chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            return header + pcmData
        }
    }
}
