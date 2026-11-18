package com.sipun.playora.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PictureInPicture
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getStringForTime
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import com.sipun.playora.R
import com.sipun.playora.core.model.VideoContentScale
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.player.buttons.LoopButton
import com.sipun.playora.feature.player.buttons.PlaybackSpeedButton
import com.sipun.playora.feature.player.buttons.PlayerButton
import com.sipun.playora.feature.player.buttons.PlayerButtonBlackAlpha
import com.sipun.playora.feature.player.buttons.ShuffleButton
import com.sipun.playora.feature.player.extensions.imageVector
import com.sipun.playora.feature.player.state.ChaptersState
import com.sipun.playora.feature.player.state.rememberChaptersState
import com.sipun.playora.feature.player.ui.preview.rememberPreviewPlayer
import com.sipun.playora.feature.player.ui.titleOrDefault

private const val MILLISECONDS_PER_SECOND = 1_000L

@OptIn(UnstableApi::class)
@Composable
fun ControlsBottomView(
    modifier: Modifier = Modifier,
    player: Player?,
    progressState: ProgressStateWithTickInterval,
    chaptersState: ChaptersState,
    controlsAlignment: Alignment.Horizontal,
    videoContentScale: VideoContentScale,
    isPipSupported: Boolean,
    showRemainingTime: Boolean,
    onToggleTimeDisplay: () -> Unit,
    onChaptersClick: () -> Unit,
    onVideoContentScaleClick: () -> Unit,
    onVideoContentScaleLongClick: () -> Unit,
    onLockControlsClick: () -> Unit,
    onPictureInPictureClick: () -> Unit,
    onPlaybackSpeedClick: () -> Unit,
    onPlayInBackgroundClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekEnd: () -> Unit,
) {
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    BoxWithConstraints(modifier = modifier) {
        val useCompactControls = maxWidth < 600.dp
        Column(
            modifier = Modifier
                .padding(systemBarsPadding.copy(top = 0.dp))
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .padding(
                    bottom =
                        16.dp.takeIf { systemBarsPadding.calculateBottomPadding() == 0.dp } ?: 0.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.weight(weight = 1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayerButton(
                        onClick = onToggleTimeDisplay,
                        containerColor = PlayerButtonBlackAlpha,
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = 1.dp,
                        ),
                    ) {
                        val timeTextPositionMs by remember(progressState) {
                            derivedStateOf {
                                val wholeSeconds =
                                    progressState.currentPositionMs / MILLISECONDS_PER_SECOND
                                wholeSeconds * MILLISECONDS_PER_SECOND
                            }
                        }
                        Text(
                            text = buildString {
                                val positionText = when (showRemainingTime) {
                                    true -> if (progressState.durationMs != C.TIME_UNSET) {
                                        val remainingMs =
                                            timeTextPositionMs - progressState.durationMs
                                        getStringForTime(remainingMs)
                                    } else {
                                        getStringForTime(C.TIME_UNSET)
                                    }

                                    false -> getStringForTime(timeTextPositionMs)
                                }
                                append(positionText)
                                append(" / ")
                                append(getStringForTime(progressState.durationMs))
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    if (chaptersState.chapters.isNotEmpty()) {
                        PlayerButton(
                            modifier = Modifier.weight(
                                weight = 1f,
                                fill = false,
                            ),
                            onClick = onChaptersClick,
                            containerColor = PlayerButtonBlackAlpha,
                            contentPadding = PaddingValues(
                                vertical = 1.dp,
                                horizontal = 8.dp,
                            ).copy(end = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text =
                                        chaptersState.chapters.getOrNull(
                                            chaptersState.currentChapterIndex,
                                        )
                                            ?.titleOrDefault(chaptersState.currentChapterIndex)
                                            ?: stringResource(R.string.chapters),
                                    modifier = Modifier.weight(
                                        weight = 1f,
                                        fill = false,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                if (!useCompactControls) {
                    PlaybackSpeedButton(
                        player = player,
                        onClick = onPlaybackSpeedClick,
                    )
                }
            }
            PlayerSeekbar(
                position = progressState.currentPositionMs.toFloat(),
                duration = progressState.durationMs.toFloat(),
                chapters = chaptersState.chapters,
                onSeek = { onSeek(it.toLong()) },
                onSeekFinished = { onSeekEnd() },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(state = rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = controlsAlignment,
                ),
            ) {
                PlayerButton(onClick = onLockControlsClick) {
                    Icon(
                        imageVector = Icons.Rounded.LockOpen,
                        contentDescription = null,
                    )
                }
                PlayerButton(
                    onClick = onVideoContentScaleClick,
                    onLongClick = onVideoContentScaleLongClick,
                ) {
                    Icon(
                        imageVector = videoContentScale.imageVector(),
                        contentDescription = null,
                    )
                }
                if (isPipSupported) {
                    PlayerButton(onClick = onPictureInPictureClick) {
                        Icon(
                            imageVector = Icons.Rounded.PictureInPicture,
                            contentDescription = null,
                        )
                    }
                }
                PlayerButton(onClick = onPlayInBackgroundClick) {
                    Icon(
                        imageVector = Icons.Rounded.Headset,
                        contentDescription = null,
                    )
                }
                LoopButton(player = player)
                ShuffleButton(player = player)
                if (useCompactControls) {
                    PlaybackSpeedButton(
                        player = player,
                        onClick = onPlaybackSpeedClick,
                    )
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
                controlsAlignment = Alignment.Start,
                videoContentScale = VideoContentScale.BEST_FIT,
                isPipSupported = true,
                showRemainingTime = false,
                onToggleTimeDisplay = {},
                onChaptersClick = {},
                onVideoContentScaleClick = {},
                onVideoContentScaleLongClick = {},
                onLockControlsClick = {},
                onPictureInPictureClick = {},
                onPlaybackSpeedClick = {},
                onPlayInBackgroundClick = {},
                onSeek = {},
                onSeekEnd = {},
            )
        }
    }
}
