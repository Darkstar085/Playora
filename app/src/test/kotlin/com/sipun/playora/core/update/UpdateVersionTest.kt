package com.sipun.playora.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionTest {
    @Test
    fun comparesNumericVersionParts() {
        assertTrue(UpdateManager.isNewerVersion("0.9.9", "0.10.0"))
        assertTrue(UpdateManager.isNewerVersion("1.0", "1.0.1"))
        assertFalse(UpdateManager.isNewerVersion("1.2.0", "1.2"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "1.99.99"))
    }

    @Test
    fun handlesVPrefixAndEqualVersions() {
        // v prefix handling
        assertTrue(UpdateManager.isNewerVersion("v1.0.0", "v1.0.1"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v1.1.0"))
        assertTrue(UpdateManager.isNewerVersion("v0.5.0", "0.6.0"))

        // Equal versions should be false
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "v1.0.0"))
    }

    @Test
    fun handlesMultiSegmentAndSuffixes() {
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.0.0.1"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0.1", "1.0.0"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0-beta1", "1.0.1"))
    }

    @Test
    fun testAppUpdateModel() {
        val update = AppUpdate(
            version = "1.0.1",
            name = "Playora 1.0.1",
            notes = "• Bug fixes\n• Performance",
            downloadUrl = "https://github.com/Darkstar085/playora/releases/download/v1.0.1/app-release.apk",
            fileName = "playora-1.0.1.apk",
            size = 35_000_000L,
            digest = "sha256:abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234",
            releaseDate = "Nov 23, 2026",
            isPrerelease = false,
        )

        assertEquals("1.0.1", update.version)
        assertEquals("Playora 1.0.1", update.name)
        assertFalse(update.isPrerelease)
        assertEquals(35_000_000L, update.size)
        assertTrue(update.downloadUrl.startsWith("https://github.com"))
    }
}
