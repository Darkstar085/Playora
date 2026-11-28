package com.sipun.playora.settings.screens.appearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppSwitch
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import io.github.itshedimisawi.vitro.rememberGlassBackdrop

@Composable
fun AppearancePreferencesScreen(viewModel: AppearancePreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppearancePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun AppearancePreferencesScreenContent(
    state: AppearancePreferencesUiState,
    onAction: (AppearancePreferencesEvent) -> Unit,
) {
    val preferences = state.preferences
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when {
        preferences.useHighContrastDarkTheme -> stringResource(R.string.theme_pure_black)
        preferences.themeConfig == ThemeConfig.SYSTEM -> stringResource(R.string.theme_device_default)
        preferences.themeConfig == ThemeConfig.OFF -> stringResource(R.string.theme_light)
        preferences.themeConfig == ThemeConfig.ON -> stringResource(R.string.theme_dark)
        else -> stringResource(R.string.theme_device_default)
    }

    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val backdrop = rememberGlassBackdrop()
    val containerColor = if (liquidGlassEnabled) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) backdrop else null,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    AppTopAppBar(
                        title = stringResource(id = R.string.appearance_name),
                        navigationIcon = {
                            AppBackButton(onClick = { onAction(AppearancePreferencesEvent.NavigateUp) })
                        },
                    )
                },
                containerColor = containerColor,
            ) { innerPadding ->
                AppGlassBackdropBox(
                    backdrop = backdrop,
                    enabled = liquidGlassEnabled,
                    pageColor = containerColor,
                    topPadding = innerPadding.calculateTopPadding(),
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState()),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
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
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        shape = CircleShape,
                                        border = if (liquidGlassEnabled) {
                                            BorderStroke(
                                                0.75.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                            )
                                        } else {
                                            null
                                        },
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
                                    AppSwitch(
                                        checked = preferences.useDynamicColors,
                                        onCheckedChange = {
                                            onAction(
                                                AppearancePreferencesEvent.SetDynamicColor(
                                                    it,
                                                ),
                                            )
                                        },
                                    )
                                },
                            )

                            if (!preferences.useDynamicColors) {
                                CustomColorSelector(
                                    color = Color(preferences.customColor),
                                    onColorChange = { onAction(AppearancePreferencesEvent.SetCustomColor(it)) },
                                )
                            }

                            AppearanceRow(
                                icon = Icons.Default.AutoAwesome,
                                title = stringResource(R.string.liquid_glass),
                                subtitle = stringResource(R.string.liquid_glass_desc),
                                trailing = {
                                    AppSwitch(
                                        checked = preferences.useLiquidGlass,
                                        onCheckedChange = {
                                            onAction(
                                                AppearancePreferencesEvent.SetLiquidGlass(it),
                                            )
                                        },
                                    )
                                },
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
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
}
