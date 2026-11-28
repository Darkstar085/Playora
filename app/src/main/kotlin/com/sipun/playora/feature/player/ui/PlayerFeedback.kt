package com.sipun.playora.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.feature.player.extensions.formatted
import com.sipun.playora.feature.player.state.TapGestureState
import com.sipun.playora.feature.player.state.VerticalGesture
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import com.sipun.playora.R as coreUiR

@Composable
internal fun InfoPill(
    info: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.appGlassSurface(
            shape = CircleShape,
            clear = true,
            dimmed = 0.35f,
            forceDark = true,
            fallbackBackground = Color.Black.copy(alpha = 0.65f),
        ),
    ) {
        Text(
            text = info,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
internal fun SeekFeedbackPill(
    seekAmountMs: Long,
    seekAmountFormatted: String,
    seekToPositionFormatted: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.appGlassSurface(
            shape = CircleShape,
            clear = true,
            dimmed = 0.35f,
            forceDark = true,
            fallbackBackground = Color.Black.copy(alpha = 0.65f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.FastForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(degrees = if (seekAmountMs < 0L) 180f else 0f),
            )
            Text(
                text = seekAmountFormatted,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = seekToPositionFormatted,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
internal fun DpadSeekIndicator(
    visible: Boolean,
    offsetMs: Long,
    positionMs: Long,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val sign = if (offsetMs >= 0L) "+" else "-"
        SeekFeedbackPill(
            seekAmountMs = offsetMs,
            seekAmountFormatted = "$sign${abs(offsetMs).milliseconds.inWholeSeconds}s",
            seekToPositionFormatted = positionMs.milliseconds.formatted(),
        )
    }
}

@Composable
internal fun BoxScope.PlayerGestureIndicators(tapGestureState: TapGestureState) {
    DoubleTapIndicator(tapGestureState = tapGestureState)
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
    val topSafePadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues().calculateTopPadding()

    AnimatedVisibility(
        modifier = Modifier
            .padding(top = topSafePadding + 16.dp)
            .align(Alignment.TopCenter),
        visible = tapGestureState.isLongPressGestureInAction,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier.appGlassSurface(
                shape = CircleShape,
                clear = true,
                dimmed = 0.30f,
                forceDark = true,
                fallbackBackground = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            ) {
                Text(
                    text = stringResource(
                        coreUiR.string.fast_playback_speed,
                        tapGestureState.longPressSpeed,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (useGlass) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.PlayerVerticalGestureIndicators(
    activeGesture: VerticalGesture?,
    volumePercentage: Int,
    maxVolumePercentage: Int,
    brightnessPercentage: Int,
) {
    val safeInsets = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = max(safeInsets.calculateStartPadding(layoutDirection), 16.dp),
        top = max(safeInsets.calculateTopPadding(), 16.dp),
        end = max(safeInsets.calculateEndPadding(layoutDirection), 16.dp),
        bottom = max(safeInsets.calculateBottomPadding(), 16.dp),
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        val adaptiveMaxHeight = (maxHeight * 0.44f).coerceIn(136.dp, 210.dp)

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterStart),
            visible = activeGesture == VerticalGesture.VOLUME,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            VerticalProgressView(
                value = volumePercentage,
                maxValue = maxVolumePercentage,
                maxBarHeight = adaptiveMaxHeight,
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = activeGesture == VerticalGesture.BRIGHTNESS,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            VerticalProgressView(
                value = brightnessPercentage,
                maxBarHeight = adaptiveMaxHeight,
                icon = Icons.Rounded.BrightnessMedium,
            )
        }
    }
}
