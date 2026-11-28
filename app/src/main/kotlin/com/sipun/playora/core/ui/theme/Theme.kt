package com.sipun.playora.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.ThemeConfig
import io.github.itshedimisawi.vitro.LocalGlassDarkTheme

private fun contrastColorFor(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val LocalAccentBrush = staticCompositionLocalOf {
    Brush.horizontalGradient(
        listOf(Color(0xFF2196F3), Color(0xFF2196F3)),
    )
}

val LocalAccentColor = staticCompositionLocalOf {
    Color(0xFF2196F3)
}

@Composable
fun PlayoraTheme(
    preferences: ApplicationPreferences = ApplicationPreferences(),
    darkTheme: Boolean = when (preferences.themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
        ThemeConfig.OFF -> false
        ThemeConfig.ON -> true
    },
    highContrastDarkTheme: Boolean = preferences.useHighContrastDarkTheme,
    dynamicColor: Boolean = preferences.useDynamicColors,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = highContrastDarkTheme || darkTheme

    val baseDynamicScheme = if (dynamicColor) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else null

    val accentColor = baseDynamicScheme?.primary ?: Color(preferences.customColor)
    val accentBrush = Brush.horizontalGradient(listOf(accentColor, accentColor))
    val onAccentColor = contrastColorFor(accentColor)

    val colorScheme = when {
        isDark && highContrastDarkTheme -> (baseDynamicScheme ?: darkScheme).copy(
            primary = accentColor,
            onPrimary = onAccentColor,
            primaryContainer = accentColor.copy(alpha = 0.35f),
            onPrimaryContainer = Color.White,
            secondary = accentColor,
            onSecondary = onAccentColor,
            secondaryContainer = accentColor.copy(alpha = 0.28f),
            onSecondaryContainer = Color.White,
            tertiary = accentColor,
            onTertiary = onAccentColor,
            tertiaryContainer = accentColor.copy(alpha = 0.28f),
            onTertiaryContainer = Color.White,
            inversePrimary = accentColor,
            background = backgroundPureBlack,
            surface = surfacePureBlack,
            surfaceDim = surfaceDimPureBlack,
            surfaceBright = surfaceBrightPureBlack,
            surfaceContainerLowest = surfaceContainerLowestPureBlack,
            surfaceContainerLow = surfaceContainerLowPureBlack,
            surfaceContainer = surfaceContainerPureBlack,
            surfaceContainerHigh = surfaceContainerHighPureBlack,
            surfaceContainerHighest = surfaceContainerHighestPureBlack,
            outline = Color(0xFF3A3A3A),
        )

        dynamicColor && baseDynamicScheme != null -> baseDynamicScheme

        isDark -> darkScheme.copy(
            primary = accentColor,
            onPrimary = onAccentColor,
            primaryContainer = accentColor.copy(alpha = 0.35f),
            onPrimaryContainer = Color.White,
            secondary = accentColor,
            onSecondary = onAccentColor,
            secondaryContainer = accentColor.copy(alpha = 0.28f),
            onSecondaryContainer = Color.White,
            tertiary = accentColor,
            onTertiary = onAccentColor,
            tertiaryContainer = accentColor.copy(alpha = 0.28f),
            onTertiaryContainer = Color.White,
            inversePrimary = accentColor,
        )

        else -> lightScheme.copy(
            primary = accentColor,
            onPrimary = onAccentColor,
            primaryContainer = accentColor.copy(alpha = 0.15f),
            onPrimaryContainer = if (accentColor.luminance() > 0.65f) {
                onPrimaryContainerLight
            } else {
                accentColor
            },
            secondary = accentColor,
            onSecondary = onAccentColor,
            secondaryContainer = accentColor.copy(alpha = 0.15f),
            onSecondaryContainer = if (accentColor.luminance() > 0.65f) {
                onSecondaryContainerLight
            } else {
                accentColor
            },
            tertiary = accentColor,
            onTertiary = onAccentColor,
            tertiaryContainer = accentColor.copy(alpha = 0.15f),
            onTertiaryContainer = if (accentColor.luminance() > 0.65f) {
                onTertiaryContainerLight
            } else {
                accentColor
            },
            inversePrimary = accentColor,
        )
    }

    CompositionLocalProvider(
        LocalAccentBrush provides accentBrush,
        LocalAccentColor provides accentColor,
        LocalGlassDarkTheme provides isDark,
        LocalLiquidGlassEnabled provides preferences.useLiquidGlass,
        LocalIsHighContrastDarkTheme provides (isDark && highContrastDarkTheme),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
