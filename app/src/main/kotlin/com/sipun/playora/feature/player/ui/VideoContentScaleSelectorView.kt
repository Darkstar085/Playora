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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.model.VideoContentScale
import com.sipun.playora.core.ui.theme.AppDarkGlassSheet
import com.sipun.playora.feature.player.extensions.descriptionRes
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
                                .fillMaxWidth()
                                .widthIn(max = 440.dp)
                        } else {
                            Modifier
                                .fillMaxHeight()
                                .width(350.dp)
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .then(if (isPortrait) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = Color(0xFF1E2833),
                                        shape = RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CropFree,
                                    contentDescription = null,
                                    tint = Color(0xFF4CA5FF),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.video_zoom),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.video_zoom_description),
                                    color = Color(0xFF8E9BA8),
                                    fontSize = 12.sp,
                                )
                            }
                        }

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
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF182028),
        border = if (isSelected) {
            BorderStroke(1.5.dp, Color(0xFF1B88F5))
        } else {
            BorderStroke(1.dp, Color(0x3326323D))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFF222B36),
                        shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (contentScale) {
                    VideoContentScale.BEST_FIT -> {
                        Icon(
                            imageVector = Icons.Rounded.ZoomOutMap,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    VideoContentScale.STRETCH -> {
                        Icon(
                            imageVector = Icons.Rounded.Height,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(90f),
                        )
                    }

                    VideoContentScale.CROP -> {
                        Icon(
                            imageVector = Icons.Rounded.Crop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    VideoContentScale.HUNDRED_PERCENT -> {
                        Text(
                            text = "1:1",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                            fontSize = 15.sp,
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
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(contentScale.descriptionRes()),
                    color = Color(0xFF8E9BA8),
                    fontSize = 11.5.sp,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF1B88F5),
                    unselectedColor = Color(0xFF5E6E7E),
                ),
            )
        }
    }
}
