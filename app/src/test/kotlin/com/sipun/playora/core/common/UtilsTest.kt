package com.sipun.playora.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UtilsTest {

    @Test
    fun testFormatDurationMillis() {
        assertEquals("00:00", Utils.formatDurationMillis(0))
        assertEquals("00:45", Utils.formatDurationMillis(45_000))
        assertEquals("01:05", Utils.formatDurationMillis(65_000))
        assertEquals("59:59", Utils.formatDurationMillis(3_599_000))
        assertEquals("01:00:00", Utils.formatDurationMillis(3_600_000))
        assertEquals("01:02:03", Utils.formatDurationMillis(3_723_000))
    }

    @Test
    fun testFormatDurationMillisSign() {
        assertEquals("+00:05", Utils.formatDurationMillisSign(5_000))
        assertEquals("-00:05", Utils.formatDurationMillisSign(-5_000))
        assertEquals("+00:00", Utils.formatDurationMillisSign(0))
    }

    @Test
    fun testFormatFileSize() {
        assertEquals("500 B", Utils.formatFileSize(500))
        assertEquals("1.00 KB", Utils.formatFileSize(1024))
        assertEquals("1.50 KB", Utils.formatFileSize(1536))
        assertEquals("1.00 MB", Utils.formatFileSize(1024 * 1024))
        assertEquals("2.50 MB", Utils.formatFileSize((2.5 * 1024 * 1024).toLong()))
        assertEquals("1.00 GB", Utils.formatFileSize(1024L * 1024 * 1024))
    }

    @Test
    fun testFormatBitrate() {
        assertNull(Utils.formatBitrate(0))
        assertNull(Utils.formatBitrate(-100))
        assertEquals("500 bps", Utils.formatBitrate(500))
        assertEquals("1.5 kbps", Utils.formatBitrate(1500))
        assertEquals("5.0 Mbps", Utils.formatBitrate(5_000_000))
        assertEquals("1.2 Gbps", Utils.formatBitrate(1_200_000_000))
    }

    @Test
    fun testFormatLanguage() {
        assertNull(Utils.formatLanguage(null))
        assertEquals("English", Utils.formatLanguage("en"))
    }
}
