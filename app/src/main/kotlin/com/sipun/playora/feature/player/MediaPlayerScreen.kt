package com.sipun.playora.feature.player

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassBackdrop
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.PlayerOrientationEffect
import com.sipun.playora.feature.player.state.rememberBrightnessState
import com.sipun.playora.feature.player.state.rememberControlsVisibilityState
import com.sipun.playora.feature.player.state.rememberCuesState
import com.sipun.playora.feature.player.state.rememberErrorState
import com.sipun.playora.feature.player.state.rememberPictureInPictureState
import com.sipun.playora.feature.player.state.rememberSeekGestureState
import com.sipun.playora.feature.player.state.rememberSubtitlePositionState
import com.sipun.playora.feature.player.state.rememberTapGestureState
import com.sipun.playora.feature.player.state.rememberVideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.rememberVolumeAndBrightnessGestureState
import com.sipun.playora.feature.player.state.rememberVolumeState
import com.sipun.playora.feature.player.ui.PlayerErrorDialogs
import com.sipun.playora.feature.player.ui.PlayerGestures
import com.sipun.playora.feature.player.ui.PlayerVerticalGestureIndicators
import com.sipun.playora.feature.player.ui.SubtitleConfiguration
import com.sipun.playora.feature.player.utils.PlayerApi
import io.github.itshedimisawi.vitro.rememberGlassBackdrop
import kotlin.time.Duration.Companion.seconds

val LocalControlsVisibilityState = compositionLocalOf<ControlsVisibilityState?> { null }

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen(viewModel: PlayerViewModel, player: Player?, api: PlayerApi? = null) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MediaPlayerContent(
        player = player,
        state = state,
        onAction = viewModel::onAction,
        api = api,
    )
}

