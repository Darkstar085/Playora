package com.sipun.playora.core.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.itshedimisawi.vitro.Glass
import io.github.itshedimisawi.vitro.GlassAppearance
import io.github.itshedimisawi.vitro.GlassBackdrop
import io.github.itshedimisawi.vitro.GlassEffectState
import io.github.itshedimisawi.vitro.LocalGlassDarkTheme
import io.github.itshedimisawi.vitro.ScrollEdgeEffectStyle
import io.github.itshedimisawi.vitro.glassBackdrop
import io.github.itshedimisawi.vitro.glassEffect
import io.github.itshedimisawi.vitro.glassEffectContainer
import io.github.itshedimisawi.vitro.scrollEdgeEffect
import kotlin.math.abs
import kotlinx.coroutines.launch

val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }
val LocalGlassBackdrop = compositionLocalOf<GlassBackdrop?> { null }
val LocalInsideGlassBackdrop = staticCompositionLocalOf { false }
val LocalIsHighContrastDarkTheme = staticCompositionLocalOf { false }

@Composable
fun rememberAppGlass(
    tint: Color = Color.Unspecified,
    interactive: Boolean = false,
    clear: Boolean = false,
    dimmed: Float = 0f,
    forceDark: Boolean = false,
    blurRadius: Dp? = null,
): Glass {
    val enabled = LocalLiquidGlassEnabled.current
    val highContrastDark = LocalIsHighContrastDarkTheme.current
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    return remember(
        enabled,
        highContrastDark,
        isDark,
        tint,
        interactive,
        clear,
        dimmed,
        forceDark,
        blurRadius,
    ) {
        if (!enabled) {
            Glass.Identity
        } else {
            val base = if (clear) Glass.Clear else Glass.Regular
            val effectiveBlur = blurRadius ?: if (clear) 8.dp else 12.dp
            var glass = base.copy(
                blurRadius = effectiveBlur,
                refractionHeight = if (clear) 12.dp else 14.dp,
                refractionAmount = if (clear) 22.dp else 28.dp,
                dispersion = 0.03f,
                saturation = if (clear) 1.18f else 1.24f,
                brightness = when {
                    forceDark || isDark -> if (clear) 0.02f else -0.02f
                    clear -> 0.04f
                    tint.isSpecified -> 0.06f
                    else -> 0.14f
                },
                highlightIntensity = when {
                    forceDark || isDark -> if (highContrastDark) 1.35f else 1.20f
                    else -> 1.15f
                },
            )
            if (forceDark) {
                glass = glass.copy(appearance = GlassAppearance.Dark)
            }
            if (tint.isSpecified) {
                glass = glass.tint(tint)
            } else if (!isDark && !forceDark && !clear) {
                glass = glass.tint(Color.White.copy(alpha = 0.36f))
            } else if ((isDark || forceDark) && !clear) {
                glass = glass.tint(
                    if (highContrastDark) {
                        Color(0x4012171E)
                    } else {
                        Color.White.copy(alpha = 0.08f)
                    },
                )
            }
            if (dimmed > 0f) {
                glass = glass.dimmed(dimmed)
            }
            if (interactive) {
                glass = glass.interactive()
            }
            if (!isDark && !forceDark) {
                glass = glass.copy(
                    shadowRadius = if (tint.isSpecified) 10.dp else 8.dp,
                    shadowOffset = if (tint.isSpecified) 3.dp else 2.dp,
                    shadowOpacity = if (tint.isSpecified) 0.09f else 0.055f,
                )
            }
            glass
        }
    }
}

@Composable
fun Modifier.appGlassSurface(
    shape: CornerBasedShape = CircleShape,
    tint: Color = Color.Unspecified,
    interactive: Boolean = false,
    clear: Boolean = false,
    dimmed: Float = 0f,
    forceDark: Boolean = false,
    blurRadius: Dp? = null,
    state: GlassEffectState? = null,
    interactionSource: InteractionSource? = null,
    fallbackBackground: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    fallbackBorder: BorderStroke? = null,
): Modifier {
    val enabled = LocalLiquidGlassEnabled.current
    val backdrop = LocalGlassBackdrop.current
    val insideBackdrop = LocalInsideGlassBackdrop.current
    return if (enabled && backdrop != null && !insideBackdrop) {
        this.glassEffect(
            backdrop = backdrop,
            glass = rememberAppGlass(
                tint = tint,
                interactive = interactive,
                clear = clear,
                dimmed = dimmed,
                forceDark = forceDark,
                blurRadius = blurRadius,
            ),
            shape = shape,
            state = state,
            interactionSource = interactionSource,
        )
    } else {
        this
            .clip(shape)
            .background(fallbackBackground, shape)
            .let { mod ->
                if (fallbackBorder != null) mod.border(fallbackBorder, shape) else mod
            }
    }
}

