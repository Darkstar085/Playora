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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.common.extensions.getFilenameFromUri
import com.sipun.playora.feature.player.extensions.externalAudio
import com.sipun.playora.feature.player.extensions.externalAudioIndex
import com.sipun.playora.feature.player.extensions.getName
import com.sipun.playora.feature.player.extensions.isPortrait
import com.sipun.playora.feature.player.state.rememberTracksState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun BoxScope.AudioTrackSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    player: Player,
    onSelectAudioClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val audioTracksState = rememberTracksState(
        player = player,
        C.TRACK_TYPE_AUDIO,
    )
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait

    var audioNames by remember { mutableStateOf(value = emptyList<String>()) }
    LaunchedEffect(
        key1 = player,
        audioTracksState.tracks,
    ) {
        val uris = player.currentMediaItem?.mediaMetadata?.externalAudio.orEmpty()
        audioNames = withContext(Dispatchers.IO) {
            uris.map { context.getFilenameFromUri(uri = it) }
        }
    }

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
                Surface(
                    shape = if (isPortrait) {
                        RoundedCornerShape(24.dp)
                    } else {
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    },
                    color = Color(0xF012171E),
                    border = BorderStroke(1.dp, Color(0xFF26323D)),
                    shadowElevation = 12.dp,
                    modifier = modifier
                        .then(
                            if (isPortrait) {
                                Modifier
                                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                                    .widthIn(max = 440.dp)
                                    .fillMaxWidth()
                            } else {
                                Modifier
                                    .fillMaxHeight()
                                    .width(350.dp)
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {},
                ) {
                    Column(
                        modifier = Modifier
                            .then(if (isPortrait) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                            .padding(if (isPortrait) 20.dp else 20.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isPortrait) 16.dp else 14.dp),
                    ) {
                        // Header: Icon box + Title/Subtitle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF0F2639)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF29B6F6),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Audio track",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                    ),
                                    color = Color.White,
                                )
                                Text(
                                    text = "Select audio track",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF90A4AE),
                                )
                            }
                        }

                        // Track list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isPortrait) {
                                        Modifier.heightIn(max = 320.dp)
                                    } else {
                                        Modifier.weight(1f, fill = false)
                                    }
                                )
                                .verticalScroll(state = rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            audioTracksState.tracks.forEachIndexed { index, track ->
                                val trackName = track.mediaTrackGroup.externalAudioIndex
                                    ?.let { audioNames.getOrNull(index = it) }
                                    ?: track.mediaTrackGroup.getName(
                                        C.TRACK_TYPE_AUDIO,
                                        index = index,
                                    )
                                AudioTrackRow(
                                    name = trackName,
                                    isSelected = track.isSelected,
                                    isDisable = false,
                                    onClick = {
                                        audioTracksState.switchTrack(index = index)
                                        onDismiss()
                                    },
                                )
                            }

                            // Disable option
                            AudioTrackRow(
                                name = stringResource(R.string.disable),
                                isSelected = audioTracksState.tracks.none { it.isSelected },
                                isDisable = true,
                                onClick = {
                                    audioTracksState.switchTrack(index = -1)
                                    onDismiss()
                                },
                            )
                        }

                        // Open local audio button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF18222B),
                            border = BorderStroke(1.dp, Color(0xFF2A3A48)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAudioClick()
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
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.open_audio),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                    ),
                                    color = Color.White,
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
private fun AudioTrackRow(
    name: String,
    isSelected: Boolean,
    isDisable: Boolean,
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = if (isDisable) Icons.AutoMirrored.Rounded.VolumeOff else Icons.Rounded.Headphones,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFFB0BEC5),
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.5.sp,
                ),
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
