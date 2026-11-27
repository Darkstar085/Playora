package com.sipun.playora.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassSlider
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import kotlin.math.roundToInt

@Composable
internal fun PreferenceIconBox(
    icon: ImageVector,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(
                brush = if (!enabled) {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                } else if (isDestructive) {
                    Brush.horizontalGradient(
                        listOf(DestructiveRed, DestructiveRed),
                    )
                } else {
                    LocalAccentBrush.current
                },
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    trailingContent: @Composable () -> Unit = {},
) {
    AppSegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        enableGlassCard = true,
        leadingContent = icon?.let {
            {
                PreferenceIconBox(icon = icon, enabled = enabled, isDestructive = isDestructive)
            }
        },
        supportingContent = description?.let { { Text(text = description) } },
        content = {
            Text(
                text = title,
                color = if (isDestructive) DestructiveRed else Color.Unspecified,
            )
        },
        trailingContent = trailingContent,
    )
}

@Composable
fun SelectablePreference(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
) {
    AppSegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        enableGlassCard = true,
        content = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium.copy(
                    textDecoration = if (selected) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                ),
            )
        },
        supportingContent = {
            description?.let {
                Text(
                    text = it,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (selected) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                    ),
                )
            }
        },
        trailingContent = {
            Checkbox(
                modifier = Modifier.semantics { contentDescription = title },
                checked = selected,
                onCheckedChange = null,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreferenceItemPreview() {
    PreferenceItem(
        title = "Title",
        description = "Description of the preference item goes here.",
        icon = AppIcons.DoubleTap,
        enabled = true,
    )
}

@Preview(showBackground = true)
@Composable
fun SelectablePreferencePreview() {
    SelectablePreference(
        title = "Title",
        description = "Description of the preference item goes here.",
    )
}

@Composable
fun ClickablePreferenceItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
) {
    PreferenceItem(
        title = title,
        description = description,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        isDestructive = isDestructive,
        onClick = onClick,
        onLongClick = onLongClick,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
    )
}

@Preview
@Composable
private fun ClickablePreferenceItemPreview() {
    ClickablePreferenceItem(
        title = "Title",
        description = "Description of the preference item goes here.",
        icon = AppIcons.DoubleTap,
        onClick = {},
        enabled = false,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceSlider(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    onReset: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {},
) {
    val span = valueRange.endInclusive - valueRange.start
    val keyStep = if (span <= 5f) 0.1f else 1f

    fun nudged(direction: Int): Float {
        val raw = (value + direction * keyStep).coerceIn(
            valueRange.start,
            valueRange.endInclusive,
        )
        return if (keyStep < 1f) (raw * 10).roundToInt() / 10f else raw.roundToInt().toFloat()
    }

    AppSegmentedListItem(
        modifier = modifier.onPreviewKeyEvent { event ->
            if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            when (event.key) {
                Key.DirectionLeft -> {
                    onValueChange(nudged(direction = -1))
                    onValueChangeFinished()
                    true
                }

                Key.DirectionRight -> {
                    onValueChange(nudged(direction = 1))
                    onValueChangeFinished()
                    true
                }

                Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                    onReset?.let {
                        it()
                        true
                    } ?: false
                }

                else -> false
            }
        },
        onClick = {},
        onLongClick = null,
        enabled = enabled,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        leadingContent = icon?.let {
            {
                PreferenceIconBox(icon = icon, enabled = enabled)
            }
        },
        supportingContent = {
            Column {
                description?.let { Text(text = description) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppGlassSlider(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        valueRange = valueRange,
                        steps = steps,
                        onValueChangeFinished = onValueChangeFinished,
                    )
                    trailingContent()
                }
            }
        },
        content = { Text(text = title) },
        trailingContent = null,
    )
}

@Composable
fun PreferenceSwitch(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    onClick: (() -> Unit) = {},
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
) {
    PreferenceItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = icon,
        enabled = enabled,
        onClick = onClick,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        trailingContent = {
            AppSwitch(
                checked = isChecked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
    )
}

@Preview
@Composable
fun PreferenceSwitchPreview() {
    PreferenceSwitch(
        title = "Title",
        description = "Description of the preference item goes here.",
        icon = AppIcons.DoubleTap,
        onClick = {},
    )
}

@Composable
fun PreferenceSwitchWithDivider(
    title: String = "",
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    onClick: (() -> Unit) = {},
    onChecked: () -> Unit = {},
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
) {
    PreferenceItem(
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (enabled &&
                event.type == KeyEventType.KeyDown &&
                (event.key == Key.DirectionLeft || event.key == Key.DirectionRight)
            ) {
                onChecked()
                true
            } else {
                false
            }
        },
        title = title,
        description = description,
        icon = icon,
        onClick = onClick,
        enabled = enabled,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VerticalDivider(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .height(40.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
                AppSwitch(
                    checked = isChecked,
                    onCheckedChange = { onChecked() },
                    enabled = enabled,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun PreferenceSwitchWithDividerPreview() {
    PreferenceSwitchWithDivider(
        title = "Title",
        description = "Description of the preference items goes here.",
        icon = AppIcons.DoubleTap,
        onClick = {},
        onChecked = {},
    )
}
