package com.sipun.playora.feature.player.service

import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.model.DecoderRecoveryStatus
import com.sipun.playora.feature.player.model.DecoderTrackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecoderRecoveryManagerTest {
    private val video = DecoderTrackType.VIDEO
    private val audio = DecoderTrackType.AUDIO

    @Test
    fun systemAudioFallsBackDirectlyToFfmpeg() {
        val manager = DecoderRecoveryManager()
        manager.onUserSelection(
            audio,
            DecoderMode.HARDWARE,
        )
        assertNull(
            manager.onDecoderFailure(
                audio,
                DecoderMode.HARDWARE,
            ),
        )
        assertEquals(
            DecoderRetry(
                audio,
                DecoderMode.FFMPEG,
            ),
            manager.confirmFallback(),
        )
    }

    @Test
    fun automaticFailureRetriesFfmpegOnce() {
        val manager = DecoderRecoveryManager()
        assertEquals(
            DecoderRetry(
                trackType = video,
                DecoderMode.FFMPEG,
            ),
            manager.onDecoderFailure(
                trackType = video,
                DecoderMode.AUTO,
            ),
        )
        assertNull(
            manager.onDecoderFailure(
                trackType = video,
                DecoderMode.FFMPEG,
            ),
        )
    }

    @Test
    fun explicitSystemRequiresConfirmationThenTriesFfmpegDirectly() {
        val manager = DecoderRecoveryManager()
        manager.onUserSelection(
            trackType = video,
            DecoderMode.HARDWARE,
        )
        assertNull(
            manager.onDecoderFailure(
                trackType = video,
                DecoderMode.HARDWARE,
            ),
        )
        assertEquals(
            DecoderRecoveryStatus.AWAITING_CONFIRMATION,
            manager.state.status,
        )
        assertEquals(
            DecoderRetry(
                trackType = video,
                DecoderMode.FFMPEG,
            ),
            manager.confirmFallback(),
        )
        assertNull(manager.confirmFallback())
        assertNull(
            manager.onDecoderFailure(
                trackType = video,
                DecoderMode.FFMPEG,
            ),
        )
        assertEquals(
            DecoderRecoveryStatus.FAILED,
            manager.state.status,
        )
    }

    @Test
    fun explicitHwPlusRequiresConfirmationThenTriesFfmpegDirectly() {
        val manager = DecoderRecoveryManager()
        manager.onUserSelection(
            trackType = video,
            DecoderMode.HW_PLUS,
        )
        assertNull(
            manager.onDecoderFailure(
                trackType = video,
                DecoderMode.HW_PLUS,
            ),
        )
        assertEquals(
            DecoderRecoveryStatus.AWAITING_CONFIRMATION,
            manager.state.status,
        )
        assertEquals(
            DecoderRetry(
                trackType = video,
                DecoderMode.FFMPEG,
            ),
            manager.confirmFallback(),
        )
    }

    @Test
    fun userSelectionClearsTerminalFailure() {
        val manager = DecoderRecoveryManager()
        manager.onUserSelection(
            trackType = video,
            DecoderMode.HW_PLUS,
        )
        manager.onDecoderFailure(
            trackType = video,
            DecoderMode.HW_PLUS,
        )
        manager.confirmFallback()
        manager.onDecoderFailure(
            trackType = video,
            DecoderMode.FFMPEG,
        )
        manager.onUserSelection(
            trackType = video,
            DecoderMode.HW_PLUS,
        )
        assertEquals(
            DecoderRecoveryStatus.NONE,
            manager.state.status,
        )
    }

    @Test
    fun decoderModeMappingPreservesAppAndSystemSeparation() {
        assertEquals(com.sipun.nextlib.media3ext.ffdecoder.DecoderMode.SYSTEM, DecoderMode.HARDWARE.systemDecoderMode)
        assertEquals(com.sipun.nextlib.media3ext.ffdecoder.DecoderMode.SYSTEM, DecoderMode.HW_PLUS.systemDecoderMode)
        assertEquals(com.sipun.nextlib.media3ext.ffdecoder.DecoderMode.FFMPEG, DecoderMode.FFMPEG.systemDecoderMode)
        assertEquals(com.sipun.nextlib.media3ext.ffdecoder.DecoderMode.AUTO, DecoderMode.AUTO.systemDecoderMode)
    }
}
