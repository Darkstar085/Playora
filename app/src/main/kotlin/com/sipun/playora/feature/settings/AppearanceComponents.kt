package com.sipun.playora.settings.screens.appearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.rememberGlassCardBorderBrush
import com.sipun.playora.core.ui.theme.rememberGlassCardTint

import com.sipun.playora.core.ui.components.PreferenceSectionCard

@Composable
internal fun AppearanceSectionCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    PreferenceSectionCard(content = content)
}

@Composable
internal fun AppearanceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        trailing?.invoke()
    }
}

@Composable
internal fun ThemeSelectionDialog(
    themeConfig: ThemeConfig,
    useHighContrastDarkTheme: Boolean,
    onSelect: (ThemeConfig, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_choose_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ThemeOptionCard(
                    title = stringResource(R.string.theme_device_default),
                    icon = Icons.Default.BrightnessAuto,
                    description = stringResource(R.string.theme_device_default_desc),
                    selected = !useHighContrastDarkTheme && themeConfig == ThemeConfig.SYSTEM,
                    onClick = {
                        onSelect(ThemeConfig.SYSTEM, false)
                        onDismiss()
                    },
                )
                ThemeOptionCard(
                    title = stringResource(R.string.theme_light),
                    icon = Icons.Default.LightMode,
                    description = stringResource(R.string.theme_light_desc),
                    selected = !useHighContrastDarkTheme && themeConfig == ThemeConfig.OFF,
                    onClick = {
                        onSelect(ThemeConfig.OFF, false)
                        onDismiss()
                    },
                )
                ThemeOptionCard(
                    title = stringResource(R.string.theme_dark),
                    icon = Icons.Default.DarkMode,
                    description = stringResource(R.string.theme_dark_desc),
                    selected = !useHighContrastDarkTheme && themeConfig == ThemeConfig.ON,
                    onClick = {
                        onSelect(ThemeConfig.ON, false)
                        onDismiss()
                    },
                )
                ThemeOptionCard(
                    title = stringResource(R.string.theme_pure_black),
                    icon = Icons.Default.DarkMode,
                    description = stringResource(R.string.theme_pure_black_desc),
                    selected = useHighContrastDarkTheme,
                    onClick = {
                        onSelect(ThemeConfig.ON, true)
                        onDismiss()
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
internal fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        },
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            if (selected) {
                Surface(
                    modifier = Modifier.size(26.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomColorSelector(
    color: Color,
    onColorChange: (Int) -> Unit,
) {
    val presetColors = listOf(
        Color(0xFF2196F3),
        Color(0xFFE92B2B),
        Color(0xFFE57373),
        Color(0xFF26A69A),
        Color(0xFF66BB6A),
        Color(0xFF9CCC65),
        Color(0xFFFFB74D),
        Color(0xFFFF7043),
        Color(0xFFAB47BC),
        Color(0xFF7E57C2),
        Color(0xFF5C6BC0),
        Color(0xFF26C6DA),
        Color(0xFF8BC34A),
        Color(0xFFFFA726),
    )

    val hsv = remember(color) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }
    }
    var hue by remember(color) { mutableFloatStateOf(hsv[0]) }

    val hueColors = remember {
        (0..360 step 30).map { Color.hsv(it.toFloat(), 0.78f, 0.92f) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.custom_colour),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.custom_colour_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            presetColors.forEach { preset ->
                val isSelected = color.toArgb() == preset.toArgb()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(preset)
                        .clickable {
                            val presetHsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(preset.toArgb(), presetHsv)
                            hue = presetHsv[0]
                            onColorChange(preset.toArgb())
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(hueColors)),
            )
            val sliderState = rememberSliderState(value = hue, trackRange = 0f..360f)
            SideEffect { sliderState.value = hue }
            Slider(
                state = sliderState,
                onValueChange = {
                    hue = it
                    onColorChange(
                        android.graphics.Color.HSVToColor(floatArrayOf(it, 0.78f, 0.92f)),
                    )
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                thumb = {
                    if (LocalLiquidGlassEnabled.current) {
                        val thumbInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(32.dp)
                                .appGlassSurface(
                                    shape = CircleShape,
                                    clear = true,
                                    tint = Color.White.copy(alpha = 0.65f),
                                    interactive = true,
                                    interactionSource = thumbInteraction,
                                    fallbackBackground = Color.White,
                                    fallbackBorder = BorderStroke(
                                        0.75.dp,
                                        Color.Black.copy(alpha = 0.25f),
                                    ),
                                )
                                .border(
                                    0.75.dp,
                                    Color.White.copy(alpha = 0.80f),
                                    CircleShape,
                                ),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(34.dp)
                                .background(Color.White, RoundedCornerShape(50))
                                .border(
                                    0.5.dp,
                                    Color.Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(50),
                                ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
