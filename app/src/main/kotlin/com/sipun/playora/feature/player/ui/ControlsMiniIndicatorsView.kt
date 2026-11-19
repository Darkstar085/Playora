package com.sipun.playora.feature.player.ui.controls

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getStringForTime
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val remainingFormatted = if (progressState.durationMs > 0 && progressState.durationMs != C.TIME_UNSET) {
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

    // Battery and clock state
    val context = LocalContext.current
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    val timeFormatter = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault())
    }

    LaunchedEffect(Unit) {
        while (true) {
            val batteryIntent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            )
            batteryIntent?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL ||
                        plugged > 0
                if (level >= 0 && scale > 0) {
                    batteryPercentage = (level * 100) / scale
                }
            }
            currentTime = timeFormatter.format(Date()).lowercase(Locale.getDefault())
            delay(10_000)
        }
    }

    val batteryText = if (isCharging) "⚡ $batteryPercentage%" else "$batteryPercentage%"
    val statusText = if (currentTime.isNotEmpty()) "$batteryText  $currentTime" else batteryText

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Bottom-left: Playback position & remaining time (flush to corner)
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

        // Bottom-right: Battery percentage & Current clock time (flush to corner)
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
