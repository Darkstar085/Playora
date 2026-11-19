package com.sipun.playora.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import com.sipun.playora.feature.player.extensions.copy
import com.sipun.playora.feature.player.service.getSubtitleDelayMilliseconds
import com.sipun.playora.feature.player.service.getSubtitleSpeed
import com.sipun.playora.feature.player.service.setSubtitleDelayMilliseconds
import com.sipun.playora.feature.player.service.setSubtitleSpeed
import com.sipun.nextlib.media3ext.renderer.subtitleDelayMilliseconds
import com.sipun.nextlib.media3ext.renderer.subtitleSpeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@UnstableApi
@Composable
fun rememberSubtitleOptionsState(
    player: Player,
    onEvent: (SubtitleOptionsEvent) -> Unit = {},
): SubtitleOptionsState {
    val scope = rememberCoroutineScope()
    val subtitleOptionsState = remember {
        SubtitleOptionsState(
            player = player,
            scope = scope,
            onEvent = onEvent,
        )
    }
    LaunchedEffect(key1 = player) { subtitleOptionsState.observe() }
    return subtitleOptionsState
}

@Stable
class SubtitleOptionsState(
    val player: Player,
    val scope: CoroutineScope,
    val onEvent: (SubtitleOptionsEvent) -> Unit = {},
) {

    var delayMilliseconds: Long by mutableLongStateOf(value = 0L)
        private set

    var speedMultiplier: Float by mutableFloatStateOf(value = 1f)
        private set

    fun setDelay(delayMillis: Long) {
        scope.launch {
            when (player) {
                is MediaController -> player.setSubtitleDelayMilliseconds(delayMillis = delayMillis)
                is ExoPlayer -> player.subtitleDelayMilliseconds = delayMillis
                else -> return@launch
            }
            updateSubtitleDelayMilliseconds()
            updateDelayMetadataAndSendEvent()
        }
    }

    fun setSpeed(speed: Float) {
        scope.launch {
            when (player) {
                is MediaController -> player.setSubtitleSpeed(speed = speed)
                is ExoPlayer -> player.subtitleSpeed = speed
                else -> return@launch
            }
            updateSubtitleSpeed()
            updateSpeedMetadataAndSendEvent()
        }
    }

    suspend fun observe() {
        updateSubtitleDelayMilliseconds()
        updateSubtitleSpeed()
        player.listen { events ->
            if (events.containsAny(
                    Player.EVENT_TRACKS_CHANGED,
                    Player.EVENT_CUES,
                )
            ) {
                scope.launch {
                    updateSubtitleDelayMilliseconds()
                    updateSubtitleSpeed()
                }
            }
        }
    }

    private suspend fun updateSubtitleDelayMilliseconds() {
        delayMilliseconds = when (player) {
            is MediaController -> player.getSubtitleDelayMilliseconds()
            is ExoPlayer -> player.subtitleDelayMilliseconds
            else -> return
        }
    }

    private suspend fun updateSubtitleSpeed() {
        speedMultiplier = when (player) {
            is MediaController -> player.getSubtitleSpeed()
            is ExoPlayer -> player.subtitleSpeed
            else -> return
        }
    }

    private fun updateDelayMetadataAndSendEvent(delay: Long = this.delayMilliseconds) {
        val currentMediaItem = player.currentMediaItem ?: return
        player.replaceMediaItem(
            player.currentMediaItemIndex,
            currentMediaItem.copy(subtitleDelayMilliseconds = delay),
        )
        onEvent(
            SubtitleOptionsEvent.DelayChanged(
                mediaItem = currentMediaItem,
                delay = delay,
            ),
        )
    }

    private fun updateSpeedMetadataAndSendEvent(speed: Float = this.speedMultiplier) {
        val currentMediaItem = player.currentMediaItem ?: return
        player.replaceMediaItem(
            player.currentMediaItemIndex,
            currentMediaItem.copy(subtitleSpeed = speed),
        )
        onEvent(
            SubtitleOptionsEvent.SpeedChanged(
                mediaItem = currentMediaItem,
                speed = speed,
            ),
        )
    }
}

sealed interface SubtitleOptionsEvent {
    data class DelayChanged(val mediaItem: MediaItem, val delay: Long) : SubtitleOptionsEvent
    data class SpeedChanged(val mediaItem: MediaItem, val speed: Float) : SubtitleOptionsEvent
}

@Composable
fun rememberSubtitlePositionState(
    savedOffsetY: Float,
    onPositionSaved: (Float) -> Unit,
): SubtitlePositionState {
    val state = remember {
        SubtitlePositionState(
            savedOffsetY,
            onPositionSaved,
        )
    }
    LaunchedEffect(savedOffsetY) { state.syncSavedOffset(savedOffsetY) }
    return state
}

@Stable
class SubtitlePositionState(initialOffsetY: Float, private val onPositionSaved: (Float) -> Unit) {
    var offsetY by mutableStateOf(initialOffsetY)
        private set
    var isDragging by mutableStateOf(false)
        private set
    var isPressed by mutableStateOf(false)
        private set

    val isHighlighted: Boolean get() = isPressed || isDragging
    var containerSize by mutableStateOf(IntSize.Zero)

    var textBounds by mutableStateOf<Rect?>(null)

    val translationYPx: Float
        get() {
            val height = containerSize.height.toFloat()
            val bounds = textBounds ?: return 0f
            if (height <= 0f) return 0f
            return (offsetY * height).coerceIn(
                -bounds.top,
                (height - bounds.bottom).coerceAtLeast(-bounds.top),
            )
        }

    private var offsetBeforeDrag = initialOffsetY

    fun isOnSubtitle(position: Offset, paddingPx: Float): Boolean {
        val bounds = textBounds ?: return false
        return bounds
            .translate(
                0f,
                translationYPx,
            )
            .inflate(paddingPx)
            .contains(position)
    }

    fun onPressChange(pressed: Boolean) {
        isPressed = pressed
    }

    fun onDragStart() {
        offsetBeforeDrag = offsetY
        isDragging = true
    }

    fun onDrag(deltaYPx: Float) {
        if (containerSize.height <= 0 || textBounds == null) return
        val height = containerSize.height.toFloat()
        val bounds = textBounds ?: return
        offsetY = (translationYPx + deltaYPx).coerceIn(
            -bounds.top,
            (height - bounds.bottom).coerceAtLeast(-bounds.top),
        ) / height
    }

    fun onDragEnd() {
        isDragging = false
        onPositionSaved(offsetY)
    }

    fun onDragCancel() {
        offsetY = offsetBeforeDrag
        isDragging = false
    }

    fun syncSavedOffset(savedOffsetY: Float) {
        if (!isDragging) offsetY = savedOffsetY
    }
}
