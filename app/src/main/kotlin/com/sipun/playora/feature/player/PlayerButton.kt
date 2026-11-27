package com.sipun.playora.feature.player.buttons

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.media3.ui.compose.state.rememberRepeatButtonState
import androidx.media3.ui.compose.state.rememberShuffleButtonState
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.feature.player.LocalControlsVisibilityState
import com.sipun.playora.R as coreUiR

@OptIn(UnstableApi::class)
@Composable
internal fun NextButton(
    modifier: Modifier = Modifier.size(48.dp),
    player: Player?,
    containerColor: Color = PlayerButtonBlackAlpha,
    iconSize: Dp = 28.dp,
) {
    val state = rememberNextButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier,
        containerColor = containerColor,
        enabled = state.isEnabled,
        onClick = {
            if (player?.hasNextMediaItem() == true) {
                player.seekToNextMediaItem()
            } else {
                state.onClick()
            }
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = stringResource(coreUiR.string.player_controls_next),
            modifier = Modifier.size(iconSize),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier.size(56.dp),
    player: Player?,
    containerColor: Color = PlayerButtonBlackAlpha,
    iconSize: Dp = 36.dp,
) {
    val state = rememberPlayPauseButtonState(player = player)
    val icon = when (state.showPlay) {
        true -> Icons.Rounded.PlayArrow
        false -> Icons.Rounded.Pause
    }
    val contentDescription = when (state.showPlay) {
        true -> stringResource(coreUiR.string.play_pause)
        false -> stringResource(coreUiR.string.play_pause)
    }

    PlayerButton(
        modifier = modifier,
        containerColor = containerColor,
        enabled = state.isEnabled,
        onClick = state::onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
        )
    }
}

val PlayerButtonBlackAlpha = Color.Black.copy(alpha = 0.35f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null

    val colors = IconButtonDefaults.iconButtonColors(
        contentColor = Color.White,
        containerColor = containerColor,
    )

    val rippleConfiguration = RippleConfiguration(color = Color.White)
    val isNeutralContainer = containerColor == Color.Transparent ||
        (containerColor.red == 0f && containerColor.green == 0f && containerColor.blue == 0f)
    val glassTint = if (isNeutralContainer) {
        Color.Unspecified
    } else {
        containerColor.copy(alpha = 0.45f)
    }

    CompositionLocalProvider(
        LocalContentColor provides if (enabled) {
            colors.contentColor
        } else {
            colors.disabledContentColor
        },
        LocalRippleConfiguration provides rippleConfiguration,
    ) {
        Box(
            modifier = modifier
                .then(
                    if (useGlass) {
                        Modifier.appGlassSurface(
                            shape = CircleShape,
                            tint = glassTint,
                            interactive = enabled,
                            clear = true,
                            dimmed = if (isNeutralContainer) 0.25f else 0.20f,
                            forceDark = true,
                            interactionSource = interactionSource,
                            fallbackBackground = if (enabled) {
                                colors.containerColor
                            } else {
                                colors.disabledContainerColor
                            },
                        )
                    } else {
                        Modifier
                            .clip(shape = CircleShape)
                            .background(
                                color = if (enabled) {
                                    colors.containerColor
                                } else {
                                    colors.disabledContainerColor
                                },
                            )
                    },
                )
                .then(
                    if (onLongClick != null) {
                        Modifier.combinedClickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            onClick = onClick,
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick()
                            },
                        )
                    } else {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = enabled,
                            onClick = onClick,
                        )
                    }
                )
                .padding(paddingValues = contentPadding),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun PreviousButton(
    modifier: Modifier = Modifier.size(48.dp),
    player: Player?,
    containerColor: Color = PlayerButtonBlackAlpha,
    iconSize: Dp = 28.dp,
) {
    val state = rememberPreviousButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier,
        containerColor = containerColor,
        enabled = state.isEnabled,
        onClick = {
            if (player?.hasPreviousMediaItem() == true) {
                player.seekToPreviousMediaItem()
            } else {
                state.onClick()
            }
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = stringResource(coreUiR.string.player_controls_previous),
            modifier = Modifier.size(iconSize),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LoopButton(
    modifier: Modifier = Modifier,
    player: Player?,
    containerColor: Color = PlayerButtonBlackAlpha,
) {
    val state = rememberRepeatButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current
    val isActive = state.repeatModeState != Player.REPEAT_MODE_OFF
    val activeAccent = MaterialTheme.colorScheme.primary

    PlayerButton(
        modifier = modifier.size(40.dp),
        containerColor = if (isActive) activeAccent.copy(alpha = 0.35f) else containerColor,
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = repeatModeIconVector(state.repeatModeState),
            contentDescription = repeatModeContentDescription(state.repeatModeState),
            modifier = Modifier.size(20.dp),
            tint = if (isActive) activeAccent else Color.White,
        )
    }
}

@Composable
private fun repeatModeIconVector(repeatMode: @Player.RepeatMode Int): ImageVector =
    when (repeatMode) {
        Player.REPEAT_MODE_OFF -> Icons.Rounded.Repeat
        Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
        else -> Icons.Rounded.Repeat
    }

@Composable
private fun repeatModeContentDescription(repeatMode: @Player.RepeatMode Int): String =
    when (repeatMode) {
        Player.REPEAT_MODE_OFF -> stringResource(coreUiR.string.loop_mode_off)
        Player.REPEAT_MODE_ONE -> stringResource(coreUiR.string.loop_mode_one)
        else -> stringResource(coreUiR.string.loop_mode_all)
    }

@OptIn(UnstableApi::class)
@Composable
fun ShuffleButton(
    modifier: Modifier = Modifier,
    player: Player?,
    containerColor: Color = PlayerButtonBlackAlpha,
) {
    val state = rememberShuffleButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current
    val isActive = state.shuffleOn
    val activeAccent = MaterialTheme.colorScheme.primary

    PlayerButton(
        modifier = modifier.size(40.dp),
        containerColor = if (isActive) activeAccent.copy(alpha = 0.35f) else containerColor,
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = shuffleContentDescription(state.shuffleOn),
            modifier = Modifier.size(20.dp),
            tint = if (isActive) activeAccent else Color.White,
        )
    }
}

@Composable
private fun shuffleContentDescription(shuffleOn: Boolean): String = when (shuffleOn) {
    true -> stringResource(coreUiR.string.shuffle_on)
    false -> stringResource(coreUiR.string.shuffle_off)
}
