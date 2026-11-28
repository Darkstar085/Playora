package com.sipun.playora.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.ui.theme.AppDarkGlassSheet
import com.sipun.playora.feature.player.extensions.getName
import com.sipun.playora.feature.player.state.SubtitleOptionsEvent
import com.sipun.playora.feature.player.state.rememberSubtitleOptionsState
import com.sipun.playora.feature.player.state.rememberTracksState
import java.util.Locale
import kotlin.math.round

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BoxScope.SubtitleSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    player: Player,
    onSelectSubtitleClick: () -> Unit,
    onEvent: (SubtitleOptionsEvent) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val subtitleTracksState = rememberTracksState(
        player = player,
        C.TRACK_TYPE_TEXT,
    )
    val subtitleOptionsState = rememberSubtitleOptionsState(
        player = player,
        onEvent = onEvent,
    )
    val hapticFeedback = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = if (isPortrait) Alignment.BottomCenter else Alignment.CenterEnd,
        ) {
            AnimatedVisibility(
                visible = show,
                enter = if (isPortrait) {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(220),
                    ) + fadeIn(animationSpec = tween(220))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(220),
                    ) + fadeIn(animationSpec = tween(220))
                },
                exit = if (isPortrait) {
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(180),
                    ) + fadeOut(animationSpec = tween(180))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(180),
                    ) + fadeOut(animationSpec = tween(180))
                },
                modifier = Modifier.align(if (isPortrait) Alignment.BottomCenter else Alignment.CenterEnd),
            ) {
                AppDarkGlassSheet(
                    shape = if (isPortrait) {
                        RoundedCornerShape(24.dp)
                    } else {
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    },
                    modifier = modifier.then(
                        if (isPortrait) {
                            Modifier
                                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                                .widthIn(max = 440.dp)
                                .fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxHeight()
                                .width(360.dp)
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .then(if (isPortrait) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                            .padding(if (isPortrait) 20.dp else 20.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isPortrait) 14.dp else 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF261D36)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ClosedCaption,
                                    contentDescription = null,
                                    tint = Color(0xFFBA86FC),
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.subtitle_track),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                    ),
                                    color = Color.White,
                                )
                                Text(
                                    text = stringResource(R.string.select_subtitle_and_adjust),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF90A4AE),
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isPortrait) {
                                        Modifier.heightIn(max = 280.dp)
                                    } else {
                                        Modifier.weight(1f, fill = false)
                                    }
                                )
                                .verticalScroll(state = rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            subtitleTracksState.tracks.forEachIndexed { index, track ->
                                SubtitleTrackRow(
                                    name = track.mediaTrackGroup.getName(
                                        C.TRACK_TYPE_TEXT,
                                        index = index,
                                    ),
                                    isSelected = track.isSelected,
                                    onClick = {
                                        subtitleTracksState.switchTrack(index = index)
                                        onDismiss()
                                    },
                                )
                            }

                            SubtitleTrackRow(
                                name = stringResource(R.string.disable),
                                isSelected = subtitleTracksState.tracks.none { it.isSelected },
                                onClick = {
                                    subtitleTracksState.switchTrack(index = -1)
                                    onDismiss()
                                },
                            )
                        }

                        HorizontalDivider(
                            color = Color(0xFF232D37),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            StepperRow(
                                icon = Icons.Rounded.Schedule,
                                title = stringResource(R.string.delay),
                                valueText = String.format(
                                    Locale.US,
                                    "%.2f sec",
                                    subtitleOptionsState.delayMilliseconds / 1000.0,
                                ),
                                onMinusClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    subtitleOptionsState.setDelay(subtitleOptionsState.delayMilliseconds - 250L)
                                },
                                onPlusClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    subtitleOptionsState.setDelay(subtitleOptionsState.delayMilliseconds + 250L)
                                },
                            )

                            StepperRow(
                                icon = Icons.Rounded.Speed,
                                title = stringResource(R.string.speed),
                                valueText = String.format(
                                    Locale.US,
                                    "%.2f×",
                                    subtitleOptionsState.speedMultiplier,
                                ),
                                onMinusClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val newSpeed =
                                        (round((subtitleOptionsState.speedMultiplier - 0.05f) * 100f) / 100f)
                                            .coerceAtLeast(0.25f)
                                    subtitleOptionsState.setSpeed(newSpeed)
                                },
                                onPlusClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val newSpeed =
                                        (round((subtitleOptionsState.speedMultiplier + 0.05f) * 100f) / 100f)
                                            .coerceAtMost(4.0f)
                                    subtitleOptionsState.setSpeed(newSpeed)
                                },
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF221A2C),
                            border = BorderStroke(1.dp, Color(0xFF382C44)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSubtitleClick()
                                    onDismiss()
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpen,
                                    contentDescription = null,
                                    tint = Color(0xFFBA86FC),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.open_subtitle),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    ),
                                    color = Color(0xFFE1D5F5),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleTrackRow(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF14344D) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Color(0xFF2196F3) else Color(0xFF78909C),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF2196F3), CircleShape),
                    )
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.5.sp,
                ),
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StepperRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    valueText: String,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                ),
                color = Color.White,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onMinusClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1B2E3D), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = stringResource(R.string.decrease),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Box(
                modifier = Modifier
                    .width(86.dp)
                    .height(36.dp)
                    .background(Color(0xFF182028), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF242F3A), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                    ),
                    color = Color.White,
                )
            }

            IconButton(
                onClick = onPlusClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1B2E3D), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.increase),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
