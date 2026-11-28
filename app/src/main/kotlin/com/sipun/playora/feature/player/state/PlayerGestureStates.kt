package com.sipun.playora.feature.player.state

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.core.model.DoubleTapGesture
import com.sipun.playora.feature.player.extensions.formatted
import com.sipun.playora.feature.player.extensions.isPortrait
import com.sipun.playora.feature.player.extensions.setIsScrubbingModeEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@UnstableApi
@Composable
fun PlayerOrientationEffect(
    player: Player,
    orientationOverride: Int? = null,
) {
    val activity = LocalActivity.current ?: return
    LaunchedEffect(
        activity,
        player,
        orientationOverride,
    ) {
        if (orientationOverride != null) {
            activity.requestedOrientation = if (orientationOverride == 99999) {
                activity.videoBasedOrientation(player)
            } else {
                orientationOverride
            }
        } else if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.requestedOrientation = activity.videoBasedOrientation(player = player)
        }
        player.listen { events ->
            if ((orientationOverride == 99999 || orientationOverride == null) &&
                events.contains(Player.EVENT_VIDEO_SIZE_CHANGED)
            ) {
                activity.requestedOrientation = activity.videoBasedOrientation(player)
            }
        }
    }
}

@UnstableApi
private fun Activity.videoBasedOrientation(player: Player) = when {
    player.videoSize.width == 0 || player.videoSize.height == 0 -> requestedOrientation
    player.videoSize.isPortrait -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

@UnstableApi
@Composable
fun rememberSeekGestureState(
    player: Player,
    sensitivity: Float = 0.5f,
    enableSeekGesture: Boolean,
): SeekGestureState {
    val seekGestureState = remember(
        player,
        sensitivity,
        enableSeekGesture,
    ) {
        SeekGestureState(
            player = player,
            sensitivity = sensitivity,
            enableSeekGesture = enableSeekGesture,
        )
    }
    DisposableEffect(key1 = seekGestureState) { onDispose { seekGestureState.onSeekEnd() } }
    return seekGestureState
}

@Stable
class SeekGestureState(
    private val player: Player,
    private val enableSeekGesture: Boolean = true,
    private val sensitivity: Float = 0.5f,
) {
    var isSeeking: Boolean by mutableStateOf(value = false)
        private set

    var seekStartPosition: Long? by mutableStateOf(value = null)
        private set

    var seekAmount: Long? by mutableStateOf(value = null)
        private set

    private var pendingSeekPosition: Long? by mutableStateOf(value = null)

    val seekPreviewPosition: Long?
        get() = pendingSeekPosition ?: if (isSeeking) {
            seekStartPosition?.plus(seekAmount ?: 0L)
        } else {
            null
        }

    private var seekStartX = 0f

    fun onSeek(value: Long) {
        val duration = player.duration
        if (duration <= 0 || player.currentPosition < 0) {
            reset()
            return
        }
        if (!isSeeking) {
            pendingSeekPosition = null
            isSeeking = true
            seekStartPosition = player.currentPosition
            player.setIsScrubbingModeEnabled(enabled = true)
        }

        val position = value.coerceIn(
            minimumValue = 0L,
            maximumValue = duration,
        )
        pendingSeekPosition = position
        seekAmount = position - seekStartPosition!!
    }

    fun onSeekEnd() {
        val targetPosition = pendingSeekPosition
        if (targetPosition != null) player.seekTo(targetPosition)
        reset(keepSeekPreview = targetPosition != null)
    }

    fun onPlaybackPositionChanged(position: Long) {
        if (isSeeking) return
        val targetPosition = pendingSeekPosition ?: return
        if (abs(position - targetPosition) <= 1_000L) {
            pendingSeekPosition = null
        }
    }

    fun onDragStart(offset: Offset) {
        if (!enableSeekGesture) return
        if (player.currentPosition < 0) return
        if (player.duration <= 0) return
        if (!player.isCurrentMediaItemSeekable) return

        pendingSeekPosition = null
        isSeeking = true
        seekStartX = offset.x
        seekStartPosition = player.currentPosition

        player.setIsScrubbingModeEnabled(enabled = true)
    }

    @OptIn(UnstableApi::class)
    fun onDrag(change: PointerInputChange, dragAmount: Float) {
        if (seekStartPosition == null) return
        val duration = player.duration
        if (duration <= 0 || player.currentPosition < 0) {
            reset()
            return
        }
        if (!player.isCurrentMediaItemSeekable) return
        if (player.currentPosition <= 0L && dragAmount < 0) return
        if (player.currentPosition >= duration && dragAmount > 0) return

        val newPosition =
            seekStartPosition!! + ((change.position.x - seekStartX) * (sensitivity * 100)).toInt()
        val position = newPosition.coerceIn(
            minimumValue = 0L,
            maximumValue = duration,
        )
        seekAmount = position - seekStartPosition!!
        player.seekTo(position)
    }

    fun onDragEnd() {
        reset()
    }

    private fun reset(keepSeekPreview: Boolean = false) {
        if (isSeeking) player.setIsScrubbingModeEnabled(enabled = false)
        if (!keepSeekPreview) pendingSeekPosition = null
        isSeeking = false
        seekStartPosition = null
        seekAmount = null

        seekStartX = 0f
    }
}

val SeekGestureState.seekAmountFormatted: String
    get() {
        val seekAmount = seekAmount ?: return ""
        val sign = if (seekAmount < 0) "-" else "+"
        return sign + abs(n = seekAmount).milliseconds.formatted()
    }

val SeekGestureState.seekToPositionFormated: String
    get() {
        val position = seekStartPosition ?: return ""
        val seekAmount = seekAmount ?: return ""
        return (position + seekAmount).milliseconds.formatted()
    }

@UnstableApi
@Composable
fun rememberTapGestureState(
    player: Player,
    doubleTapGesture: DoubleTapGesture,
    seekIncrementMillis: Long,
    useLongPressGesture: Boolean,
    longPressSpeed: Float,
): TapGestureState {
    val coroutineScope = rememberCoroutineScope()
    val tapGestureState = remember(
        player,
        doubleTapGesture,
        seekIncrementMillis,
        useLongPressGesture,
        longPressSpeed,
    ) {
        TapGestureState(
            player = player,
            doubleTapGesture = doubleTapGesture,
            seekIncrementMillis = seekIncrementMillis,
            useLongPressGesture = useLongPressGesture,
            longPressSpeed = longPressSpeed,
            coroutineScope = coroutineScope,
        )
    }
    return tapGestureState
}

@Stable
class TapGestureState(
    private val player: Player,
    private val seekIncrementMillis: Long,
    private val useLongPressGesture: Boolean = true,
    private val coroutineScope: CoroutineScope,
    val longPressSpeed: Float = 2.0f,
    val doubleTapGesture: DoubleTapGesture,
    val interactionSource: MutableInteractionSource = MutableInteractionSource(),
) {
    var seekMillis by mutableLongStateOf(value = 0L)
    var isLongPressGestureInAction by mutableStateOf(value = false)

    private var resetJob: Job? = null
    private var currentSpeed: Float = player.playbackParameters.speed

    fun handleDoubleTap(offset: Offset, size: IntSize) {
        if (!player.isCurrentMediaItemSeekable) return

        val action = when (doubleTapGesture) {
            DoubleTapGesture.FAST_FORWARD_AND_REWIND -> {
                val viewCenterX = size.width / 2
                when {
                    offset.x < viewCenterX -> DoubleTapAction.SEEK_BACKWARD
                    else -> DoubleTapAction.SEEK_FORWARD
                }
            }

            DoubleTapGesture.BOTH -> {
                val eventPositionX = offset.x / size.width
                when {
                    eventPositionX < 0.35 -> DoubleTapAction.SEEK_BACKWARD
                    eventPositionX > 0.65 -> DoubleTapAction.SEEK_FORWARD
                    else -> DoubleTapAction.PLAY_PAUSE
                }
            }

            DoubleTapGesture.PLAY_PAUSE -> DoubleTapAction.PLAY_PAUSE

            DoubleTapGesture.NONE -> return
        }

        when (action) {
            DoubleTapAction.SEEK_BACKWARD -> {
                player.seekTo(player.currentPosition - seekIncrementMillis)
                if (seekMillis > 0L) {
                    seekMillis = 0L
                }
                seekMillis -= seekIncrementMillis
                interactionSource.tryEmit(PressInteraction.Press(pressPosition = offset))
            }

            DoubleTapAction.SEEK_FORWARD -> {
                player.seekTo(player.currentPosition + seekIncrementMillis)
                if (seekMillis < 0L) {
                    seekMillis = 0L
                }
                seekMillis += seekIncrementMillis
                interactionSource.tryEmit(PressInteraction.Press(pressPosition = offset))
            }

            DoubleTapAction.PLAY_PAUSE -> {
                when (player.isPlaying) {
                    true -> player.pause()
                    false -> player.play()
                }
            }
        }
        resetDoubleTapSeekState()
    }

    fun handleLongPress() {
        if (!useLongPressGesture) return
        if (!player.isPlaying) return

        isLongPressGestureInAction = true
        currentSpeed = player.playbackParameters.speed
        player.setPlaybackSpeed(longPressSpeed)
    }

    fun handleOnLongPressRelease() {
        if (isLongPressGestureInAction) {
            isLongPressGestureInAction = false
            player.setPlaybackSpeed(currentSpeed)
        }
    }

    private fun resetDoubleTapSeekState() {
        resetJob?.cancel()
        resetJob = coroutineScope.launch {
            delay(750.milliseconds)
            seekMillis = 0L
        }
    }
}

enum class DoubleTapAction {
    SEEK_BACKWARD,
    SEEK_FORWARD,
    PLAY_PAUSE,
}
