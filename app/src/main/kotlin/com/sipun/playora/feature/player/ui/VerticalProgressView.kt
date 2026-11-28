package com.sipun.playora.feature.player.ui

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.feature.player.buttons.PlayerButtonBlackAlpha

private const val NORMAL_MAX_PERCENTAGE = 100

@Composable
fun VerticalProgressView(
    modifier: Modifier = Modifier,
    width: Dp = 28.dp,
    maxBarHeight: Dp = 210.dp,
    icon: ImageVector,
    @IntRange(
        from = 0,
        to = 200,
    ) value: Int,
    maxValue: Int = NORMAL_MAX_PERCENTAGE,
    boostColor: Color = Color(color = 0xFFFC6E6E),
) {
    val normalizedValue = value.coerceIn(
        minimumValue = 0,
        maximumValue = maxValue,
    )
    val fillFraction = normalizedValue.toFloat() / maxValue.toFloat()
    val isBoostActive = maxValue > NORMAL_MAX_PERCENTAGE && value > NORMAL_MAX_PERCENTAGE
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
    val contentTint = if (useGlass) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .heightIn(min = 120.dp, max = maxBarHeight)
            .appGlassSurface(
                shape = CircleShape,
                clear = true,
                dimmed = 0.28f,
                forceDark = true,
                fallbackBackground = PlayerButtonBlackAlpha,
            )
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(size = width),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = normalizedValue.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = contentTint,
                ),
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
                ),
            )
        }
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .width(width = width)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (useGlass) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(width = width)
                    .fillMaxHeight(fraction = fillFraction)
                    .background(
                        color = if (isBoostActive) {
                            boostColor
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    ),
            )
        }
        Box(
            modifier = Modifier.size(size = width),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentTint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
