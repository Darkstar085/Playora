package com.sipun.playora.feature.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import com.sipun.playora.feature.player.extensions.nameRes
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.SeekGestureState
import com.sipun.playora.feature.player.state.TapGestureState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.rememberChaptersState
import com.sipun.playora.feature.player.state.rememberMetadataState
import com.sipun.playora.feature.player.state.rememberPlayerKeyInputState
import com.sipun.playora.feature.player.ui.DpadSeekIndicator
import com.sipun.playora.feature.player.ui.InfoPill
import com.sipun.playora.feature.player.ui.OverlayShowView
import com.sipun.playora.feature.player.ui.OverlayView
import com.sipun.playora.feature.player.ui.PlayerGestureIndicators
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

    fun showOverlay(overlay: OverlayView) {
        controlsVisibilityState.hideControls()
        overlayView = overlay
    }

    val rootFocusRequester = remember { FocusRequester() }
    LaunchedEffect(
        controlsVisibilityState.controlsVisible,
        overlayView,
    ) {
        if (overlayView == null) {
            runCatching { rootFocusRequester.requestFocus() }
        }
    }

    val topSafePadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues().calculateTopPadding()

    CompositionLocalProvider(
        LocalControlsVisibilityState provides controlsVisibilityState,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .focusRequester(focusRequester = rootFocusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    overlayView == null && keyInputState.onKeyEvent(keyEvent = keyEvent)
                },
        ) {
            PlayerGestureIndicators(tapGestureState = tapGestureState)
            DpadSeekIndicator(
                visible = keyInputState.isSeeking && keyInputState.seekOffsetMs != 0L,
                offsetMs = keyInputState.seekOffsetMs,
                positionMs = keyInputState.seekPositionMs,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topSafePadding + 16.dp),
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
                    activeVideoDecoderMode = state.decoderServiceState.activeVideoMode,
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
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = topSafePadding + 16.dp),
                    visible = videoZoomAndContentScaleState.showContentScaleIndicator,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    InfoPill(
                        info = stringResource(videoZoomAndContentScaleState.videoContentScale.nameRes()),
                    )
                }
            }

            OverlayShowView(
                player = player,
                overlayView = overlayView,
                videoDecoderMode = state.decoderServiceState.videoMode,
                audioDecoderMode = state.decoderServiceState.audioMode,
                activeVideoDecoderMode = state.decoderServiceState.activeVideoMode,
                activeAudioDecoderMode = state.decoderServiceState.activeAudioMode,
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
