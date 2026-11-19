package com.sipun.playora.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getStringForTime
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import com.sipun.playora.core.model.VideoContentScale
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.player.buttons.NextButton
import com.sipun.playora.feature.player.buttons.PlayPauseButton
import com.sipun.playora.feature.player.buttons.PlayerButton
import com.sipun.playora.feature.player.buttons.PlayerButtonBlackAlpha
import com.sipun.playora.feature.player.buttons.PreviousButton
import com.sipun.playora.feature.player.extensions.imageVector
import com.sipun.playora.feature.player.state.ChaptersState
import com.sipun.playora.feature.player.state.rememberChaptersState
import com.sipun.playora.feature.player.ui.preview.rememberPreviewPlayer

private const val MILLISECONDS_PER_SECOND = 1_000L

@OptIn(UnstableApi::class)
@Composable
fun ControlsBottomView(
    modifier: Modifier = Modifier,
    player: Player?,
    progressState: ProgressStateWithTickInterval,
    chaptersState: ChaptersState,
    videoContentScale: VideoContentScale,
    isPipSupported: Boolean,
    showRemainingTime: Boolean,
    onToggleTimeDisplay: () -> Unit,
    onVideoContentScaleClick: () -> Unit,
    onVideoContentScaleLongClick: () -> Unit,
    onLockControlsClick: () -> Unit,
    onPictureInPictureClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    onChaptersClick: () -> Unit = {},
    onPlaybackSpeedClick: () -> Unit = {},
    onPlayInBackgroundClick: () -> Unit = {},
    controlsAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()

    val timeTextPositionMs by remember(progressState) {
        derivedStateOf {
            val wholeSeconds =
                progressState.currentPositionMs / MILLISECONDS_PER_SECOND
            wholeSeconds * MILLISECONDS_PER_SECOND
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.45f),
                        Color.Black.copy(alpha = 0.8f),
                    ),
                ),
            )
            .padding(systemBarsPadding.copy(top = 0.dp))
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp)
            .padding(
                bottom = 16.dp.takeIf { systemBarsPadding.calculateBottomPadding() == 0.dp } ?: 0.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Row 1: Seekbar with timers
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = getStringForTime(timeTextPositionMs),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(end = 8.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                PlayerSeekbar(
                    position = progressState.currentPositionMs.toFloat(),
                    duration = progressState.durationMs.toFloat(),
                    chapters = chaptersState.chapters,
                    onSeek = { onSeek(it.toLong()) },
                    onSeekFinished = { onSeekEnd() },
                )
            }

            Text(
                text = buildString {
                    if (showRemainingTime && progressState.durationMs != C.TIME_UNSET) {
                        val remainingMs = progressState.durationMs - timeTextPositionMs
                        append("-")
                        append(getStringForTime(remainingMs.coerceAtLeast(0)))
                    } else {
                        append(getStringForTime(progressState.durationMs))
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .clickable { onToggleTimeDisplay() }
                    .padding(start = 8.dp),
            )
        }

        // Row 2: Bottom controls row (Lock | Prev Play Next | Scale PiP)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlayerButton(
                modifier = Modifier.size(40.dp),
                onClick = onLockControlsClick,
                containerColor = PlayerButtonBlackAlpha,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                PreviousButton(player = player)
                PlayPauseButton(player = player)
                NextButton(player = player)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlayerButton(
                    modifier = Modifier.size(40.dp),
                    onClick = onVideoContentScaleClick,
                    onLongClick = onVideoContentScaleLongClick,
                    containerColor = PlayerButtonBlackAlpha,
                ) {
                    Icon(
                        imageVector = videoContentScale.imageVector(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (isPipSupported) {
                    PlayerButton(
                        modifier = Modifier.size(40.dp),
                        onClick = onPictureInPictureClick,
                        containerColor = PlayerButtonBlackAlpha,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureInPictureAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
private fun ControlsBottomViewPreview() {
    val player = rememberPreviewPlayer()
    val progressState = rememberProgressStateWithTickInterval(player = player)
    PlayoraTheme(darkTheme = true) {
        Surface {
            ControlsBottomView(
                player = player,
                progressState = progressState,
                chaptersState = rememberChaptersState(
                    player = player,
                    progressState = progressState,
                ),
                videoContentScale = VideoContentScale.BEST_FIT,
                isPipSupported = true,
                showRemainingTime = false,
                onToggleTimeDisplay = {},
                onVideoContentScaleClick = {},
                onVideoContentScaleLongClick = {},
                onLockControlsClick = {},
                onPictureInPictureClick = {},
                onSeek = {},
                onSeekEnd = {},
            )
        }
    }
}
