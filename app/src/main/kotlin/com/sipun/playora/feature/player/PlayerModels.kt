package com.sipun.playora.feature.player.model

import android.net.Uri
import androidx.annotation.StringRes
import com.sipun.playora.R
import com.sipun.nextlib.media3ext.ffdecoder.DecoderMode

val selectableDecoderModes = listOf(
    DecoderMode.HARDWARE,
    DecoderMode.SOFTWARE,
    DecoderMode.SYSTEM,
    DecoderMode.FFMPEG,
)

@get:StringRes
val DecoderMode.labelRes: Int
    get() = when (this) {
        DecoderMode.AUTO -> R.string.auto
        DecoderMode.HARDWARE -> R.string.decoder_mode_hardware
        DecoderMode.SOFTWARE -> R.string.decoder_mode_system_software
        DecoderMode.SYSTEM -> R.string.decoder_mode_system_software
        DecoderMode.FFMPEG -> R.string.decoder_mode_app_software
    }

@get:StringRes
val DecoderMode.descriptionRes: Int
    get() = when (this) {
        DecoderMode.AUTO -> R.string.auto
        DecoderMode.HARDWARE -> R.string.decoder_hardware_description
        DecoderMode.SOFTWARE -> R.string.decoder_system_software_description
        DecoderMode.SYSTEM -> R.string.decoder_system_software_description
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
    val recoveryState: DecoderRecoveryState = DecoderRecoveryState(),
)

data class Subtitle(
    val name: String?,
    val uri: Uri,
    val isSelected: Boolean,
    val filename: String? = null,
)
