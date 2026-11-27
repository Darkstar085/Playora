package com.sipun.playora.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.ui.theme.AppDarkGlassSheet
import com.sipun.playora.feature.player.model.DecoderMode
import com.sipun.playora.feature.player.model.DecoderTrackType
import com.sipun.playora.feature.player.model.descriptionRes
import com.sipun.playora.feature.player.model.labelRes

@Composable
fun BoxScope.DecoderSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    videoMode: DecoderMode?,
    audioMode: DecoderMode?,
    onVideoModeSelected: (DecoderMode) -> Unit,
    onAudioModeSelected: (DecoderMode) -> Unit,
    onDismiss: () -> Unit = {},
) {
    var selectedTrackType by rememberSaveable { mutableStateOf(DecoderTrackType.VIDEO) }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        // Full screen scrim
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
                        verticalArrangement = Arrangement.spacedBy(if (isPortrait) 16.dp else 16.dp),
                    ) {
                        // Header: Green Chip Icon Box + Title/Subtitle (NO drag handle indicator!)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF142C1E)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Memory,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Decoders",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                    ),
                                    color = Color.White,
                                )
                                Text(
                                    text = "Choose video and audio decoders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF90A4AE),
                                )
                            }
                        }

                        // Segmented Pill Tab Switcher: Video vs Audio
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF141C24))
                                .border(1.dp, Color(0xFF283644), RoundedCornerShape(22.dp))
                                .padding(3.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                // Video Tab
                                val isVideoSelected = selectedTrackType == DecoderTrackType.VIDEO
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(19.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { selectedTrackType = DecoderTrackType.VIDEO }
                                        .background(if (isVideoSelected) Color(0xFF194C72) else Color.Transparent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isVideoSelected) Color.White else Color(
                                                0xFF90A4AE
                                            ),
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.video),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isVideoSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                            ),
                                            color = if (isVideoSelected) Color.White else Color(
                                                0xFF90A4AE
                                            ),
                                        )
                                    }
                                }

                                // Audio Tab
                                val isAudioSelected = selectedTrackType == DecoderTrackType.AUDIO
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(19.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { selectedTrackType = DecoderTrackType.AUDIO }
                                        .background(if (isAudioSelected) Color(0xFF194C72) else Color.Transparent),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.MusicNote,
                                            contentDescription = null,
                                            tint = if (isAudioSelected) Color.White else Color(
                                                0xFF90A4AE
                                            ),
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.audio),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isAudioSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                            ),
                                            color = if (isAudioSelected) Color.White else Color(
                                                0xFF90A4AE
                                            ),
                                        )
                                    }
                                }
                            }
                        }

                        // Decoder Options List
                        val currentMode = when (selectedTrackType) {
                            DecoderTrackType.VIDEO -> videoMode ?: DecoderMode.HARDWARE
                            DecoderTrackType.AUDIO -> audioMode ?: DecoderMode.HARDWARE
                        }
                        val onSelectMode = when (selectedTrackType) {
                            DecoderTrackType.VIDEO -> onVideoModeSelected
                            DecoderTrackType.AUDIO -> onAudioModeSelected
                        }

                        val modes = remember(selectedTrackType) {
                            when (selectedTrackType) {
                                DecoderTrackType.VIDEO -> listOf(
                                    DecoderMode.HARDWARE,
                                    DecoderMode.HW_PLUS,
                                    DecoderMode.FFMPEG,
                                )

                                DecoderTrackType.AUDIO -> listOf(
                                    DecoderMode.HARDWARE,
                                    DecoderMode.FFMPEG,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isPortrait) Modifier else Modifier.weight(
                                        1f,
                                        fill = false
                                    )
                                )
                                .verticalScroll(state = rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            modes.forEach { mode ->
                                DecoderModeRow(
                                    label = stringResource(mode.labelRes),
                                    description = stringResource(mode.descriptionRes),
                                    isSelected = currentMode == mode,
                                    onClick = { onSelectMode(mode) },
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
private fun DecoderModeRow(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Radio button
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = Color.White,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                    ),
                    color = Color(0xFF90A4AE),
                )
            }
        }
    }
}
