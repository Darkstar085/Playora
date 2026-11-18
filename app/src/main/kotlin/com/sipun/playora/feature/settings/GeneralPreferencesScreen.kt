package com.sipun.playora.settings.screens.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.DeleteButton
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ResetButton
import com.sipun.playora.core.ui.designsystem.AppIcons

@Composable
fun GeneralPreferencesScreen(viewModel: GeneralPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GeneralPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun GeneralPreferencesScreenContent(
    state: GeneralPreferencesUiState,
    onAction: (GeneralPreferencesUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.general_name),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = {
                            onAction(GeneralPreferencesUiEvent.NavigateUp)
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
            if (state.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            ListSectionTitle(text = stringResource(id = R.string.user_data))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
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
