package com.sipun.playora.feature.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import com.sipun.playora.core.ui.components.thenIf
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.SeekGestureState
import com.sipun.playora.feature.player.state.TapGestureState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.rememberChaptersState
import com.sipun.playora.feature.player.state.rememberMediaPresentationState
import com.sipun.playora.feature.player.state.rememberMetadataState
import com.sipun.playora.feature.player.state.rememberPlayerKeyInputState
import com.sipun.playora.feature.player.ui.DpadSeekIndicator
import com.sipun.playora.feature.player.ui.OverlayShowView
import com.sipun.playora.feature.player.ui.OverlayView
import com.sipun.playora.feature.player.ui.PlayerGestureIndicators
import com.sipun.playora.feature.player.ui.InfoView
import com.sipun.playora.feature.player.extensions.nameRes
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
internal fun MediaPlayerControls(
    player: Player,
    state: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    controlsVisibilityState: ControlsVisibilityState,
    tapGestureState: TapGestureState,
    seekGestureState: SeekGestureState,
    videoZoomAndContentScaleState: VideoZoomAndContentScaleState,
    isPipSupported: Boolean,
    onPictureInPictureClick: () -> Unit,
    modifier: Modifier = Modifier,
    dpadUpDown: Int? = null,
) {
    val playerPreferences = state.playerPreferences
    val metadataState = rememberMetadataState(player = player)
    val mediaPresentationState = rememberMediaPresentationState(player = player)
    val progressState = rememberProgressStateWithTickInterval(player = player)
    val chaptersState = rememberChaptersState(
        player = player,
        progressState = progressState,
    )
    val keyInputState = rememberPlayerKeyInputState(
        player = player,
        controls = controlsVisibilityState,
        seekIncrementMs = playerPreferences.seekIncrement.seconds.inWholeMilliseconds,
        dpadUpDown = dpadUpDown,
    )
    var overlayView by remember { mutableStateOf<OverlayView?>(value = null) }
    LaunchedEffect(metadataState.mediaId) {
        if (overlayView == OverlayView.CHAPTERS) overlayView = null
    }

    fun showOverlay(overlay: OverlayView) {
        controlsVisibilityState.hideControls()
        overlayView = overlay
    }

    val handleDpadKeys = dpadUpDown != null
    val rootFocusRequester = remember { FocusRequester() }
    if (handleDpadKeys) {
        LaunchedEffect(
            controlsVisibilityState.controlsVisible,
            key2 = overlayView,
        ) {
            if (overlayView == null) {
                runCatching { rootFocusRequester.requestFocus() }
            }
        }
    }

    CompositionLocalProvider(
        LocalControlsVisibilityState provides controlsVisibilityState,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .thenIf(condition = handleDpadKeys) {
                    focusRequester(focusRequester = rootFocusRequester)
                        .focusable()
                        .onPreviewKeyEvent { keyEvent ->
                            overlayView == null &&
                                (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.DirectionDown) &&
                                keyInputState.onKeyEvent(keyEvent = keyEvent)
                        }
                },
        ) {
            if (mediaPresentationState.isBuffering) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .size(72.dp),
                )
            }
            PlayerGestureIndicators(tapGestureState = tapGestureState)
            DpadSeekIndicator(
                visible = keyInputState.isSeeking && keyInputState.seekOffsetMs != 0L,
                offsetMs = keyInputState.seekOffsetMs,
                positionMs = keyInputState.seekPositionMs,
            )

            if (controlsVisibilityState.controlsVisible && controlsVisibilityState.controlsLocked) {
                PlayerLockedControls(
                    onUnlockControls = controlsVisibilityState::unlockControls,
                )
            } else {
                PlayerControls(
                    player = player,
                    title = metadataState.title.orEmpty(),
                    playerPreferences = playerPreferences,
                    videoDecoderMode = state.decoderServiceState.videoMode,
                    controlsVisibilityState = controlsVisibilityState,
                    seekGestureState = seekGestureState,
                    videoZoomAndContentScaleState = videoZoomAndContentScaleState,
                    progressState = progressState,
                    chaptersState = chaptersState,
                    isPipSupported = isPipSupported,
                    onShowOverlay = ::showOverlay,
                    onBackClick = { onAction(PlayerAction.NavigateUp) },
                    onPlayInBackgroundClick = { onAction(PlayerAction.PlayInBackground) },
                    onToggleTimeDisplay = { onAction(PlayerAction.ToggleTimeDisplay) },
                    onPictureInPictureClick = onPictureInPictureClick,
                )
            }

            key(videoZoomAndContentScaleState.contentScaleIndicatorKey) {
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.Center),
                    visible = videoZoomAndContentScaleState.showContentScaleIndicator,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    InfoView(
                        info = stringResource(videoZoomAndContentScaleState.videoContentScale.nameRes()),
                    )
                }
            }

            OverlayShowView(
                player = player,
                overlayView = overlayView,
                chapters = chaptersState.chapters,
                currentChapterIndex = chaptersState.currentChapterIndex,
                onChapterSelected = { chapter ->
                    if (player.isCurrentMediaItemSeekable) {
                        player.seekTo(chapter.startTimeMs)
                        overlayView = null
                        controlsVisibilityState.showControls()
                    }
                },
                videoDecoderMode = state.decoderServiceState.videoMode,
                audioDecoderMode = state.decoderServiceState.audioMode,
                videoContentScale = videoZoomAndContentScaleState.videoContentScale,
                onDismiss = { overlayView = null },
                onVideoDecoderModeSelected = {
                    onAction(PlayerAction.SetVideoDecoderMode(mode = it))
                },
                onAudioDecoderModeSelected = {
                    onAction(PlayerAction.SetAudioDecoderMode(mode = it))
                },
                onSelectSubtitleClick = { onAction(PlayerAction.SelectSubtitle) },
                onSelectAudioClick = { onAction(PlayerAction.SelectAudio) },
                onSubtitleOptionEvent = {
                    onAction(PlayerAction.OnSubtitleOptionEvent(event = it))
                },
                onVideoContentScaleChanged =
                    videoZoomAndContentScaleState::onVideoContentScaleChanged,
            )
        }
    }

    BackHandler {
        when {
            overlayView != null -> overlayView = null
            else -> onAction(PlayerAction.NavigateUp)
        }
    }
}
