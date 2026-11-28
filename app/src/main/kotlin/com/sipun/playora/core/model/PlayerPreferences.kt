package com.sipun.playora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerPreferences(
    val resume: Resume = Resume.YES,
    val playerBrightness: Float = 0.5f,
    val playerVideoZoom: VideoContentScale = VideoContentScale.BEST_FIT,
    val autoplay: Boolean = true,
    val autoPip: Boolean = true,
    val autoBackgroundPlay: Boolean = false,
    val loopMode: LoopMode = LoopMode.OFF,

    val enableVolumeSwipeGesture: Boolean = true,
    val enableBrightnessSwipeGesture: Boolean = true,
    val useSeekControls: Boolean = true,
    val useZoomControls: Boolean = true,
    val doubleTapGesture: DoubleTapGesture = DoubleTapGesture.BOTH,
    val useLongPressControls: Boolean = false,
    val longPressControlsSpeed: Float = 2.0f,
    val seekIncrement: Int = DEFAULT_SEEK_INCREMENT,

    val showRemainingTime: Boolean = false,

    val preferredAudioLanguage: String = "",
    val pauseOnHeadsetDisconnect: Boolean = true,
    val requireAudioFocus: Boolean = true,
    val showSystemVolumePanel: Boolean = false,
    val enableVolumeBoost: Boolean = false,

    val useSystemCaptionStyle: Boolean = false,
    val preferredSubtitleLanguage: String = "",
    val subtitleTextSize: Int = DEFAULT_SUBTITLE_TEXT_SIZE,
    val subtitleBackground: Boolean = false,
    val subtitleFont: Font = Font.DEFAULT,
    val subtitleTextBold: Boolean = true,

    val subtitleOffsetY: Float = 0f,
) {

    companion object {
        const val DEFAULT_SEEK_INCREMENT = 10
        const val DEFAULT_SUBTITLE_TEXT_SIZE = 20
    }
}
