package com.example.voice.whisper

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class WhisperModelStatus {
    object NotDownloaded : WhisperModelStatus()
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : WhisperModelStatus()
    data class Ready(val modelFile: File, val sizeMb: Float) : WhisperModelStatus()
    data class InsufficientStorage(val availableMb: Long, val requiredMb: Long) : WhisperModelStatus()
    data class Error(val message: String) : WhisperModelStatus()
}

class WhisperModelManager(private val context: Context) {

    private val _status = MutableStateFlow<WhisperModelStatus>(WhisperModelStatus.NotDownloaded)
    val status: StateFlow<WhisperModelStatus> = _status.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val modelDir: File get() = File(context.filesDir, "whisper").apply { if (!exists()) mkdirs() }
    val modelFile: File get() = File(modelDir, MODEL_FILE_NAME)

    init {
        checkLocalModel()
    }

    fun checkLocalModel() {
        if (modelFile.exists() && modelFile.length() > 1024 * 1024) { // At least 1MB
            val sizeMb = modelFile.length() / (1024f * 1024f)
            _status.value = WhisperModelStatus.Ready(modelFile, sizeMb)
            Log.d(TAG, "Whisper model ready at: ${modelFile.absolutePath} ($sizeMb MB)")
        } else {
            _status.value = WhisperModelStatus.NotDownloaded
            Log.d(TAG, "Whisper model not present locally")
        }
    }

    suspend fun downloadModel(onProgress: ((Float) -> Unit)? = null): Boolean = withContext(Dispatchers.IO) {
        val requiredBytes = ESTIMATED_MODEL_BYTES
        val availableBytes = getAvailableStorageBytes()

        if (availableBytes < requiredBytes * 2) {
            val availMb = availableBytes / (1024 * 1024)
            val reqMb = (requiredBytes * 2) / (1024 * 1024)
            _status.value = WhisperModelStatus.InsufficientStorage(availMb, reqMb)
            Log.e(TAG, "Insufficient storage for Whisper model: $availMb MB available, $reqMb MB required")
            return@withContext false
        }

        _status.value = WhisperModelStatus.Downloading(0f, 0L, requiredBytes)
        val tempFile = File(modelDir, "$MODEL_FILE_NAME.tmp")

        try {
            val request = Request.Builder()
                .url(MODEL_DOWNLOAD_URL)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = "Download failed with HTTP ${response.code}: ${response.message}"
                Log.e(TAG, errorMsg)
                _status.value = WhisperModelStatus.Error(errorMsg)
                return@withContext false
            }

            val body = response.body ?: throw IllegalStateException("Empty response body from model server")
            val totalBytes = if (body.contentLength() > 0) body.contentLength() else requiredBytes

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L
                    var lastReportTime = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastReportTime > 200 || totalDownloaded == totalBytes) {
                            lastReportTime = now
                            val progress = (totalDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f)
                            _status.value = WhisperModelStatus.Downloading(progress, totalDownloaded, totalBytes)
                            onProgress?.invoke(progress)
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 1024 * 1024) {
                if (modelFile.exists()) modelFile.delete()
                tempFile.renameTo(modelFile)
                val sizeMb = modelFile.length() / (1024f * 1024f)
                _status.value = WhisperModelStatus.Ready(modelFile, sizeMb)
                Log.d(TAG, "Whisper model successfully installed: $sizeMb MB")
                return@withContext true
            } else {
                throw IllegalStateException("Downloaded model file is corrupted or too small")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading Whisper model", e)
            if (tempFile.exists()) tempFile.delete()
            _status.value = WhisperModelStatus.Error(e.message ?: "Model download failed")
            return@withContext false
        }
    }

    fun deleteModel(): Boolean {
        return try {
            if (modelFile.exists()) {
                val deleted = modelFile.delete()
                checkLocalModel()
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting Whisper model", e)
            false
        }
    }

    private fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBytes
        } catch (e: Exception) {
            1024L * 1024 * 500 // Assume 500MB fallback
        }
    }

    companion object {
        private const val TAG = "WhisperModelManager"
        const val MODEL_NAME = "Whisper Tiny (English)"
        private const val MODEL_FILE_NAME = "ggml-tiny.en.bin"
        private const val ESTIMATED_MODEL_BYTES = 39L * 1024 * 1024 // ~39 MB
        // Standard high-speed mirror for whisper.cpp ggml-tiny model
        private const val MODEL_DOWNLOAD_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin"
    }
}
