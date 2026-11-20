package com.sipun.playora.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.model.VideoContentScale
import com.sipun.playora.feature.player.extensions.descriptionRes
import com.sipun.playora.feature.player.extensions.isPortrait
import com.sipun.playora.feature.player.extensions.nameRes

@Composable
fun BoxScope.VideoContentScaleSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    videoContentScale: VideoContentScale,
    onVideoContentScaleChanged: (VideoContentScale) -> Unit,
    onDismiss: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait

    AnimatedVisibility(
        modifier = Modifier.align(
            alignment = if (isPortrait) {
                Alignment.BottomCenter
            } else {
                Alignment.CenterEnd
            },
        ),
        visible = show,
        enter = if (isPortrait) {
            slideInVertically { it }
        } else {
            slideInHorizontally { it }
        },
        exit = if (isPortrait) {
            slideOutVertically { it }
        } else {
            slideOutHorizontally { it }
        },
    ) {
        Surface(
            shape = if (isPortrait) {
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            } else {
                RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
            },
            color = Color(0xFF0F1418),
            modifier = modifier
                .then(
                    if (isPortrait) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier
                            .fillMaxWidth(fraction = 0.5f)
                            .fillMaxHeight()
                    },
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .background(
                                color = Color(0xFF5E666D),
                                shape = RoundedCornerShape(2.dp),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CropFree,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.video_zoom),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.video_zoom_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9EABB8),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    VideoContentScale.entries.forEach { contentScale ->
                        VideoContentScaleCard(
                            contentScale = contentScale,
                            isSelected = contentScale == videoContentScale,
                            onClick = {
                                onVideoContentScaleChanged(contentScale)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoContentScaleCard(
    contentScale: VideoContentScale,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1B2228),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFF262E36),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (contentScale) {
                    VideoContentScale.BEST_FIT -> {
                        Icon(
                            imageVector = Icons.Rounded.ZoomOutMap,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    VideoContentScale.STRETCH -> {
                        Icon(
                            imageVector = Icons.Rounded.Height,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(90f),
                        )
                    }

                    VideoContentScale.CROP -> {
                        Icon(
                            imageVector = Icons.Rounded.Crop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    VideoContentScale.HUNDRED_PERCENT -> {
                        Text(
                            text = "1:1",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(contentScale.nameRes()),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(contentScale.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9EABB8),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = Color(0xFF7A8693),
                ),
            )
        }
    }
}
