package com.sipun.playora.core.common

import com.sipun.playora.core.common.extensions.isSubtitle
import com.sipun.playora.core.common.extensions.prettyName
import com.sipun.playora.core.common.extensions.round
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CommonExtensionsTest {

    @Test
    fun testFloatRound() {
        val value = 3.14159f
        assertEquals(3.14f, value.round(2), 0.0001f)
        assertEquals(3.1f, value.round(1), 0.0001f)
        assertEquals(3.0f, value.round(0), 0.0001f)
    }

    @Test
    fun testIsSubtitle() {
        assertTrue(File("movie.srt").isSubtitle())
        assertTrue(File("movie.vtt").isSubtitle())
        assertTrue(File("movie.ass").isSubtitle())
        assertTrue(File("movie.ssa").isSubtitle())
        assertTrue(File("movie.ttml").isSubtitle())
        assertTrue(File("movie.SRT").isSubtitle())

        assertFalse(File("movie.mp4").isSubtitle())
        assertFalse(File("movie.mkv").isSubtitle())
        assertFalse(File("movie.txt").isSubtitle())
    }

    @Test
    fun testPrettyName() {
        val emulatedStorage = File("/storage/emulated/0")
        assertEquals("Internal Storage", emulatedStorage.prettyName)

        val regularFolder = File("/storage/emulated/0/Movies")
        assertEquals("Movies", regularFolder.prettyName)
    }
}
