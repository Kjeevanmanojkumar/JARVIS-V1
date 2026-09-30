package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiEngine
import com.example.ai.GeminiAiProvider
import com.example.ai.LocalRuleProvider
import com.example.database.JarvisDatabase
import com.example.database.entity.MessageEntity
import com.example.database.repository.ConversationRepository
import com.example.database.repository.MemoryRepository
import com.example.model.MemoryCategory
import com.example.tools.AlarmTool
import com.example.tools.CallTool
import com.example.tools.DeviceStatusTool
import com.example.tools.ManageMemoryTool
import com.example.tools.MediaControlTool
import com.example.tools.OpenAppTool
import com.example.tools.OpenUrlTool
import com.example.tools.SettingsTool
import com.example.tools.TimeDateTool
import com.example.tools.TimerTool
import com.example.tools.ToolRegistry
import com.example.tools.WebSearchTool
import com.example.voice.whisper.AudioRecorder
import com.example.voice.whisper.WhisperModelManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JarvisV1TestSuite {

    private lateinit var context: Context
    private lateinit var database: JarvisDatabase
    private lateinit var memoryRepository: MemoryRepository
    private lateinit var conversationRepository: ConversationRepository
    private lateinit var toolRegistry: ToolRegistry
    private lateinit var localRuleProvider: LocalRuleProvider

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, JarvisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memoryRepository = MemoryRepository(database.memoryDao())
        conversationRepository = ConversationRepository(database.conversationDao(), database.messageDao())
        toolRegistry = ToolRegistry(context, memoryRepository)
        localRuleProvider = LocalRuleProvider()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testOpenAppTool_whatsAppNotInstalledReturnsClearMessage() = runBlocking {
        val tool = OpenAppTool(context)
        val result = tool.execute(mapOf("app_name" to "WhatsApp."))
        assertFalse(result.success)
        assertEquals("WhatsApp is not installed on this device.", result.message)
    }

    @Test
    fun testOpenAppTool_cameraAndSettings() = runBlocking {
        val tool = OpenAppTool(context)
        val cameraResult = tool.execute(mapOf("app_name" to "Camera"))
        assertTrue(cameraResult.success)
        assertEquals("Opening Camera.", cameraResult.message)

        val settingsResult = tool.execute(mapOf("app_name" to "Settings"))
        assertTrue(settingsResult.success)
        assertEquals("Opening Settings.", settingsResult.message)
    }

    @Test
    fun testAlarmTool_validation() = runBlocking {
        val tool = AlarmTool(context)
        val invalidHour = tool.execute(mapOf("hour" to 25, "minutes" to 0))
        assertFalse(invalidHour.success)

        val invalidMin = tool.execute(mapOf("hour" to 6, "minutes" to 65))
        assertFalse(invalidMin.success)

        val validAlarm = tool.execute(mapOf("hour" to 6, "minutes" to 0, "label" to "Morning Alarm"))
        assertTrue(validAlarm.success)
        assertTrue(validAlarm.message.contains("Alarm scheduled for 06:00"))
    }

    @Test
    fun testTimerTool_validation() = runBlocking {
        val tool = TimerTool(context)
        val invalidTimer = tool.execute(mapOf("duration_seconds" to 0))
        assertFalse(invalidTimer.success)

        val validTimer = tool.execute(mapOf("duration_seconds" to 600, "label" to "Tea Timer"))
        assertTrue(validTimer.success)
        assertTrue(validTimer.message.contains("10 minute(s)"))
    }

    @Test
    fun testDeviceStatusTool() = runBlocking {
        val tool = DeviceStatusTool(context)
        val batteryResult = tool.execute(mapOf("metric" to "battery"))
        assertTrue(batteryResult.success)
        assertTrue(batteryResult.message.contains("Battery"))

        val allResult = tool.execute(mapOf("metric" to "all"))
        assertTrue(allResult.success)
        assertTrue(allResult.message.contains("Device Telemetry"))
    }

    @Test
    fun testTimeDateTool() = runBlocking {
        val tool = TimeDateTool()
        val timeResult = tool.execute(mapOf("query_type" to "time"))
        assertTrue(timeResult.success)
        assertTrue(timeResult.message.contains("The current local time is"))

        val dateResult = tool.execute(mapOf("query_type" to "date"))
        assertTrue(dateResult.success)
        assertTrue(dateResult.message.contains("Today is"))
    }

    @Test
    fun testSettingsTool() = runBlocking {
        val tool = SettingsTool(context)
        val wifiResult = tool.execute(mapOf("setting_type" to "wifi"))
        assertTrue(wifiResult.success)
        assertTrue(wifiResult.message.contains("wifi"))

        val btResult = tool.execute(mapOf("setting_type" to "bluetooth"))
        assertTrue(btResult.success)
        assertTrue(btResult.message.contains("bluetooth"))
    }

    @Test
    fun testMediaControlTool() = runBlocking {
        val tool = MediaControlTool(context)
        val pauseResult = tool.execute(mapOf("action" to "pause"))
        assertTrue(pauseResult.success)
        assertEquals("Playback paused.", pauseResult.message)

        val nextResult = tool.execute(mapOf("action" to "next"))
        assertTrue(nextResult.success)
        assertEquals("Skipped to next track.", nextResult.message)
    }

    @Test
    fun testWebSearchTool() = runBlocking {
        val tool = WebSearchTool(context)
        val result = tool.execute(mapOf("query" to "Kotlin coroutines"))
        assertTrue(result.success)
        assertTrue(result.message.contains("Kotlin coroutines"))
    }

    @Test
    fun testOpenUrlTool() = runBlocking {
        val tool = OpenUrlTool(context)
        val result = tool.execute(mapOf("url" to "github.com"))
        assertTrue(result.success)
        assertEquals("Navigating to https://github.com.", result.message)
    }

    @Test
    fun testMemoryTool_saveRecallForgetLifecycle() = runBlocking {
        val tool = ManageMemoryTool(memoryRepository)

        // 1. Save
        val saveResult = tool.execute(mapOf(
            "action" to "save",
            "key" to "Android project",
            "value" to "JARVIS",
            "category" to "PROJECT"
        ))
        assertTrue(saveResult.success)
        assertTrue(saveResult.message.contains("JARVIS"))

        // 2. Recall
        val recallResult = tool.execute(mapOf(
            "action" to "recall",
            "key" to "Android project"
        ))
        assertTrue(recallResult.success)
        assertTrue(recallResult.message.contains("JARVIS"))

        // 3. Forget
        val forgetResult = tool.execute(mapOf(
            "action" to "forget",
            "key" to "Android project"
        ))
        assertTrue(forgetResult.success)

        // 4. Verify gone
        val verifyResult = tool.execute(mapOf(
            "action" to "recall",
            "key" to "Android project"
        ))
        assertFalse(verifyResult.success)
    }

    @Test
    fun testLocalRuleProvider_testCommandSuite() = runBlocking {
        fun responseFor(prompt: String) = runBlocking {
            localRuleProvider.generateResponse(
                messages = listOf(MessageEntity(conversationId = 1, role = "USER", content = prompt)),
                systemPrompt = "",
                tools = emptyList(),
                apiKey = "",
                modelName = ""
            )
        }

        // Voice commands
        val hello = responseFor("Hello JARVIS.")
        assertTrue(hello.text.contains("Greetings, sir"))

        val capabilities = responseFor("What can you do?")
        assertTrue(capabilities.text.contains("10 Android system protocols"))

        val time = responseFor("What time is it?")
        assertEquals("get_time_date", time.toolCall?.name)

        val battery = responseFor("What's my battery percentage?")
        assertEquals("get_device_status", battery.toolCall?.name)

        val whatsapp = responseFor("Open WhatsApp.")
        assertEquals("open_app", whatsapp.toolCall?.name)
        assertEquals("whatsapp", whatsapp.toolCall?.arguments?.get("app_name"))

        val youtube = responseFor("Open YouTube.")
        assertEquals("open_app", youtube.toolCall?.name)

        val chrome = responseFor("Open Chrome.")
        assertEquals("open_app", chrome.toolCall?.name)

        val instagram = responseFor("Open Instagram.")
        assertEquals("open_app", instagram.toolCall?.name)

        val spotify = responseFor("Open Spotify.")
        assertEquals("open_app", spotify.toolCall?.name)

        val maps = responseFor("Open Google Maps.")
        assertEquals("open_app", maps.toolCall?.name)

        val camera = responseFor("Open Camera.")
        assertEquals("open_app", camera.toolCall?.name)

        val settings = responseFor("Open Settings.")
        assertEquals("open_settings", settings.toolCall?.name)

        val wifi = responseFor("Open Wi-Fi settings.")
        assertEquals("open_settings", wifi.toolCall?.name)
        assertEquals("wifi", wifi.toolCall?.arguments?.get("setting_type"))

        val bluetooth = responseFor("Open Bluetooth settings.")
        assertEquals("open_settings", bluetooth.toolCall?.name)
        assertEquals("bluetooth", bluetooth.toolCall?.arguments?.get("setting_type"))

        val timer = responseFor("Set a timer for 10 minutes.")
        assertEquals("set_timer", timer.toolCall?.name)
        assertEquals(600, timer.toolCall?.arguments?.get("duration_seconds"))

        val alarm = responseFor("Set an alarm for 6 AM.")
        assertEquals("set_alarm", alarm.toolCall?.name)
        assertEquals(6, alarm.toolCall?.arguments?.get("hour"))

        val pause = responseFor("Pause music.")
        assertEquals("media_control", pause.toolCall?.name)
        assertEquals("pause", pause.toolCall?.arguments?.get("action"))

        val search = responseFor("Search the web for Kotlin coroutines.")
        assertEquals("web_search", search.toolCall?.name)
        assertEquals("Kotlin coroutines", search.toolCall?.arguments?.get("query"))

        val memorySave = responseFor("Remember that my Android project is called JARVIS.")
        assertEquals("manage_memory", memorySave.toolCall?.name)
        assertEquals("save", memorySave.toolCall?.arguments?.get("action"))
        assertEquals("Android project", memorySave.toolCall?.arguments?.get("key"))
        assertEquals("JARVIS", memorySave.toolCall?.arguments?.get("value"))

        val memoryRecall = responseFor("What is my Android project called?")
        assertEquals("manage_memory", memoryRecall.toolCall?.name)
        assertEquals("recall", memoryRecall.toolCall?.arguments?.get("action"))

        val memoryForget = responseFor("Forget my Android project name.")
        assertEquals("manage_memory", memoryForget.toolCall?.name)
        assertEquals("forget", memoryForget.toolCall?.arguments?.get("action"))

        val stopSpeaking = responseFor("Stop speaking.")
        assertTrue(stopSpeaking.text.contains("halted"))
    }

    @Test
    fun testAudioRecorder_wavHeaderGeneration() {
        val dummyPcm = ByteArray(3200) // 0.1s of 16kHz 16-bit audio
        val wav = AudioRecorder.pcmToWav(dummyPcm, sampleRate = 16000, channels = 1, bitsPerSample = 16)
        assertEquals(dummyPcm.size + 44, wav.size)
        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])
    }
}
