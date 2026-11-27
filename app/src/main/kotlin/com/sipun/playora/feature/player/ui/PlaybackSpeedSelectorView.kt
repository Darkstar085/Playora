package com.sipun.playora.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.core.ui.theme.AppDarkGlassSheet
import com.sipun.playora.core.ui.theme.AppGlassSlider
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.feature.player.state.rememberPlaybackParametersState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun PlaybackSpeedSelectorView(
    modifier: Modifier = Modifier,
    show: Boolean,
    player: Player,
    onDismiss: () -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current
    val playbackParametersState = rememberPlaybackParametersState(player = player)
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.isPortrait
    val useGlass = LocalLiquidGlassEnabled.current

    val minValue = 0.25f
    val maxValue = 4.0f
    val stepSize = 0.05f

    var showCustomSpeedDialog by remember { mutableStateOf(false) }
    var customSpeedText by remember { mutableStateOf("") }

    if (showCustomSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showCustomSpeedDialog = false },
            title = {
                Text(
                    text = "Custom playback speed",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter speed between 0.25x and 8.00x",
                        color = Color(0xFF9EABB8),
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customSpeedText,
                        onValueChange = { customSpeedText = it },
                        singleLine = true,
                        placeholder = { Text("1.00") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = customSpeedText.toFloatOrNull()
                        if (parsed != null && parsed in 0.25f..8.0f) {
                            playbackParametersState.setPlaybackSpeed(parsed)
                        }
                        showCustomSpeedDialog = false
                    },
                ) {
                    Text("OK", color = Color(0xFF1B88F5))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSpeedDialog = false }) {
                    Text("Cancel", color = Color(0xFF9EABB8))
                }
            },
            containerColor = Color(0xFF1A222C),
        )
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = show,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(220),
                ) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(180),
                ) + fadeOut(animationSpec = tween(180)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                AppDarkGlassSheet(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 14.dp,
                    modifier = modifier.then(
                        if (isPortrait) {
                            Modifier
                                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .widthIn(max = 440.dp)
                        } else {
                            Modifier
                                .padding(bottom = 24.dp)
                                .width(520.dp)
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Top Row: Minus | Speed Pill (with Edit) | Plus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // Minus Button
                            val minusInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .then(
                                        if (useGlass) {
                                            Modifier
                                                .appGlassSurface(
                                                    shape = CircleShape,
                                                    clear = true,
                                                    tint = Color.White.copy(alpha = 0.08f),
                                                    dimmed = 0.22f,
                                                    forceDark = true,
                                                    interactive = true,
                                                    interactionSource = minusInteraction,
                                                    fallbackBackground = Color(0xFF202730),
                                                )
                                                .border(0.75.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                        } else {
                                            Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFF202730))
                                        },
                                    )
                                    .clickable(
                                        interactionSource = minusInteraction,
                                        indication = null,
                                    ) {
                                        val newSpeed =
                                            ((playbackParametersState.speed - stepSize) * 100).roundToInt() / 100f
                                        val clamped = newSpeed.coerceAtLeast(minValue)
                                        playbackParametersState.setPlaybackSpeed(clamped)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Decrease speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            // Center Speed Pill
                            val speedPillInteraction = remember { MutableInteractionSource() }
                            val speedPillModifier = if (useGlass) {
                                Modifier
                                    .appGlassSurface(
                                        shape = RoundedCornerShape(20.dp),
                                        clear = true,
                                        tint = Color.White.copy(alpha = 0.10f),
                                        dimmed = 0.20f,
                                        forceDark = true,
                                        interactive = true,
                                        interactionSource = speedPillInteraction,
                                        fallbackBackground = Color(0xFF202730),
                                        fallbackBorder = BorderStroke(1.dp, Color(0xFF2E3844)),
                                    )
                                    .border(0.75.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF202730))
                                    .border(1.dp, Color(0xFF2E3844), RoundedCornerShape(20.dp))
                            }
                            Row(
                                modifier = speedPillModifier
                                    .clickable(
                                        interactionSource = speedPillInteraction,
                                        indication = null,
                                    ) {
                                        customSpeedText = String.format(
                                            Locale.US,
                                            "%.2f",
                                            playbackParametersState.speed,
                                        )
                                        showCustomSpeedDialog = true
                                    }
                                    .padding(
                                        horizontal = 24.dp,
                                        vertical = 8.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%.2fx",
                                        playbackParametersState.speed,
                                    ),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit speed",
                                    tint = Color(0xFF9EABB8),
                                    modifier = Modifier.size(15.dp),
                                )
                            }

                            // Plus Button
                            val plusInteraction = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .then(
                                        if (useGlass) {
                                            Modifier
                                                .appGlassSurface(
                                                    shape = CircleShape,
                                                    clear = true,
                                                    tint = Color.White.copy(alpha = 0.08f),
                                                    dimmed = 0.22f,
                                                    forceDark = true,
                                                    interactive = true,
                                                    interactionSource = plusInteraction,
                                                    fallbackBackground = Color(0xFF202730),
                                                )
                                                .border(0.75.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                        } else {
                                            Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFF202730))
                                        },
                                    )
                                    .clickable(
                                        interactionSource = plusInteraction,
                                        indication = null,
                                    ) {
                                        val newSpeed =
                                            ((playbackParametersState.speed + stepSize) * 100).roundToInt() / 100f
                                        val clamped = newSpeed.coerceAtMost(maxValue)
                                        playbackParametersState.setPlaybackSpeed(clamped)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Increase speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }

                        // Middle Row: Slider with tick dots + Reset Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val tickValues = listOf(0.25f, 1.0f, 2.0f, 3.0f, 4.0f)

                                // Slider
                                AppGlassSlider(
                                    value = playbackParametersState.speed.coerceIn(
                                        minValue,
                                        maxValue,
                                    ),
                                    valueRange = minValue..maxValue,
                                    onValueChange = {
                                        val rounded = (it * 100).roundToInt() / 100f
                                        playbackParametersState.setPlaybackSpeed(rounded)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    forceDark = true,
                                    activeTrackColor = Color(0xFF1B88F5),
                                    inactiveTrackColor = Color(0xFF2A3542),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                // Tick dots on track
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                        .height(10.dp),
                                ) {
                                    val w = size.width
                                    val centerY = size.height / 2f
                                    tickValues.forEach { tick ->
                                        val fraction = (tick - minValue) / (maxValue - minValue)
                                        val cx = fraction * w
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.8f),
                                            radius = 2.5f.dp.toPx(),
                                            center = Offset(cx, centerY),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Reset button
                            IconButton(
                                onClick = {
                                    playbackParametersState.setPlaybackSpeed(1.0f)
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Reset speed",
                                    tint = Color(0xFF9EABB8),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }

                        // Bottom Row: Preset Buttons (0.25x, 1.0x, 2.0x, 3.0x, 4.0x)
                        val presets = listOf(0.25f, 1.0f, 2.0f, 3.0f, 4.0f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            presets.forEach { preset ->
                                val isSelected = abs(playbackParametersState.speed - preset) < 0.03f
                                val presetInteraction = remember { MutableInteractionSource() }
                                val presetShape = RoundedCornerShape(14.dp)
                                val presetModifier = if (useGlass) {
                                    Modifier
                                        .appGlassSurface(
                                            shape = presetShape,
                                            clear = true,
                                            tint = if (isSelected) {
                                                Color(0xFF1B88F5).copy(alpha = 0.72f)
                                            } else {
                                                Color.White.copy(alpha = 0.06f)
                                            },
                                            dimmed = if (isSelected) 0.14f else 0.22f,
                                            forceDark = true,
                                            interactive = true,
                                            interactionSource = presetInteraction,
                                            fallbackBackground = if (isSelected) Color(0xFF1B88F5) else Color.Transparent,
                                        )
                                        .border(
                                            width = 0.75.dp,
                                            color = if (isSelected) {
                                                Color.White.copy(alpha = 0.30f)
                                            } else {
                                                Color.White.copy(alpha = 0.10f)
                                            },
                                            shape = presetShape,
                                        )
                                } else {
                                    Modifier
                                        .clip(presetShape)
                                        .background(if (isSelected) Color(0xFF1B88F5) else Color.Transparent)
                                }

                                Box(
                                    modifier = presetModifier
                                        .clickable(
                                            interactionSource = presetInteraction,
                                            indication = null,
                                        ) {
                                            playbackParametersState.setPlaybackSpeed(preset)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 6.dp,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${preset}x",
                                        color = if (isSelected) Color.White else Color(0xFF9EABB8),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
