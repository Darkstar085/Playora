package com.sipun.playora.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getStringForTime
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval

private const val MILLISECONDS_PER_SECOND = 1_000L

@OptIn(UnstableApi::class)
@Composable
fun ControlsMiniIndicatorsView(
    progressState: ProgressStateWithTickInterval,
    modifier: Modifier = Modifier,
) {
    val timeTextPositionMs by remember(progressState) {
        derivedStateOf {
            val wholeSeconds = progressState.currentPositionMs / MILLISECONDS_PER_SECOND
            wholeSeconds * MILLISECONDS_PER_SECOND
        }
    }

    val currentFormatted = getStringForTime(timeTextPositionMs)
    val remainingFormatted =
        if (progressState.durationMs > 0 && progressState.durationMs != C.TIME_UNSET) {
            val remainingMs = (progressState.durationMs - timeTextPositionMs).coerceAtLeast(0)
            "-" + getStringForTime(remainingMs)
        } else {
            null
        }

    val playbackTimeText = if (remainingFormatted != null) {
        "$currentFormatted / $remainingFormatted"
    } else {
        currentFormatted
    }

    val status = rememberBatteryAndClockStatus(uppercaseAmPm = false)
    val batteryText = if (status.isCharging) {
        "⚡ ${status.batteryPercentage}%"
    } else {
        "${status.batteryPercentage}%"
    }
    val statusText = if (status.currentTime.isNotEmpty()) {
        "$batteryText  ${status.currentTime}"
    } else {
        batteryText
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topEnd = 4.dp),
        ) {
            Text(
                text = playbackTimeText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 4.dp),
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}
