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
    fun testResumeEnum() {
        assertEquals(2, Resume.entries.size)
        assertEquals(Resume.YES, Resume.valueOf("YES"))
        assertEquals(Resume.NO, Resume.valueOf("NO"))
    }

    @Test
    fun testFontEnum() {
        assertEquals(4, Font.entries.size)
        assertEquals(Font.DEFAULT, Font.valueOf("DEFAULT"))
        assertEquals(Font.MONOSPACE, Font.valueOf("MONOSPACE"))
        assertEquals(Font.SANS_SERIF, Font.valueOf("SANS_SERIF"))
        assertEquals(Font.SERIF, Font.valueOf("SERIF"))
    }

    @Test
    fun testDoubleTapGesture() {
        assertEquals(4, DoubleTapGesture.entries.size)
        assertNotNull(DoubleTapGesture.valueOf("NONE"))
        assertNotNull(DoubleTapGesture.valueOf("FAST_FORWARD_AND_REWIND"))
        assertNotNull(DoubleTapGesture.valueOf("PLAY_PAUSE"))
        assertNotNull(DoubleTapGesture.valueOf("BOTH"))
    }

    @Test
    fun testThumbnailGenerationStrategy() {
        assertEquals(3, ThumbnailGenerationStrategy.entries.size)
        assertNotNull(ThumbnailGenerationStrategy.valueOf("FIRST_FRAME"))
        assertNotNull(ThumbnailGenerationStrategy.valueOf("FRAME_AT_PERCENTAGE"))
        assertNotNull(ThumbnailGenerationStrategy.valueOf("HYBRID"))
    }

    @Test
    fun testVideoContentScale() {
        assertEquals(4, VideoContentScale.entries.size)
        assertNotNull(VideoContentScale.valueOf("BEST_FIT"))
        assertNotNull(VideoContentScale.valueOf("STRETCH"))
        assertNotNull(VideoContentScale.valueOf("CROP"))
        assertNotNull(VideoContentScale.valueOf("HUNDRED_PERCENT"))
    }

    @Test
    fun testSortEnums() {
        assertEquals(5, Sort.By.entries.size)
        assertEquals(2, Sort.Order.entries.size)
    }
}