fun Modifier.appGlassBackdrop(
    backdrop: GlassBackdrop?,
    enabled: Boolean,
): Modifier = if (enabled && (backdrop != null)) this.glassBackdrop(backdrop) else this

@Composable
fun AppGlassBackdropBox(
    backdrop: GlassBackdrop?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    pageColor: Color = MaterialTheme.colorScheme.background,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    enableScrollEdgeEffect: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .appGlassBackdrop(backdrop, enabled = enabled)
            .background(pageColor)
            .let { mod ->
                if (enabled && enableScrollEdgeEffect) {
                    mod.appScrollEdgeEffect(
                        topPadding = topPadding,
                        bottomPadding = bottomPadding,
                        pageColor = pageColor,
                        enabled = true,
                    )
                } else {
                    mod
                }
            },
    ) {
        CompositionLocalProvider(LocalInsideGlassBackdrop provides true) {
            content()
        }
    }
}

fun Modifier.appGlassContainer(
    enabled: Boolean,
): Modifier = if (enabled) this.glassEffectContainer() else this

@Composable
fun Modifier.appScrollEdgeEffect(
    topPadding: Dp,
    bottomPadding: Dp,
    pageColor: Color = MaterialTheme.colorScheme.background,
    enabled: Boolean = LocalLiquidGlassEnabled.current,
): Modifier {
    if (!enabled) return this
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val effectiveTop = if (statusTop > 0.dp) statusTop else topPadding
    val effectiveBottom = if (navBottom > 0.dp) navBottom else bottomPadding
    return this.scrollEdgeEffect(
        top = effectiveTop,
        bottom = effectiveBottom,
        style = if (isDark) ScrollEdgeEffectStyle.Automatic else ScrollEdgeEffectStyle.Soft,
        color = pageColor,
    )
}

@Composable
fun rememberGlassContentColor(
    state: GlassEffectState?,
    fallback: Color = MaterialTheme.colorScheme.onSurface,
): Color {
    val enabled = LocalLiquidGlassEnabled.current
    val backdrop = LocalGlassBackdrop.current
    val insideBackdrop = LocalInsideGlassBackdrop.current
    return if (enabled && backdrop != null && !insideBackdrop && state != null && state.contentColor.isSpecified) {
        state.contentColor
    } else {
        fallback
    }
}

@Composable
fun isAppGlassActive(): Boolean =
    LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null

@Composable
fun rememberGlassOverlayTint(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    val highContrastDark = LocalIsHighContrastDarkTheme.current
    return when {
        isDark && highContrastDark -> Color(0x9E12161E)
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.54f)
        else -> Color.White.copy(alpha = 0.48f)
    }
}

@Composable
fun rememberGlassOverlayBorderColor(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    return if (isDark) {
        Color.White.copy(alpha = 0.16f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
}

@Composable
fun rememberGlassInnerContainerColor(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    return if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.055f)
    }
}

@Composable
fun rememberGlassInnerBorderColor(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    return if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)
    }
}

