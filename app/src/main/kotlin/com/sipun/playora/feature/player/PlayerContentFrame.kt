package com.sipun.playora.feature.player

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.feature.player.extensions.toContentScale
import com.sipun.playora.feature.player.state.CuesState
import com.sipun.playora.feature.player.state.PictureInPictureState
import com.sipun.playora.feature.player.state.SubtitlePositionState
import com.sipun.playora.feature.player.state.VideoZoomAndContentScaleState
import com.sipun.playora.feature.player.ui.ShutterView
import com.sipun.playora.feature.player.ui.SubtitleConfiguration
import com.sipun.playora.feature.player.ui.SubtitleView

@OptIn(UnstableApi::class)
@Composable
fun PlayerContentFrame(
    modifier: Modifier = Modifier,
    player: Player,
    pictureInPictureState: PictureInPictureState,
    videoZoomAndContentScaleState: VideoZoomAndContentScaleState,
    subtitleConfiguration: SubtitleConfiguration,
    cuesState: CuesState,
    subtitlePositionState: SubtitlePositionState,
    videoVisible: Boolean = true,
    displayAspectRatio: Float? = null,
) {
    val presentationState = rememberPresentationState(player = player)
    val useGlassSurface = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
    val videoModifier = Modifier
        .resizeWithContentScale(
            contentScale = videoZoomAndContentScaleState.videoContentScale
                .toContentScale(),
            sourceSizeDp = presentationState.videoSizeDp?.let { size ->
                size.copy(
                    width = with(LocalDensity.current) {
                        (
                                displayAspectRatio?.let {
                                    size.height * it
                                } ?: size.width
                                ).toDp().value
                    },
                    height = with(LocalDensity.current) { size.height.toDp().value },
                )
            },
        )
        .graphicsLayer {
            scaleX = videoZoomAndContentScaleState.zoom
            scaleY = videoZoomAndContentScaleState.zoom
            translationX = videoZoomAndContentScaleState.offset.x
            translationY = videoZoomAndContentScaleState.offset.y
        }
    Box(modifier.fillMaxSize()) {
        if (videoVisible) {
            PlayerSurface(
                player = player,
                surfaceType = if (useGlassSurface) {
                    SURFACE_TYPE_TEXTURE_VIEW
                } else {
                    SURFACE_TYPE_SURFACE_VIEW
                },
                modifier = videoModifier
                    .onGloballyPositioned {
                        val bounds = it.boundsInWindow()
                        val rect = Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt(),
                        )
                        pictureInPictureState.setVideoViewRect(rect)
                    },
            )
        }

        AnimatedVisibility(
            visible =
                subtitleConfiguration.controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
            )
        }
        SubtitleView(
            bitmapModifier = videoModifier,
            cuesState = cuesState,
            positionState = subtitlePositionState,
            isInPictureInPictureMode = pictureInPictureState.isInPictureInPictureMode,
            configuration = subtitleConfiguration,
        )

        if (videoVisible && presentationState.coverSurface) {
            ShutterView()
        }
    }
}
