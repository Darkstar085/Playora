package com.sipun.playora.feature.videopicker.composables.privacy

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.isAppGlassActive
import com.sipun.playora.core.ui.theme.rememberGlassCardBorderBrush
import com.sipun.playora.core.ui.theme.rememberGlassCardBorderColor
import com.sipun.playora.core.ui.theme.rememberGlassCardTint
import com.sipun.playora.feature.videopicker.privacy.PRIVACY_PIN_LENGTH

@Composable
fun PinDotsIndicator(filledCount: Int, error: Boolean, modifier: Modifier = Modifier) {
    val offsetX = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(key1 = error) {
        if (error) {
            for (target in listOf(
                16f,
                -16f,
                10f,
                -10f,
                0f,
            )) {
                offsetX.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 50),
                )
            }
        }
    }

    Row(
        modifier = modifier.offset { IntOffset(x = offsetX.value.dp.roundToPx(), y = 0) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(times = PRIVACY_PIN_LENGTH) { index ->
            val filled = index < filledCount
            Spacer(
                modifier = Modifier
                    .size(14.dp)
                    .clip(shape = CircleShape)
                    .background(
                        color = if (error) {
                            MaterialTheme.colorScheme.error
                        } else if (filled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                    ),
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .widthIn(max = 320.dp)
            .alpha(if (enabled) 1f else 0.45f),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val rows = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                row.forEach { digit ->
                    KeyButton(
                        modifier = Modifier.weight(weight = 1f),
                        enabled = enabled,
                        onClick = { onDigit(digit) },
                    ) {
                        Text(
                            text = digit.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.weight(weight = 1f))
            KeyButton(
                modifier = Modifier.weight(weight = 1f),
                enabled = enabled,
                onClick = { onDigit('0') },
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            KeyButton(
                modifier = Modifier.weight(weight = 1f),
                enabled = enabled,
                onClick = onBackspace,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val shape = CircleShape
    if (isAppGlassActive()) {
        val interactionSource = remember { MutableInteractionSource() }
        val borderBrush = rememberGlassCardBorderBrush()
        val fallbackBorderColor = rememberGlassCardBorderColor()
        Column(
            modifier = modifier
                .aspectRatio(ratio = 1f)
                .appGlassSurface(
                    shape = shape,
                    tint = rememberGlassCardTint(),
                    interactive = enabled,
                    interactionSource = interactionSource,
                    fallbackBackground = MaterialTheme.colorScheme.surfaceContainerHigh,
                    fallbackBorder = BorderStroke(0.85.dp, fallbackBorderColor),
                )
                .border(0.85.dp, borderBrush, shape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            content()
        }
    } else {
        Column(
            modifier = modifier
                .aspectRatio(ratio = 1f)
                .clip(shape = shape)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = shape,
                )
                .clickable(enabled = enabled, onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            content()
        }
    }
}
