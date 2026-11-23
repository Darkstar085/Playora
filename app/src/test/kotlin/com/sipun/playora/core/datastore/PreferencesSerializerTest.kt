package com.sipun.playora.core.datastore

import com.sipun.playora.core.datastore.serializer.ApplicationPreferencesSerializer
import com.sipun.playora.core.datastore.serializer.PlayerPreferencesSerializer
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.PlayerPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PreferencesSerializerTest {

    @Test
    fun testApplicationPreferencesRoundTrip() = runBlocking {
        val original = ApplicationPreferences()
        val outputStream = ByteArrayOutputStream()

        ApplicationPreferencesSerializer.writeTo(original, outputStream)
        val bytes = outputStream.toByteArray()

        val inputStream = ByteArrayInputStream(bytes)
        val deserialized = ApplicationPreferencesSerializer.readFrom(inputStream)

        assertNotNull(deserialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun testApplicationPreferencesWithCustomValuesRoundTrip() = runBlocking {
        val original = ApplicationPreferences(
            hasCompletedOnboarding = true,
            hasSeenVaultHint = true,
            useHighContrastDarkTheme = true,
            showPathField = true,
            newVideoThresholdDays = 14,
        )
        val outputStream = ByteArrayOutputStream()

        ApplicationPreferencesSerializer.writeTo(original, outputStream)
        val bytes = outputStream.toByteArray()

        val inputStream = ByteArrayInputStream(bytes)
        val deserialized = ApplicationPreferencesSerializer.readFrom(inputStream)

        assertNotNull(deserialized)
        assertEquals(original, deserialized)
        assertEquals(true, deserialized.hasCompletedOnboarding)
        assertEquals(true, deserialized.hasSeenVaultHint)
    }

    @Test
    fun testPlayerPreferencesWithCustomValuesRoundTrip() = runBlocking {
        val original = PlayerPreferences(
            resume = com.sipun.playora.core.model.Resume.NO,
            seekIncrement = 25,
            preferredAudioLanguage = "jpn",
            preferredSubtitleLanguage = "eng",
            subtitleFont = com.sipun.playora.core.model.Font.MONOSPACE,
            subtitleTextSize = 28,
        )
        val outputStream = ByteArrayOutputStream()

        PlayerPreferencesSerializer.writeTo(original, outputStream)
        val bytes = outputStream.toByteArray()

        val inputStream = ByteArrayInputStream(bytes)
        val deserialized = PlayerPreferencesSerializer.readFrom(inputStream)

        assertNotNull(deserialized)
        assertEquals(original, deserialized)
        assertEquals(com.sipun.playora.core.model.Resume.NO, deserialized.resume)
        assertEquals(25, deserialized.seekIncrement)
        assertEquals(com.sipun.playora.core.model.Font.MONOSPACE, deserialized.subtitleFont)
    }
}
