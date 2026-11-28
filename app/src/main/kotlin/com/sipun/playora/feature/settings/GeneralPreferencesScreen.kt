package com.sipun.playora.settings.screens.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.DeleteButton
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.PreferenceSectionCard
import com.sipun.playora.core.ui.components.ResetButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import io.github.itshedimisawi.vitro.rememberGlassBackdrop

@Composable
fun GeneralPreferencesScreen(viewModel: GeneralPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GeneralPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun GeneralPreferencesScreenContent(
    state: GeneralPreferencesUiState,
    onAction: (GeneralPreferencesUiEvent) -> Unit,
) {
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
                        title = stringResource(id = R.string.general_name),
                        navigationIcon = {
                            AppBackButton(onClick = { onAction(GeneralPreferencesUiEvent.NavigateUp) })
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
                            .verticalScroll(state = rememberScrollState())
                            .padding(paddingValues = innerPadding),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        ) {
                            if (state.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                            ListSectionTitle(text = stringResource(id = R.string.media_library))
                            PreferenceSectionCard {
                                ClickablePreferenceItem(
                                    title = stringResource(id = R.string.manage_folders),
                                    description = stringResource(id = R.string.manage_folders_desc),
                                    icon = AppIcons.FolderOff,
                                    onClick = { onAction(GeneralPreferencesUiEvent.OpenFolders) },
                                    isFirstItem = true,
                                    isLastItem = true,
                                )
                            }

                            ListSectionTitle(text = stringResource(id = R.string.user_data))
                            PreferenceSectionCard {
                                ClickablePreferenceItem(
                                    title = stringResource(R.string.delete_thumbnail_cache),
                                    description = stringResource(R.string.delete_thumbnail_cache_description),
                                    icon = AppIcons.DeleteSweep,
                                    enabled = !state.isBusy,
                                    isDestructive = true,
                                    onClick = {
                                        onAction(
                                            GeneralPreferencesUiEvent.ShowDialog(
                                                value = GeneralPreferencesDialog.ClearThumbnailCacheDialog,
                                            ),
                                        )
                                    },
                                    isFirstItem = true,
                                )
                                ClickablePreferenceItem(
                                    title = stringResource(R.string.reset_settings),
                                    description = stringResource(R.string.reset_settings_description),
                                    icon = AppIcons.History,
                                    enabled = !state.isBusy,
                                    isDestructive = true,
                                    onClick = {
                                        onAction(
                                            GeneralPreferencesUiEvent.ShowDialog(
                                                value = GeneralPreferencesDialog.ResetSettingsDialog,
                                            ),
                                        )
                                    },
                                    isLastItem = true,
                                )
                            }
                        }
                    }
                }
            }

            state.showDialog?.let { dialog ->
                when (dialog) {
                    GeneralPreferencesDialog.ClearThumbnailCacheDialog -> {
                        AppDialog(
                            onDismissRequest = {
                                onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.delete_thumbnail_cache),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            confirmButton = {
                                DeleteButton(
                                    onClick = {
                                        onAction(GeneralPreferencesUiEvent.ClearThumbnailCache)
                                        onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                                    },
                                )
                            },
                            dismissButton = {
                                CancelButton(onClick = {
                                    onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                                })
                            },
                            content = {
                                Text(
                                    text = stringResource(R.string.delete_thumbnail_cache_confirmation),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            },
                        )
                    }

                    GeneralPreferencesDialog.ResetSettingsDialog -> {
                        AppDialog(
                            onDismissRequest = {
                                onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.reset_settings),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            },
                            confirmButton = {
                                ResetButton(
                                    onClick = {
                                        onAction(GeneralPreferencesUiEvent.ResetSettings)
                                        onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                                    },
                                )
                            },
                            dismissButton = {
                                CancelButton(onClick = {
                                    onAction(GeneralPreferencesUiEvent.ShowDialog(value = null))
                                })
                            },
                            content = {
                                Text(
                                    text = stringResource(R.string.reset_settings_confirmation),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
