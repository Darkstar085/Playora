package com.sipun.playora.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.rememberGlassCardBorderBrush
import com.sipun.playora.core.ui.theme.rememberGlassCardTint

@Composable
fun AppSegmentedListItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    enableGlassCard: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    colors: ListItemColors = ListItemDefaults.segmentedColors(),
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val overrideShape = MaterialTheme.shapes.large

    val focusInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by focusInteractionSource.collectIsFocusedAsState()
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        label = "focusScale",
    )

    val resolvedShapes = remember(
        isFirstItem,
        isLastItem,
        shapes,
        overrideShape,
    ) {
        val defaultBaseShape = shapes.shape
        if (defaultBaseShape is CornerBasedShape) {
            shapes.copy(
                shape = defaultBaseShape.copy(
                    topStart =
                        overrideShape.topStart.takeIf { isFirstItem }
                            ?: defaultBaseShape.topStart,
                    topEnd =
                        overrideShape.topEnd.takeIf { isFirstItem } ?: defaultBaseShape.topEnd,
                    bottomStart =
                        overrideShape.bottomStart.takeIf { isLastItem }
                            ?: defaultBaseShape.bottomStart,
                    bottomEnd =
                        overrideShape.bottomEnd.takeIf { isLastItem }
                            ?: defaultBaseShape.bottomEnd,
                ),
            )
        } else {
            shapes
        }
    }

    val useGlassCard = enableGlassCard &&
        LocalLiquidGlassEnabled.current &&
        !selected &&
        colors.containerColor != Color.Transparent &&
        (resolvedShapes.shape is CornerBasedShape)

    val glassShape = resolvedShapes.shape as? CornerBasedShape
    val cardTint = rememberGlassCardTint()
    val cardBorderBrush = rememberGlassCardBorderBrush()

    val effectiveColors = if (useGlassCard) {
        colors.copy(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        )
    } else {
        colors
    }

    SegmentedListItem(
        modifier = modifier
            .zIndex(zIndex = if (isFocused) 1f else 0f)
            .scale(scale = focusScale)
            .then(
                if (useGlassCard && glassShape != null) {
                    val fillColor = if (enabled) cardTint else cardTint.copy(alpha = cardTint.alpha * 0.65f)
                    Modifier
                        .clip(glassShape)
                        .background(fillColor, glassShape)
                        .border(0.85.dp, cardBorderBrush, glassShape)
                } else {
                    Modifier
                },
            )
            .then(
                other = if (isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = overrideShape,
                    )
                } else {
                    Modifier
                },
            ),
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
        enabled = enabled,
        verticalAlignment = Alignment.CenterVertically,
        shapes = resolvedShapes,
        colors = effectiveColors,
        contentPadding = contentPadding,
        leadingContent = leadingContent,
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        overlineContent = overlineContent,
        interactionSource = focusInteractionSource,
        content = content,
    )
}

@Composable
fun ListSectionTitle(
    modifier: Modifier = Modifier,
    text: String,
    contentPadding: PaddingValues = PaddingValues(
        start = 12.dp,
        top = 20.dp,
        bottom = 10.dp,
    ),
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier.padding(paddingValues = contentPadding),
        color = color,
        style = MaterialTheme.typography.labelLarge,
    )
}
