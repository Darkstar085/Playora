package com.sipun.playora.feature.player

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.state.ChaptersState
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.SeekGestureState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.seekAmountFormatted
import com.sipun.playora.feature.player.state.seekToPositionFormated
import com.sipun.playora.feature.player.ui.InfoPill
import com.sipun.playora.feature.player.ui.OverlayView
import com.sipun.playora.feature.player.ui.SeekFeedbackPill
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
    activeVideoDecoderMode: DecoderMode?,
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
) {
    LaunchedEffect(
        progressState.currentPositionMs,
        seekGestureState.seekPreviewPosition,
        seekGestureState.isSeeking,
    ) {
        seekGestureState.onPlaybackPositionChanged(progressState.currentPositionMs)
    }

    val topSafePadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues().calculateTopPadding()

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
                    activeVideoDecoderMode = activeVideoDecoderMode,
                    player = player,
                    onDecoderClick = { onShowOverlay(OverlayView.DECODER_SELECTOR) },
                    onAudioClick = { onShowOverlay(OverlayView.AUDIO_SELECTOR) },
                    onSubtitleClick = { onShowOverlay(OverlayView.SUBTITLE_SELECTOR) },
                    onPlaybackSpeedClick = { onShowOverlay(OverlayView.PLAYBACK_SPEED) },
                    onPlayInBackgroundClick = onPlayInBackgroundClick,
                    onBackClick = onBackClick,
                )
            }
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val topMargin = if (controlsVisibilityState.controlsVisible) {
                    12.dp
                } else {
                    topSafePadding + 16.dp
                }
                when {
                    seekGestureState.seekAmount != null -> SeekFeedbackPill(
                        seekAmountMs = seekGestureState.seekAmount ?: 0L,
                        seekAmountFormatted = seekGestureState.seekAmountFormatted,
                        seekToPositionFormatted = seekGestureState.seekToPositionFormated,
                        modifier = Modifier.padding(top = topMargin),
                    )

                    videoZoomAndContentScaleState.isZooming -> InfoPill(
                        info = "${(videoZoomAndContentScaleState.zoom * 100).toInt()}%",
                        modifier = Modifier.padding(top = topMargin),
                    )
                }
            }
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
                    seekPreviewPosition = seekGestureState.seekPreviewPosition,
                    videoContentScale = videoZoomAndContentScaleState.videoContentScale,
                    isPipSupported = isPipSupported,
                    showRemainingTime = playerPreferences.showRemainingTime,
                    onToggleTimeDisplay = onToggleTimeDisplay,
                    onSeek = seekGestureState::onSeek,
                    onSeekEnd = seekGestureState::onSeekEnd,
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
    }
}
