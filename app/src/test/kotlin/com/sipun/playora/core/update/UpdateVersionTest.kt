package com.sipun.playora.core.update

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
}