@OptIn(UnstableApi::class)
@Composable
internal fun MediaPlayerContent(
    player: Player?,
    state: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    api: PlayerApi? = null,
) {
    val playerPreferences = state.playerPreferences
    val volumeState = rememberVolumeState(
        player = player,
        showVolumePanelIfHeadsetIsOn = playerPreferences.showSystemVolumePanel,
    )
    if (player == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        )
        return
    }
    val context = LocalContext.current
    val controlsVisibilityState = rememberControlsVisibilityState(
        player = player,
        hideAfter = 4.seconds,
    )
    val tapGestureState = rememberTapGestureState(
        player = player,
        doubleTapGesture = playerPreferences.doubleTapGesture,
        seekIncrementMillis = playerPreferences.seekIncrement.seconds.inWholeMilliseconds,
        useLongPressGesture = playerPreferences.useLongPressControls,
        longPressSpeed = playerPreferences.longPressControlsSpeed,
    )
    val seekGestureState = rememberSeekGestureState(
        player = player,
        sensitivity = 1.0f,
        enableSeekGesture = playerPreferences.useSeekControls,
    )
    val pictureInPictureState = rememberPictureInPictureState(
        player = player,
        autoEnter = playerPreferences.autoPip,
    )
    val videoZoomAndContentScaleState = rememberVideoZoomAndContentScaleState(
        player = player,
        initialContentScale = api?.videoContentScale ?: playerPreferences.playerVideoZoom,
        enableZoomGesture = playerPreferences.useZoomControls,
        enablePanGesture = true,
        onEvent = { onAction(PlayerAction.OnVideoZoomEvent(event = it)) },
    )
    val brightnessState = rememberBrightnessState()
    val volumeAndBrightnessGestureState = rememberVolumeAndBrightnessGestureState(
        volumeState = volumeState,
        brightnessState = brightnessState,
        enableVolumeGesture = playerPreferences.enableVolumeSwipeGesture,
        enableBrightnessGesture = playerPreferences.enableBrightnessSwipeGesture,
        volumeGestureSensitivity = 1.0f,
        brightnessGestureSensitivity = 1.0f,
    )
    PlayerOrientationEffect(
        player = player,
        orientationOverride = api?.orientation,
    )
    val errorState = rememberErrorState(player = player)
    val cuesState = rememberCuesState(player = player)
    val subtitlePositionState = rememberSubtitlePositionState(
        savedOffsetY = playerPreferences.subtitleOffsetY,
        onPositionSaved = { onAction(PlayerAction.UpdateSubtitleOffset(it)) },
    )
    val hapticFeedback = LocalHapticFeedback.current
    var wasDraggingSubtitle by remember { mutableStateOf(false) }
    LaunchedEffect(subtitlePositionState.isDragging) {
        if (subtitlePositionState.isDragging) {
            wasDraggingSubtitle = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            controlsVisibilityState.hideControls()
        } else if (wasDraggingSubtitle) {
            wasDraggingSubtitle = false
            controlsVisibilityState.showControls()
        }
    }

    LaunchedEffect(pictureInPictureState.isInPictureInPictureMode) {
        if (pictureInPictureState.isInPictureInPictureMode) {
            controlsVisibilityState.hideControls()
        }
    }
    LaunchedEffect(tapGestureState.isLongPressGestureInAction) {
        if (tapGestureState.isLongPressGestureInAction) controlsVisibilityState.hideControls()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        brightnessState.setBrightness(playerPreferences.playerBrightness)
    }
    LaunchedEffect(brightnessState.currentBrightness) {
        onAction(PlayerAction.UpdatePlayerBrightness(brightnessState.currentBrightness))
    }

    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val playerBackdrop = rememberGlassBackdrop()

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) playerBackdrop else null,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .appGlassBackdrop(playerBackdrop, enabled = liquidGlassEnabled)
                    .background(Color.Black),
            ) {
                PlayerContentFrame(
                    player = player,
                    pictureInPictureState = pictureInPictureState,
                    videoZoomAndContentScaleState = videoZoomAndContentScaleState,
                    videoVisible = api?.videoVisible ?: true,
                    displayAspectRatio = api?.displayAspectRatio,
                    cuesState = cuesState,
                    subtitlePositionState = subtitlePositionState,
                    subtitleConfiguration = SubtitleConfiguration(
                        useSystemCaptionStyle = playerPreferences.useSystemCaptionStyle,
                        showBackground = playerPreferences.subtitleBackground,
                        font = playerPreferences.subtitleFont,
                        textSize = playerPreferences.subtitleTextSize,
                        textBold = playerPreferences.subtitleTextBold,
                        applyEmbeddedStyles = true,
                        controlsVisible = controlsVisibilityState.controlsVisible &&
                            !controlsVisibilityState.controlsLocked,
                    ),
                )
                PlayerGestures(
                    controlsVisibilityState = controlsVisibilityState,
                    tapGestureState = tapGestureState,
                    pictureInPictureState = pictureInPictureState,
                    seekGestureState = seekGestureState,
                    videoZoomAndContentScaleState = videoZoomAndContentScaleState,
                    volumeAndBrightnessGestureState = volumeAndBrightnessGestureState,
                    subtitlePositionState = subtitlePositionState,
                    hasSubtitles = cuesState.currentCues?.cues?.isNotEmpty() == true,
                    canRepositionSubtitles = cuesState.canReposition,
                )
            }
            MediaPlayerControls(
                player = player,
                state = state,
                onAction = onAction,
                controlsVisibilityState = controlsVisibilityState,
                tapGestureState = tapGestureState,
                seekGestureState = seekGestureState,
                videoZoomAndContentScaleState = videoZoomAndContentScaleState,
                dpadUpDown = api?.dpadUpDown,
                isPipSupported = pictureInPictureState.isPipSupported,
                onPictureInPictureClick = {
                    val entered = pictureInPictureState.enterPictureInPictureMode()
                    if (!entered && !pictureInPictureState.hasPipPermission) {
                        Toast.makeText(
                            context,
                            R.string.enable_pip_from_settings,
                            Toast.LENGTH_SHORT,
                        ).show()
                        pictureInPictureState.openPictureInPictureSettings()
                    }
                },
            )
            PlayerVerticalGestureIndicators(
                activeGesture = volumeAndBrightnessGestureState.activeGesture,
                volumePercentage = volumeState.volumePercentage,
                maxVolumePercentage = volumeState.maxVolumePercentage,
                brightnessPercentage = brightnessState.brightnessPercentage,
            )
        }
    }

    if (api?.suppressErrorMessage != true) {
        PlayerErrorDialogs(
            decoderRecoveryState = state.decoderServiceState.recoveryState,
            playbackError = errorState.playbackError,
            hasNextMediaItem = player.hasNextMediaItem(),
            onTryDecoderFallback = { onAction(PlayerAction.TryDecoderFallback) },
            onPlayNextVideo = {
                errorState.dismiss()
                player.seekToNextMediaItem()
                player.play()
            },
            onExit = {
                errorState.dismiss()
                onAction(PlayerAction.NavigateUp)
            },
        )
    }
}
