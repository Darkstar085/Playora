package com.sipun.playora.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PreferenceEnumsTest {

    @Test
    fun testLoopMode() {
        assertEquals(3, LoopMode.entries.size)
        assertNotNull(LoopMode.valueOf("OFF"))
        assertNotNull(LoopMode.valueOf("ONE"))
        assertNotNull(LoopMode.valueOf("ALL"))
    }

    @Test
    fun testThemeConfig() {
        assertEquals(3, ThemeConfig.entries.size)
        assertEquals(ThemeConfig.SYSTEM, ThemeConfig.valueOf("SYSTEM"))
        assertEquals(ThemeConfig.OFF, ThemeConfig.valueOf("OFF"))
        assertEquals(ThemeConfig.ON, ThemeConfig.valueOf("ON"))
    }

    @Test
    fun testMediaLayoutAndMode() {
        assertEquals(2, MediaLayoutMode.entries.size)
        assertEquals(3, MediaViewMode.entries.size)
    }

    @Test
    fun testScreenOrientation() {
        assertEquals(1, ScreenOrientation.entries.size)
        assertNotNull(ScreenOrientation.valueOf("VIDEO_ORIENTATION"))
    }
}
