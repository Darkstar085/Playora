package com.sipun.playora.settings.screens.medialibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.ui.base.DataState
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.SelectablePreference
import com.sipun.playora.core.ui.extensions.plus
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.PlayoraTheme
import io.github.itshedimisawi.vitro.rememberGlassBackdrop

@Composable
fun FolderPreferencesScreen(viewModel: FolderPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED,
    )

    FolderPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun FolderPreferencesScreenContent(
    state: FolderPreferencesUiState,
    onAction: (FolderPreferencesUiEvent) -> Unit,
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
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = stringResource(id = R.string.manage_folders),
                    navigationIcon = {
                        AppBackButton(onClick = { onAction(FolderPreferencesUiEvent.NavigateUp) })
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
                when (state.foldersDataState) {
                    is DataState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues = innerPadding),
                        ) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                    }

                    is DataState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = innerPadding + PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                        ) {
                            itemsIndexed(state.foldersDataState.value) { index, folder ->
                                SelectablePreference(
                                    title = folder.name,
                                    description = folder.path,
                                    selected = folder.path in state.preferences.excludeFolders,
                                    onClick = {
                                        onAction(FolderPreferencesUiEvent.UpdateExcludeList(folder.path))
                                    },
                                    isFirstItem = index == 0,
                                    isLastItem = index == state.foldersDataState.value.lastIndex,
                                )
                            }
                        }
                    }

                    is DataState.Error -> Unit
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FolderPreferencesScreenPreview() {
    PlayoraTheme {
        FolderPreferencesScreenContent(
            state = FolderPreferencesUiState(),
            onAction = {},
        )
    }
}
