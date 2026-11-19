package com.sipun.playora.feature.player.service

import com.sipun.playora.feature.player.model.DecoderRecoveryState
import com.sipun.playora.feature.player.model.DecoderRecoveryStatus
import com.sipun.playora.feature.player.model.DecoderTrackType
import com.sipun.nextlib.media3ext.ffdecoder.DecoderMode

internal class DecoderRecoveryManager {

    private val videoRecovery = TrackRecovery()
    private val audioRecovery = TrackRecovery()

    val state: DecoderRecoveryState
        get() = listOf(
            videoRecovery.state,
            audioRecovery.state,
        ).maxBy { it.status }

    fun onUserSelection(trackType: DecoderTrackType, mode: DecoderMode) {
        recoveryFor(trackType = trackType).apply {
            reset()
            requiresConfirmation = mode != DecoderMode.AUTO
        }
    }

    fun onDecoderFailure(trackType: DecoderTrackType, mode: DecoderMode): DecoderRetry? {
        val recovery = recoveryFor(trackType = trackType)
        if (recovery.failedMode == mode) {
            return null
        }

        recovery.failedMode = mode
        if (recovery.fallbackModes == null) {
            recovery.fallbackModes = ArrayDeque(mode.fallbackModes())
        }
        if (recovery.fallbackModes.isNullOrEmpty()) {
            recovery.state = DecoderRecoveryState(
                status = DecoderRecoveryStatus.FAILED,
                trackType = trackType,
            )
            return null
        }

        if (recovery.requiresConfirmation) {
            recovery.state = DecoderRecoveryState(
                status = DecoderRecoveryStatus.AWAITING_CONFIRMATION,
                trackType = trackType,
                unsupportedMode = mode,
            )
            return null
        }

        val fallbackMode = checkNotNull(recovery.fallbackModes).removeFirst()
        recovery.state = DecoderRecoveryState(
            status = DecoderRecoveryStatus.RECOVERING,
            trackType = trackType,
        )
        return DecoderRetry(
            trackType = trackType,
            mode = fallbackMode,
        )
    }

    fun confirmFallback(): DecoderRetry? {
        if (state.status != DecoderRecoveryStatus.AWAITING_CONFIRMATION) return null
        val trackType = state.trackType ?: return null
        val recovery = recoveryFor(trackType = trackType)
        if (recovery.fallbackModes.isNullOrEmpty()) return null
        val fallbackMode = checkNotNull(recovery.fallbackModes).removeFirst()

        recovery.requiresConfirmation = false
        recovery.state = DecoderRecoveryState(
            status = DecoderRecoveryStatus.RECOVERING,
            trackType = trackType,
        )
        return DecoderRetry(
            trackType = trackType,
            mode = fallbackMode,
        )
    }

    fun onDecoderInitialized(trackType: DecoderTrackType) {
        val recovery = recoveryFor(trackType = trackType)
        if (recovery.state.status == DecoderRecoveryStatus.RECOVERING) {
            recovery.state = DecoderRecoveryState()
        }
    }

    fun onNonDecoderError() {
        videoRecovery.state = DecoderRecoveryState()
        audioRecovery.state = DecoderRecoveryState()
    }

    private fun recoveryFor(trackType: DecoderTrackType): TrackRecovery = when (trackType) {
        DecoderTrackType.VIDEO -> videoRecovery
        DecoderTrackType.AUDIO -> audioRecovery
    }
}

internal data class DecoderRetry(val trackType: DecoderTrackType, val mode: DecoderMode)

private class TrackRecovery {
    var state = DecoderRecoveryState()
    var requiresConfirmation = false
    var fallbackModes: ArrayDeque<DecoderMode>? = null
    var failedMode: DecoderMode? = null

    fun reset() {
        state = DecoderRecoveryState()
        requiresConfirmation = false
        fallbackModes = null
        failedMode = null
    }
}

private fun DecoderMode.fallbackModes(): List<DecoderMode> = when (this) {
    DecoderMode.AUTO -> listOf(DecoderMode.FFMPEG)

    DecoderMode.HARDWARE -> listOf(
        DecoderMode.SOFTWARE,
        DecoderMode.SYSTEM,
        DecoderMode.FFMPEG,
    )

    DecoderMode.SOFTWARE, DecoderMode.SYSTEM -> listOf(DecoderMode.FFMPEG)

    DecoderMode.FFMPEG -> listOf(DecoderMode.AUTO)
}
