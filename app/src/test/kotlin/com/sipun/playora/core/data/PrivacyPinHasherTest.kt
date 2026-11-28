package com.sipun.playora.core.data

import com.sipun.playora.core.data.repository.PinHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPinHasherTest {

    @Test
    fun pbkdf2HashVerifiesMatchingPinAndRejectsWrongPin() {
        val salt = PinHasher.generateSalt()
        val storedHash = PinHasher.hashPbkdf2(pin = "1234", salt = salt)

        assertTrue(PinHasher.isPbkdf2Hash(storedHash))
        assertTrue(PinHasher.verify(pin = "1234", salt = salt, storedHash = storedHash))
        assertFalse(PinHasher.verify(pin = "0000", salt = salt, storedHash = storedHash))
    }

    @Test
    fun pbkdf2UsesDistinctSaltsAndHashesForSamePin() {
        val saltA = PinHasher.generateSalt()
        val saltB = PinHasher.generateSalt()
        val hashA = PinHasher.hashPbkdf2(pin = "4321", salt = saltA)
        val hashB = PinHasher.hashPbkdf2(pin = "4321", salt = saltB)

        assertNotEquals(saltA, saltB)
        assertNotEquals(hashA, hashB)
        assertFalse(PinHasher.verify(pin = "4321", salt = saltB, storedHash = hashA))
    }

    @Test
    fun legacySha256HashVerifiesAndIsDetectedAsLegacy() {
        val salt = PinHasher.generateSalt()
        val legacyHash = PinHasher.hashLegacySha256(pin = "9876", salt = salt)

        assertFalse(PinHasher.isPbkdf2Hash(legacyHash))
        assertEquals(64, legacyHash.length)
        assertTrue(PinHasher.verify(pin = "9876", salt = salt, storedHash = legacyHash))
        assertFalse(PinHasher.verify(pin = "1111", salt = salt, storedHash = legacyHash))
    }
}
