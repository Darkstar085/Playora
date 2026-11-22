package com.sipun.playora.feature.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import com.sipun.playora.core.model.ControlButtonsPosition
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.state.ChaptersState
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.SeekGestureState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.seekAmountFormatted
import com.sipun.playora.feature.player.state.seekToPositionFormated
import com.sipun.playora.feature.player.ui.InfoView
import com.sipun.playora.feature.player.ui.OverlayView
import com.sipun.playora.feature.player.ui.controls.ControlsBottomView
import com.sipun.playora.feature.player.ui.controls.ControlsMiniIndicatorsView
import com.sipun.playora.feature.player.ui.controls.ControlsTopView

@OptIn(UnstableApi::class)
@Composable
fun PlayerControls(
    player: Player?,
    title: String,
    playerPreferences: PlayerPreferences,
    videoDecoderMode: DecoderMode?,
    controlsVisibilityState: ControlsVisibilityState,
    seekGestureState: SeekGestureState,
    videoZoomAndContentScaleState: VideoZoomAndContentScaleState,
    progressState: ProgressStateWithTickInterval,
    chaptersState: ChaptersState,
    isPipSupported: Boolean,
    onShowOverlay: (OverlayView) -> Unit,
    onBackClick: () -> Unit,
    onPlayInBackgroundClick: () -> Unit,
    onToggleTimeDisplay: () -> Unit,
    onPictureInPictureClick: () -> Unit,
    modifier: Modifier = Modifier,
    middleControlsModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            AnimatedVisibility(
                visible = controlsVisibilityState.controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ControlsTopView(
                    title = title,
                    videoDecoderMode = videoDecoderMode,
                    player = player,
                    onDecoderClick = { onShowOverlay(OverlayView.DECODER_SELECTOR) },
                    onAudioClick = { onShowOverlay(OverlayView.AUDIO_SELECTOR) },
                    onSubtitleClick = { onShowOverlay(OverlayView.SUBTITLE_SELECTOR) },
                    onPlaylistClick = { onShowOverlay(OverlayView.PLAYLIST) },
                    onChaptersClick = { onShowOverlay(OverlayView.CHAPTERS) },
                    onPlaybackSpeedClick = { onShowOverlay(OverlayView.PLAYBACK_SPEED) },
                    onPlayInBackgroundClick = onPlayInBackgroundClick,
                    onBackClick = onBackClick,
                )
            }
            Spacer(modifier = Modifier.weight(weight = 1f))
            AnimatedVisibility(
                visible =
                    controlsVisibilityState.controlsVisible &&
                            !controlsVisibilityState.controlsLocked,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ControlsBottomView(
                    player = player,
                    progressState = progressState,
                    chaptersState = chaptersState,
                    onChaptersClick = { onShowOverlay(OverlayView.CHAPTERS) },
                    controlsAlignment = when (playerPreferences.controlButtonsPosition) {
                        ControlButtonsPosition.LEFT -> Alignment.Start
                        ControlButtonsPosition.RIGHT -> Alignment.End
                    },
                    videoContentScale = videoZoomAndContentScaleState.videoContentScale,
                    isPipSupported = isPipSupported,
                    showRemainingTime = playerPreferences.showRemainingTime,
                    onToggleTimeDisplay = onToggleTimeDisplay,
                    onSeek = seekGestureState::onSeek,
                    onSeekEnd = seekGestureState::onSeekEnd,
                    onPlaybackSpeedClick = { onShowOverlay(OverlayView.PLAYBACK_SPEED) },
                    onPlayInBackgroundClick = onPlayInBackgroundClick,
                    onLockControlsClick = {
                        controlsVisibilityState.showControls()
                        controlsVisibilityState.lockControls()
                    },
                    onVideoContentScaleClick = {
                        onShowOverlay(OverlayView.VIDEO_CONTENT_SCALE)
                    },
                    onVideoContentScaleLongClick = {
                        controlsVisibilityState.showControls()
                        videoZoomAndContentScaleState.switchToNextVideoContentScale()
                    },
                    onPictureInPictureClick = onPictureInPictureClick,
                )
            }
        }
        AnimatedVisibility(
            visible = !controlsVisibilityState.controlsVisible || controlsVisibilityState.controlsLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ControlsMiniIndicatorsView(
                progressState = progressState,
            )
        }
        when {
            seekGestureState.seekAmount != null -> InfoView(
                info =
                    "${seekGestureState.seekAmountFormatted}\n" +
                            "[${seekGestureState.seekToPositionFormated}]",
            )

            videoZoomAndContentScaleState.isZooming -> InfoView(
                info = "${(videoZoomAndContentScaleState.zoom * 100).toInt()}%",
            )
        }
    }
}