@Composable
fun AppGlassModalOverlay(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = RoundedCornerShape(28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    border: BorderStroke? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: @Composable () -> Unit,
) {
    val useGlass = isAppGlassActive()
    if (useGlass) {
        val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
        val progress = remember { Animatable(0f) }
        val noOpInteraction = remember { MutableInteractionSource() }
        val glassTint = rememberGlassOverlayTint()
        val glassBorder = border ?: BorderStroke(0.85.dp, rememberGlassOverlayBorderColor())

        LaunchedEffect(Unit) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 440f,
                ),
            )
        }

        BackHandler(enabled = dismissOnBackPress) {
            onDismissRequest()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = (if (isDark) 0.44f else 0.22f) * progress.value),
                    )
                    .clickable(
                        interactionSource = noOpInteraction,
                        indication = null,
                        onClick = {
                            if (dismissOnClickOutside) {
                                onDismissRequest()
                            }
                        },
                    ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .then(modifier)
                    .graphicsLayer {
                        alpha = progress.value
                        val scale = 0.94f + (0.06f * progress.value)
                        scaleX = scale
                        scaleY = scale
                    }
                    .appGlassSurface(
                        shape = shape,
                        tint = glassTint,
                        blurRadius = 18.dp,
                        fallbackBackground = containerColor,
                        fallbackBorder = glassBorder,
                    )
                    .border(glassBorder, shape)
                    .clickable(
                        interactionSource = noOpInteraction,
                        indication = null,
                        onClick = {},
                    ),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    LocalInsideGlassBackdrop provides true,
                ) {
                    content()
                }
            }
        }
    } else {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Surface(
                modifier = modifier,
                shape = shape,
                color = containerColor,
                border = border,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGlassBottomSheetOverlay(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    val useGlass = isAppGlassActive()
    if (useGlass) {
        val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
        val progress = remember { Animatable(0f) }
        val dragOffset = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val dismissThresholdPx = with(density) { 96.dp.toPx() }
        val noOpInteraction = remember { MutableInteractionSource() }
        val glassTint = rememberGlassOverlayTint()
        val glassBorder = BorderStroke(0.85.dp, rememberGlassOverlayBorderColor())
        val sheetShape = RoundedCornerShape(30.dp)

        LaunchedEffect(Unit) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = 380f,
                ),
            )
        }

        BackHandler(onBack = onDismissRequest)

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = (if (isDark) 0.44f else 0.22f) * progress.value),
                    )
                    .clickable(
                        interactionSource = noOpInteraction,
                        indication = null,
                        onClick = onDismissRequest,
                    ),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .then(modifier)
                    .graphicsLayer {
                        alpha = progress.value
                        translationY = ((1f - progress.value) * 160f) + dragOffset.value
                    }
                    .appGlassSurface(
                        shape = sheetShape,
                        tint = glassTint,
                        blurRadius = 18.dp,
                        fallbackBackground = containerColor,
                        fallbackBorder = glassBorder,
                    )
                    .border(glassBorder, sheetShape)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f))
                            }
                        },
                        onDragStopped = { velocity ->
                            if (dragOffset.value > dismissThresholdPx || velocity > 1200f) {
                                onDismissRequest()
                            } else {
                                scope.launch {
                                    dragOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = 420f,
                                        ),
                                    )
                                }
                            }
                        },
                    )
                    .clickable(
                        interactionSource = noOpInteraction,
                        indication = null,
                        onClick = {},
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f)),
                )
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    LocalInsideGlassBackdrop provides true,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content,
                    )
                }
            }
        }
    } else {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        )
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = containerColor,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                )
            },
            content = content,
        )
    }
}

@Composable
fun AppDarkGlassSheet(
    shape: CornerBasedShape,
    modifier: Modifier = Modifier,
    fallbackColor: Color = Color(0xF012171E),
    fallbackBorder: BorderStroke? = BorderStroke(1.dp, Color(0xFF26323D)),
    shadowElevation: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
    val noOpInteraction = remember { MutableInteractionSource() }
    if (useGlass) {
        Box(
            modifier = modifier
                .appGlassSurface(
                    shape = shape,
                    tint = Color(0xA812171E),
                    clear = true,
                    dimmed = 0.22f,
                    forceDark = true,
                    blurRadius = 18.dp,
                    fallbackBackground = fallbackColor,
                    fallbackBorder = fallbackBorder,
                )
                .border(
                    width = 0.85.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.24f),
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f),
                        ),
                    ),
                    shape = shape,
                )
                .clickable(
                    interactionSource = noOpInteraction,
                    indication = null,
                    onClick = {},
                ),
        ) {
            CompositionLocalProvider(LocalInsideGlassBackdrop provides true) {
                content()
            }
        }
    } else {
        Surface(
            shape = shape,
            color = fallbackColor,
            border = fallbackBorder,
            shadowElevation = shadowElevation,
            modifier = modifier.clickable(
                interactionSource = noOpInteraction,
                indication = null,
                onClick = {},
            ),
            content = content,
        )
    }
}

@Composable
fun rememberGlassCardTint(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    val highContrastDark = LocalIsHighContrastDarkTheme.current
    return when {
        isDark && highContrastDark -> Color(0xFF131820).copy(alpha = 0.90f)
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
        else -> Color.White.copy(alpha = 0.86f)
    }
}

