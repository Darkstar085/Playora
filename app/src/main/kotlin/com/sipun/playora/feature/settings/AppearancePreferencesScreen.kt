package com.sipun.playora.settings.screens.appearance

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.core.ui.theme.readWallpaperSeed

@Composable
fun AppearancePreferencesScreen(viewModel: AppearancePreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearancePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun AppearancePreferencesScreenContent(
    state: AppearancePreferencesUiState,
    onAction: (AppearancePreferencesEvent) -> Unit,
) {
    val preferences = state.preferences
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when {
        preferences.useHighContrastDarkTheme -> stringResource(R.string.theme_pure_black)
        preferences.themeConfig == ThemeConfig.SYSTEM -> stringResource(R.string.theme_device_default)
        preferences.themeConfig == ThemeConfig.OFF -> stringResource(R.string.theme_light)
        preferences.themeConfig == ThemeConfig.ON -> stringResource(R.string.theme_dark)
        else -> stringResource(R.string.theme_device_default)
    }

    val isDark = preferences.useHighContrastDarkTheme || when (preferences.themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
        ThemeConfig.OFF -> false
        ThemeConfig.ON -> true
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.appearance_name),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = {
                            onAction(AppearancePreferencesEvent.NavigateUp)
                        },
                    ) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ListSectionTitle(text = stringResource(id = R.string.appearance_name))

            AppearanceSectionCard {
                AppearanceRow(
                    icon = Icons.Default.LightMode,
                    title = stringResource(R.string.theme),
                    subtitle = themeLabel,
                    onClick = { showThemeDialog = true },
                    trailing = {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = themeLabel,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    },
                )

                AppearanceRow(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.dynamic_color),
                    subtitle = stringResource(R.string.dynamic_color_desc),
                    trailing = {
                        Switch(
                            checked = preferences.useDynamicColors,
                            onCheckedChange = {
                                onAction(
                                    AppearancePreferencesEvent.SetDynamicColor(
                                        it
                                    )
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = if (isDark) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    Color.White
                                },
                                uncheckedTrackColor = if (isDark) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color(0xFFE3E3E8)
                                },
                                uncheckedBorderColor = if (isDark) {
                                    MaterialTheme.colorScheme.outline
                                } else {
                                    Color(0xFFC1C1C8)
                                },
                            ),
                        )
                    },
                )

                if (preferences.useDynamicColors) {
                    PaletteSelector(
                        seed = readWallpaperSeed(context),
                        darkTheme = isDark,
                        selected = preferences.dynamicPalette,
                        onSelect = { onAction(AppearancePreferencesEvent.SetDynamicPalette(it)) },
                    )
                } else {
                    CustomColorSelector(
                        color = Color(preferences.customColor),
                        onColorChange = { onAction(AppearancePreferencesEvent.SetCustomColor(it)) },
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                themeConfig = preferences.themeConfig,
                useHighContrastDarkTheme = preferences.useHighContrastDarkTheme,
                onSelect = { config, pureBlack ->
                    onAction(AppearancePreferencesEvent.SetTheme(config, pureBlack))
                },
                onDismiss = { showThemeDialog = false },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AppearancePreferencesScreenPreview() {
    PlayoraTheme {
        AppearancePreferencesScreenContent(
            state = AppearancePreferencesUiState(
                preferences = ApplicationPreferences(),
            ),
            onAction = {},
        )
    }
}
