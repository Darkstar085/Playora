package com.sipun.playora.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalInsideGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import io.github.itshedimisawi.vitro.LocalGlassDarkTheme

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedIcon: ImageVector = AppIcons.Check,
) {
    if (LocalLiquidGlassEnabled.current) {
        val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
        val canUseShaderThumb = LocalGlassBackdrop.current != null && !LocalInsideGlassBackdrop.current
        val interactionSource = remember { MutableInteractionSource() }

        val trackWidth = 52.dp
        val trackHeight = 28.dp
        val thumbWidth = 28.dp
        val thumbHeight = 22.dp
        val thumbInset = 3.dp
        val maxOffset = trackWidth - thumbWidth - (thumbInset * 2)

        val thumbOffset by animateDpAsState(
            targetValue = if (checked) thumbInset + maxOffset else thumbInset,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = 480f,
            ),
            label = "glassSwitchOffset",
        )
        val trackColor by animateColorAsState(
            targetValue = when {
                checked && enabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                checked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
                isDark && enabled -> Color.White.copy(alpha = 0.14f)
                isDark -> Color.White.copy(alpha = 0.08f)
                enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f)
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            },
            label = "glassSwitchTrackColor",
        )
        val trackBorderColor by animateColorAsState(
            targetValue = when {
                checked && enabled && isDark -> Color.White.copy(alpha = 0.28f)
                checked && enabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                checked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                isDark -> Color.White.copy(alpha = if (enabled) 0.20f else 0.10f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (enabled) 0.55f else 0.25f)
            },
            label = "glassSwitchTrackBorder",
        )
        val thumbBorderColor = if (isDark) {
            Color.White.copy(alpha = if (enabled) 0.70f else 0.28f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (enabled) 0.40f else 0.20f)
        }
        val thumbFallbackColor = Color.White.copy(alpha = if (enabled) 0.96f else 0.52f)

        Box(
            modifier = modifier
                .size(width = trackWidth, height = trackHeight)
                .clip(CircleShape)
                .background(trackColor, CircleShape)
                .border(0.75.dp, trackBorderColor, CircleShape)
                .then(
                    if (onCheckedChange != null) {
                        Modifier.toggleable(
                            value = checked,
                            enabled = enabled,
                            role = Role.Switch,
                            interactionSource = interactionSource,
                            indication = null,
                            onValueChange = onCheckedChange,
                        )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                    .size(width = thumbWidth, height = thumbHeight)
                    .then(
                        if (!canUseShaderThumb) {
                            Modifier.shadow(
                                elevation = if (enabled) 2.5.dp else 0.dp,
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .appGlassSurface(
                        shape = CircleShape,
                        clear = true,
                        tint = Color.White.copy(alpha = if (enabled) 0.84f else 0.46f),
                        interactive = enabled && onCheckedChange != null,
                        interactionSource = interactionSource,
                        fallbackBackground = thumbFallbackColor,
                    )
                    .border(0.75.dp, thumbBorderColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(
                        imageVector = checkedIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (enabled) 1f else 0.45f,
                        ),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    } else {
        val thumbContent: (@Composable () -> Unit)? = if (checked) {
            {
                Icon(
                    imageVector = checkedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            thumbContent = thumbContent,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedIconColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

