package com.sipun.playora.core.data.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

sealed interface PinVerificationResult {
    data object Success : PinVerificationResult
    data class Invalid(val failedAttempts: Int) : PinVerificationResult
    data class LockedOut(val remainingSeconds: Int) : PinVerificationResult
}

internal object PinHasher {
    private const val PBKDF2_PREFIX = "pbkdf2$"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val PBKDF2_KEY_LENGTH_BITS = 256

    fun generateSalt(): String {
        val bytes = ByteArray(size = 16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    fun hashPbkdf2(pin: String, salt: String): String {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt.toByteArray(Charsets.UTF_8),
            PBKDF2_ITERATIONS,
            PBKDF2_KEY_LENGTH_BITS,
        )
        return try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val hash = factory.generateSecret(spec).encoded
            PBKDF2_PREFIX + hash.joinToString(separator = "") { "%02x".format(it) }
        } finally {
            spec.clearPassword()
        }
    }

    fun hashLegacySha256(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString(separator = "") { "%02x".format(it) }
    }

    fun isPbkdf2Hash(storedHash: String): Boolean = storedHash.startsWith(PBKDF2_PREFIX)

    fun verify(pin: String, salt: String, storedHash: String): Boolean {
        val computed = if (isPbkdf2Hash(storedHash)) {
            hashPbkdf2(pin, salt)
        } else {
            hashLegacySha256(pin, salt)
        }
        return MessageDigest.isEqual(
            computed.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8),
        )
    }
}

interface PrivacyPinRepository {

    suspend fun hasPinSet(): Boolean

    suspend fun setPin(pin: String, preserveBiometric: Boolean = false)

    suspend fun verifyPin(pin: String): Boolean

    suspend fun verifyPinWithLockout(pin: String): PinVerificationResult

    suspend fun getLockoutRemainingSeconds(): Int

    suspend fun isBiometricEnabled(): Boolean

    suspend fun setBiometricEnabled(enabled: Boolean)
}

@Single
class LocalPrivacyPinRepository(private val context: Context) : PrivacyPinRepository {

    internal var nowMs: () -> Long = System::currentTimeMillis

    private val preferences by lazy {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE,
        )
    }

    override suspend fun hasPinSet(): Boolean = withContext(Dispatchers.IO) {
        preferences.contains(KEY_PIN_HASH)
    }

    override suspend fun setPin(pin: String, preserveBiometric: Boolean) = withContext(Dispatchers.IO) {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPbkdf2(pin = pin, salt = salt)
        preferences.edit {
            putString(KEY_PIN_HASH, hash)
            putString(KEY_SALT, salt)
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putInt(KEY_CONSECUTIVE_LOCKOUTS, 0)
            putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
            if (!preserveBiometric) {
                putBoolean(KEY_BIOMETRIC_ENABLED, false)
            }
        }
    }

    override suspend fun verifyPin(pin: String): Boolean =
        verifyPinWithLockout(pin) is PinVerificationResult.Success

    override suspend fun getLockoutRemainingSeconds(): Int = withContext(Dispatchers.IO) {
        computeRemainingLockoutSeconds()
    }

    override suspend fun verifyPinWithLockout(pin: String): PinVerificationResult =
        withContext(Dispatchers.IO) {
            val remainingSeconds = computeRemainingLockoutSeconds()
            if (remainingSeconds > 0) {
                return@withContext PinVerificationResult.LockedOut(remainingSeconds)
            }

            val storedHash = preferences.getString(KEY_PIN_HASH, null)
                ?: return@withContext PinVerificationResult.Invalid(0)
            val salt = preferences.getString(KEY_SALT, null)
                ?: return@withContext PinVerificationResult.Invalid(0)

            if (PinHasher.verify(pin = pin, salt = salt, storedHash = storedHash)) {
                preferences.edit {
                    if (!PinHasher.isPbkdf2Hash(storedHash)) {
                        val upgradedSalt = PinHasher.generateSalt()
                        putString(KEY_PIN_HASH, PinHasher.hashPbkdf2(pin = pin, salt = upgradedSalt))
                        putString(KEY_SALT, upgradedSalt)
                    }
                    putInt(KEY_FAILED_ATTEMPTS, 0)
                    putInt(KEY_CONSECUTIVE_LOCKOUTS, 0)
                    putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
                }
                PinVerificationResult.Success
            } else {
                val attempts = preferences.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    val lockouts = preferences.getInt(KEY_CONSECUTIVE_LOCKOUTS, 0) + 1
                    val lockoutDuration = lockoutDurationMs(lockouts)
                    val lockoutUntil = nowMs() + lockoutDuration
                    preferences.edit {
                        putInt(KEY_FAILED_ATTEMPTS, 0)
                        putInt(KEY_CONSECUTIVE_LOCKOUTS, lockouts)
                        putLong(KEY_LOCKOUT_UNTIL_MS, lockoutUntil)
                    }
                    PinVerificationResult.LockedOut(
                        remainingSeconds = (lockoutDuration / 1000L).toInt(),
                    )
                } else {
                    preferences.edit {
                        putInt(KEY_FAILED_ATTEMPTS, attempts)
                    }
                    PinVerificationResult.Invalid(failedAttempts = attempts)
                }
            }
        }

    override suspend fun isBiometricEnabled(): Boolean = withContext(Dispatchers.IO) {
        preferences.getBoolean(
            KEY_BIOMETRIC_ENABLED,
            false,
        )
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        preferences.edit {
            putBoolean(
                KEY_BIOMETRIC_ENABLED,
                enabled,
            )
        }
    }

    private fun computeRemainingLockoutSeconds(): Int {
        val now = nowMs()
        val rawUntil = preferences.getLong(KEY_LOCKOUT_UNTIL_MS, 0L)
        if (rawUntil <= 0L) return 0
        val maxLockoutDuration = lockoutDurationMs(
            preferences.getInt(KEY_CONSECUTIVE_LOCKOUTS, 0),
        )
        val effectiveUntil = if (rawUntil > now + maxLockoutDuration) {
            val clamped = now + maxLockoutDuration
            preferences.edit { putLong(KEY_LOCKOUT_UNTIL_MS, clamped) }
            clamped
        } else {
            rawUntil
        }
        val remainingMs = effectiveUntil - now
        if (remainingMs <= 0L) {
            preferences.edit { putLong(KEY_LOCKOUT_UNTIL_MS, 0L) }
            return 0
        }
        return ((remainingMs + 999L) / 1000L).toInt().coerceAtLeast(1)
    }

    companion object {
        private const val PREFS_NAME = "privacy_security_prefs"
        private const val KEY_PIN_HASH = "privacy_pin_hash"
        private const val KEY_SALT = "privacy_pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "privacy_biometric_enabled"
        private const val KEY_FAILED_ATTEMPTS = "privacy_pin_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL_MS = "privacy_pin_lockout_until_ms"
        private const val KEY_CONSECUTIVE_LOCKOUTS = "privacy_pin_consecutive_lockouts"
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L

        internal fun lockoutDurationMs(consecutiveLockouts: Int): Long = when {
            consecutiveLockouts <= 1 -> LOCKOUT_DURATION_MS
            consecutiveLockouts == 2 -> 60_000L
            consecutiveLockouts == 3 -> 120_000L
            else -> 300_000L
        }
    }
}
