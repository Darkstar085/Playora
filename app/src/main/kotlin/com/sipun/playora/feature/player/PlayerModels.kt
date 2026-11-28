package com.sipun.playora.feature.player.model

import android.net.Uri
import androidx.annotation.StringRes
import com.sipun.playora.R
import com.sipun.nextlib.media3ext.ffdecoder.DecoderMode as NextDecoderMode

enum class DecoderMode {
    AUTO,
    HARDWARE,
    HW_PLUS,
    FFMPEG,
    ;

    val systemDecoderMode: NextDecoderMode
        get() = when (this) {
            AUTO -> NextDecoderMode.AUTO
            HARDWARE, HW_PLUS -> NextDecoderMode.SYSTEM
            FFMPEG -> NextDecoderMode.FFMPEG
        }
}

internal fun NextDecoderMode.playerMode(): DecoderMode = when (this) {
    NextDecoderMode.AUTO -> DecoderMode.AUTO
    NextDecoderMode.FFMPEG -> DecoderMode.FFMPEG
    else -> DecoderMode.HARDWARE
}

@get:StringRes
val DecoderMode.labelRes: Int
    get() = when (this) {
        DecoderMode.AUTO -> R.string.auto
        DecoderMode.HARDWARE -> R.string.decoder_mode_hardware
        DecoderMode.HW_PLUS -> R.string.decoder_mode_hardware_plus
        DecoderMode.FFMPEG -> R.string.decoder_mode_app_software
    }

@get:StringRes
val DecoderMode.descriptionRes: Int
    get() = when (this) {
        DecoderMode.AUTO -> R.string.decoder_auto_description
        DecoderMode.HARDWARE -> R.string.decoder_system_description
        DecoderMode.HW_PLUS -> R.string.decoder_hardware_plus_description
        DecoderMode.FFMPEG -> R.string.decoder_app_software_description
    }

enum class DecoderRecoveryStatus {
    NONE,
    RECOVERING,
    AWAITING_CONFIRMATION,
    FAILED,
}

enum class DecoderTrackType {
    VIDEO,
    AUDIO,
}

data class DecoderRecoveryState(
    val status: DecoderRecoveryStatus = DecoderRecoveryStatus.NONE,
    val trackType: DecoderTrackType? = null,
    val unsupportedMode: DecoderMode? = null,
)

data class DecoderServiceState(
    val videoMode: DecoderMode? = null,
    val audioMode: DecoderMode? = null,
    val activeVideoMode: DecoderMode? = null,
    val activeAudioMode: DecoderMode? = null,
    val recoveryState: DecoderRecoveryState = DecoderRecoveryState(),
)

data class Subtitle(
    val name: String?,
    val uri: Uri,
    val isSelected: Boolean,
    val filename: String? = null,
)
