package com.sipun.playora.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface

@Composable
fun BoxScope.OverlayView(
    modifier: Modifier = Modifier,
    show: Boolean,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val endPadding = WindowInsets.safeDrawing
        .asPaddingValues()
        .calculateEndPadding(layoutDirection = layoutDirection)
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null

    AnimatedVisibility(
        modifier = Modifier.align(
            alignment = if (configuration.isPortrait) {
                Alignment.BottomCenter
            } else {
                Alignment.CenterEnd
            },
        ),
        visible = show,
        enter = if (configuration.isPortrait) {
            slideInVertically { it }
        } else {
            slideInHorizontally { it }
        },
        exit = if (configuration.isPortrait) {
            slideOutVertically { it }
        } else {
            slideOutHorizontally { it }
        },
    ) {
        val panelSizeModifier = modifier.then(
            other = if (configuration.isPortrait) {
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.50f)
            } else {
                Modifier
                    .widthIn(min = 320.dp, max = 440.dp)
                    .fillMaxWidth(fraction = 0.45f)
                    .fillMaxHeight()
            },
        )
        val panelBody: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .padding(end = endPadding),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.size(8.dp))
                content()
            }
        }
        if (useGlass) {
            Box(
                modifier = panelSizeModifier.appGlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    tint = Color(0xBD12171E),
                    clear = true,
                    dimmed = 0.24f,
                    forceDark = true,
                    fallbackBackground = MaterialTheme.colorScheme.surface,
                ),
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    panelBody()
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = panelSizeModifier,
            ) {
                panelBody()
            }
        }
    }
}
