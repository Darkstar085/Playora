package com.sipun.playora.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.rememberGlassBarBorderStroke
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
import io.github.itshedimisawi.vitro.rememberGlassEffectState

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
    if (useGlass) {
        val interactionSource = remember { MutableInteractionSource() }
        val glassState = rememberGlassEffectState()
        val contentColor = rememberGlassContentColor(
            state = glassState,
            fallback = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = modifier
                .size(38.dp)
                .appGlassSurface(
                    shape = CircleShape,
                    interactive = true,
                    state = glassState,
                    interactionSource = interactionSource,
                )
                .border(rememberGlassBarBorderStroke(), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.ArrowBack,
                contentDescription = stringResource(id = R.string.navigate_up),
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
    } else {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier.size(36.dp),
        ) {
            Icon(
                imageVector = AppIcons.ArrowBack,
                contentDescription = stringResource(id = R.string.navigate_up),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun AppTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ),
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
    )
}

@Composable
fun AppTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    fontWeight: FontWeight? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ),
) {
    AppTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = fontWeight,
                modifier = Modifier.padding(start = 12.dp),
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        modifier = modifier,
    )
}
