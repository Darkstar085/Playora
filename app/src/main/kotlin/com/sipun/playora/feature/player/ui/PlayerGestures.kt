package com.sipun.playora.feature.player.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sipun.playora.feature.player.extensions.detectCustomHorizontalDragGestures
import com.sipun.playora.feature.player.extensions.detectCustomTransformGestures
import com.sipun.playora.feature.player.extensions.detectCustomVerticalDragGestures
import com.sipun.playora.feature.player.extensions.detectSubtitleDragGestures
import com.sipun.playora.feature.player.state.ControlsVisibilityState
import com.sipun.playora.feature.player.state.PictureInPictureState
import com.sipun.playora.feature.player.state.SeekGestureState
import com.sipun.playora.feature.player.state.SubtitlePositionState
import com.sipun.playora.feature.player.state.TapGestureState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.state.VolumeAndBrightnessGestureState

private val SUBTITLE_TOUCH_PADDING = 16.dp

@Composable
fun PlayerGestures(
    modifier: Modifier = Modifier,
    controlsVisibilityState: ControlsVisibilityState,
    tapGestureState: TapGestureState,
    pictureInPictureState: PictureInPictureState,
    seekGestureState: SeekGestureState,
    videoZoomAndContentScaleState: VideoZoomAndContentScaleState,
    volumeAndBrightnessGestureState: VolumeAndBrightnessGestureState,
    subtitlePositionState: SubtitlePositionState,
    hasSubtitles: Boolean,
    canRepositionSubtitles: Boolean,
) {

    val currentHasSubtitles by rememberUpdatedState(hasSubtitles)
    val currentCanReposition by rememberUpdatedState(canRepositionSubtitles)

    val gestureInsets = WindowInsets.systemGestures.union(WindowInsets.systemBarsIgnoringVisibility)
    val density = LocalDensity.current
    val subtitleTouchPaddingPx = with(density) { SUBTITLE_TOUCH_PADDING.toPx() }

    fun isOnSubtitle(position: Offset) = currentCanReposition && currentHasSubtitles &&
            subtitlePositionState.isOnSubtitle(
                position,
                subtitleTouchPaddingPx,
            )

    val layoutDirection = LocalLayoutDirection.current
    BoxWithConstraints {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { subtitlePositionState.containerSize = it }
                .pointerInput(
                    canRepositionSubtitles,
                    controlsVisibilityState.controlsLocked,
                    pictureInPictureState.isInPictureInPictureMode,
                ) {
                    if (controlsVisibilityState.controlsLocked) return@pointerInput
                    if (pictureInPictureState.isInPictureInPictureMode) return@pointerInput

                    detectSubtitleDragGestures(
                        shouldStartGesture = ::isOnSubtitle,
                        onPressChange = subtitlePositionState::onPressChange,
                        onDragStart = subtitlePositionState::onDragStart,
                        onDrag = { subtitlePositionState.onDrag(it.y) },
                        onDragEnd = subtitlePositionState::onDragEnd,
                        onDragCancel = subtitlePositionState::onDragCancel,
                    )
                }
                .pointerInput(pictureInPictureState.isInPictureInPictureMode) {
                    if (pictureInPictureState.isInPictureInPictureMode) return@pointerInput

                    detectTapGestures(
                        onTap = {
                            if (tapGestureState.seekMillis != 0L) return@detectTapGestures
                            controlsVisibilityState.toggleControlsVisibility()
                        },
                        onDoubleTap = {
                            if (controlsVisibilityState.controlsLocked) return@detectTapGestures
                            tapGestureState.handleDoubleTap(
                                offset = it,
                                size = size,
                            )
                        },
                        onPress = {
                            tryAwaitRelease()
                            tapGestureState.handleOnLongPressRelease()
                        },
                        onLongPress = {
                            if (controlsVisibilityState.controlsLocked) return@detectTapGestures
                            if (isOnSubtitle(it)) return@detectTapGestures
                            tapGestureState.handleLongPress()
                        },
                    )
                }
                .pointerInput(
                    seekGestureState,
                    gestureInsets,
                    density,
                    layoutDirection,
                    controlsVisibilityState.controlsLocked,
                    pictureInPictureState.isInPictureInPictureMode,
                ) {
                    if (controlsVisibilityState.controlsLocked) return@pointerInput
                    if (pictureInPictureState.isInPictureInPictureMode) return@pointerInput

                    detectCustomHorizontalDragGestures(
                        shouldStartGesture = {
                            gestureInsets.isPlayerGestureStart(
                                it,
                                size,
                                density,
                                layoutDirection,
                            )
                        },
                        onDragStart = seekGestureState::onDragStart,
                        onHorizontalDrag = seekGestureState::onDrag,
                        onDragCancel = seekGestureState::onDragEnd,
                        onDragEnd = seekGestureState::onDragEnd,
                    )
                }
                .pointerInput(
                    gestureInsets,
                    density,
                    layoutDirection,
                    controlsVisibilityState.controlsLocked,
                    pictureInPictureState.isInPictureInPictureMode,
                ) {
                    if (controlsVisibilityState.controlsLocked) return@pointerInput
                    if (pictureInPictureState.isInPictureInPictureMode) return@pointerInput

                    detectCustomVerticalDragGestures(
                        shouldStartGesture = {
                            gestureInsets.isPlayerGestureStart(
                                it,
                                size,
                                density,
                                layoutDirection,
                            )
                        },
                        onDragStart = {
                            volumeAndBrightnessGestureState.onDragStart(
                                offset = it,
                                size = size,
                            )
                        },
                        onVerticalDrag = volumeAndBrightnessGestureState::onDrag,
                        onDragCancel = volumeAndBrightnessGestureState::onDragEnd,
                        onDragEnd = volumeAndBrightnessGestureState::onDragEnd,
                    )
                }
                .pointerInput(
                    videoZoomAndContentScaleState,
                    controlsVisibilityState.controlsLocked,
                    pictureInPictureState.isInPictureInPictureMode,
                ) {
                    if (controlsVisibilityState.controlsLocked) return@pointerInput
                    if (pictureInPictureState.isInPictureInPictureMode) return@pointerInput

                    detectCustomTransformGestures(
                        onGesture = { _, panChange, zoomChange, _ ->
                            if (tapGestureState.isLongPressGestureInAction) {
                                return@detectCustomTransformGestures
                            }
                            videoZoomAndContentScaleState.onZoomPanGesture(
                                constraints = this@BoxWithConstraints.constraints,
                                panChange = panChange,
                                zoomChange = zoomChange,
                            )
                        },
                        onGestureEnd = { videoZoomAndContentScaleState.onZoomPanGestureEnd() },
                    )
                },
        )
    }
}

internal fun WindowInsets.isPlayerGestureStart(
    position: Offset,
    size: IntSize,
    density: Density,
    layoutDirection: LayoutDirection,
): Boolean = position.x >= getLeft(
    density,
    layoutDirection,
) &&
        position.x < size.width - getRight(
    density,
    layoutDirection,
) &&
        position.y >= getTop(density) &&
        position.y < size.height - getBottom(density)