@Composable
fun rememberGlassCardBorderColor(): Color {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    val highContrastDark = LocalIsHighContrastDarkTheme.current
    return when {
        isDark && highContrastDark -> Color.White.copy(alpha = 0.16f)
        isDark -> Color.White.copy(alpha = 0.13f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
    }
}

@Composable
fun rememberGlassCardBorderBrush(): Brush {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    val highContrastDark = LocalIsHighContrastDarkTheme.current
    val outline = MaterialTheme.colorScheme.outlineVariant
    return remember(isDark, highContrastDark, outline) {
        when {
            isDark && highContrastDark -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.11f),
                    Color.White.copy(alpha = 0.05f),
                ),
                start = Offset.Zero,
                end = Offset(600f, 600f),
            )
            isDark -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.04f),
                ),
                start = Offset.Zero,
                end = Offset(600f, 600f),
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f),
                    outline.copy(alpha = 0.28f),
                    outline.copy(alpha = 0.16f),
                ),
                start = Offset.Zero,
                end = Offset(600f, 600f),
            )
        }
    }
}

@Composable
fun rememberGlassBarBorderStroke(): BorderStroke {
    val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
    return BorderStroke(
        width = 0.75.dp,
        brush = if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.24f),
                    Color.White.copy(alpha = 0.07f),
                ),
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.90f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f),
                ),
            )
        },
    )
}

@Composable
fun AppGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: () -> Unit = {},
    forceDark: Boolean = false,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = Color.Unspecified,
) {
    val glassEnabled = LocalLiquidGlassEnabled.current
    val clampedValue = value.coerceIn(valueRange)
    val sliderState = key(valueRange, steps) {
        rememberSliderState(
            value = clampedValue,
            steps = steps,
            trackRange = valueRange,
        )
    }
    SideEffect {
        if (abs(sliderState.value - clampedValue) > 0.0001f) {
            sliderState.value = clampedValue
        }
    }

    if (glassEnabled) {
        val isDark = forceDark || (LocalGlassDarkTheme.current ?: isSystemInDarkTheme())
        val interactionSource = remember { MutableInteractionSource() }
        val isDragged by interactionSource.collectIsDraggedAsState()
        val isPressed by interactionSource.collectIsPressedAsState()
        val isActive = isDragged || isPressed
        val thumbWidth by animateDpAsState(
            targetValue = if (isActive) 30.dp else 26.dp,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
            label = "glassSliderThumbWidth",
        )
        val thumbHeight by animateDpAsState(
            targetValue = if (isActive) 21.dp else 18.dp,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
            label = "glassSliderThumbHeight",
        )
        val resolvedInactive = when {
            inactiveTrackColor.isSpecified -> inactiveTrackColor
            isDark -> Color.White.copy(alpha = if (enabled) 0.16f else 0.08f)
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.13f else 0.06f)
        }
        val trackBorderColor = if (isDark) {
            Color.White.copy(alpha = if (enabled) 0.16f else 0.08f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (enabled) 0.50f else 0.25f)
        }
        val resolvedActive = activeTrackColor.copy(alpha = if (enabled) 0.90f else 0.38f)
        val thumbFallback = if (isDark) {
            Color.White.copy(alpha = if (enabled) 0.92f else 0.45f)
        } else {
            Color.White.copy(alpha = if (enabled) 0.96f else 0.55f)
        }
        val thumbBorder = if (isDark) {
            Color.White.copy(alpha = if (enabled) 0.68f else 0.25f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (enabled) 0.45f else 0.22f)
        }
        val canUseShaderThumb = LocalGlassBackdrop.current != null && !LocalInsideGlassBackdrop.current

        Slider(
            state = sliderState,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = modifier.height(30.dp),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(width = thumbWidth, height = thumbHeight)
                        .then(
                            if (!canUseShaderThumb) {
                                Modifier.shadow(
                                    elevation = if (enabled) 3.dp else 0.dp,
                                    shape = CircleShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .appGlassSurface(
                            shape = CircleShape,
                            clear = true,
                            tint = Color.White.copy(
                                alpha = when {
                                    !enabled -> 0.42f
                                    isActive -> 0.36f
                                    else -> 0.72f
                                },
                            ),
                            forceDark = forceDark,
                            interactive = enabled,
                            interactionSource = interactionSource,
                            fallbackBackground = thumbFallback,
                            fallbackBorder = BorderStroke(0.75.dp, thumbBorder),
                        )
                        .border(0.75.dp, thumbBorder, CircleShape),
                )
            },
            track = { state ->
                val span = valueRange.endInclusive - valueRange.start
                val fraction = if (span > 0f) {
                    ((state.value - valueRange.start) / span).coerceIn(0f, 1f)
                } else {
                    0f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(resolvedInactive)
                        .border(0.5.dp, trackBorderColor, CircleShape),
                ) {
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = fraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(resolvedActive),
                        )
                    }
                }
            },
        )
    } else {
        Slider(
            state = sliderState,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier,
            enabled = enabled,
            colors = SliderDefaults.colors(),
        )
    }
}
