package com.sipun.playora.feature.player.buttons

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPlaybackSpeedState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.media3.ui.compose.state.rememberRepeatButtonState
import androidx.media3.ui.compose.state.rememberShuffleButtonState
import com.sipun.playora.R
import com.sipun.playora.core.common.extensions.round
import com.sipun.playora.feature.player.LocalControlsVisibilityState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds
import com.sipun.playora.R as coreUiR

@OptIn(UnstableApi::class)
@Composable
internal fun NextButton(modifier: Modifier = Modifier, player: Player?) {
    val state = rememberNextButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier.size(48.dp),
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = stringResource(coreUiR.string.player_controls_next),
            modifier = Modifier.size(28.dp),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayPauseButton(modifier: Modifier = Modifier, player: Player?) {
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
        modifier = modifier.size(64.dp),
        enabled = state.isEnabled,
        onClick = state::onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(48.dp),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlaybackSpeedButton(player: Player?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val state = rememberPlaybackSpeedState(player = player)
    val description = stringResource(R.string.select_playback_speed)
    PlayerButton(
        modifier = modifier.semantics { contentDescription = description },
        enabled = state.isEnabled,
        onClick = onClick,
        containerColor = PlayerButtonBlackAlpha,
        contentPadding = PaddingValues(4.dp),
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = state.playbackSpeed.round(decimalPlaces = 2).toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = LocalContentColor.current,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.labelLarge.fontSize,
                    minFontSize = 6.sp,
                    stepSize = 0.5.sp,
                ),
            )
        }
    }
}

val PlayerButtonBlackAlpha = Color.Black.copy(alpha = 0.3f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val viewConfiguration = LocalViewConfiguration.current
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(key1 = interactionSource) {
        var isLongPressClicked = false
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongPressClicked = false
                    delay(viewConfiguration.longPressTimeoutMillis.milliseconds)
                    onLongClick?.let {
                        isLongPressClicked = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        it.invoke()
                    }
                }

                is PressInteraction.Release -> {
                    if (!isLongPressClicked) {
                        onClick()
                    }
                }
            }
        }
    }

    val colors = IconButtonDefaults.iconButtonColors(
        contentColor = Color.White,
        containerColor = containerColor,
    )

    val rippleConfiguration = RippleConfiguration(
        color = Color.White,
        rippleAlpha = RippleAlpha(
            pressedAlpha = 0.5f,
            focusedAlpha = 0.5f,
            draggedAlpha = 0.5f,
            hoveredAlpha = 0.5f,
        ),
    )

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
                .clip(shape = CircleShape)
                .background(
                    color = if (enabled) colors.containerColor else colors.disabledContainerColor,
                )
                .clickable(
                    interactionSource = interactionSource,
                    enabled = enabled,
                    onClick = {},
                )
                .padding(paddingValues = contentPadding),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
internal fun PreviousButton(modifier: Modifier = Modifier, player: Player?) {
    val state = rememberPreviousButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier.size(48.dp),
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = stringResource(coreUiR.string.player_controls_previous),
            modifier = Modifier.size(28.dp),
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LoopButton(modifier: Modifier = Modifier, player: Player?) {
    val state = rememberRepeatButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier,
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = repeatModeIconVector(state.repeatModeState),
            contentDescription = repeatModeContentDescription(state.repeatModeState),
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
fun ShuffleButton(modifier: Modifier = Modifier, player: Player?) {
    val state = rememberShuffleButtonState(player = player)
    val controlsVisibilityState = LocalControlsVisibilityState.current

    PlayerButton(
        modifier = modifier,
        enabled = state.isEnabled,
        onClick = {
            state.onClick()
            controlsVisibilityState?.showControls()
        },
    ) {
        Icon(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = shuffleContentDescription(state.shuffleOn),
        )
    }
}

@Composable
private fun shuffleContentDescription(shuffleOn: Boolean): String = when (shuffleOn) {
    true -> stringResource(coreUiR.string.shuffle_on)
    false -> stringResource(coreUiR.string.shuffle_off)
}
