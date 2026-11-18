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
    fun testPlayerPreferencesRoundTrip() = runBlocking {
        val original = PlayerPreferences()
        val outputStream = ByteArrayOutputStream()

        PlayerPreferencesSerializer.writeTo(original, outputStream)
        val bytes = outputStream.toByteArray()

        val inputStream = ByteArrayInputStream(bytes)
        val deserialized = PlayerPreferencesSerializer.readFrom(inputStream)

        assertNotNull(deserialized)
        assertEquals(original, deserialized)
    }
}
