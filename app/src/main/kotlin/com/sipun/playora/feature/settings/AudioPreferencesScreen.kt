package com.sipun.playora.settings.screens.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.components.RadioTextButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.settings.composables.LanguageSelectionBottomSheet
import com.sipun.playora.settings.composables.OptionsDialog
import com.sipun.playora.settings.utils.LocalesHelper

@Composable
fun AudioPreferencesScreen(viewModel: AudioPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AudioPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun AudioPreferencesScreenContent(
    state: AudioPreferencesUiState,
    onAction: (AudioPreferencesUiEvent) -> Unit,
) {
    val languages = remember {
        listOf(
            element = Pair(
                first = "None",
                second = "",
            ),
        ) +
                LocalesHelper.getAvailableLocales()
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.audio),
                navigationIcon = {
                    AppBackButton(onClick = { onAction(AudioPreferencesUiEvent.NavigateUp) })
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
            ListSectionTitle(text = stringResource(id = R.string.playback))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ClickablePreferenceItem(
                    title = stringResource(id = R.string.preferred_audio_lang),
                    description =
                        LocalesHelper.getLocaleDisplayLanguage(
                            state.preferences.preferredAudioLanguage,
                        )
                            .takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.preferred_audio_lang_description),
                    icon = AppIcons.Language,
                    onClick = {
                        onAction(
                            AudioPreferencesUiEvent.ShowDialog(
                                value = AudioPreferenceDialog.AudioLanguageDialog,
                            ),
                        )
                    },
                    isFirstItem = true,
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.volume_boost),
                    description = stringResource(id = R.string.volume_boost_desc),
                    icon = AppIcons.VolumeUp,
                    isChecked = state.preferences.enableVolumeBoost,
                    onClick = { onAction(AudioPreferencesUiEvent.ToggleVolumeBoost) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                AudioPreferenceDialog.AudioLanguageDialog -> {
                    LanguageSelectionBottomSheet(
                        title = stringResource(id = R.string.preferred_audio_lang),
                        subtitle = stringResource(id = R.string.preferred_audio_lang_desc),
                        languages = languages,
                        selectedLanguage = state.preferences.preferredAudioLanguage,
                        onApply = {
                            onAction(AudioPreferencesUiEvent.UpdateAudioLanguage(it))
                            onAction(AudioPreferencesUiEvent.ShowDialog(value = null))
                        },
                        onDismiss = {
                            onAction(AudioPreferencesUiEvent.ShowDialog(value = null))
                        },
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AudioPreferencesScreenPreview() {
    PlayoraTheme {
        AudioPreferencesScreenContent(
            state = AudioPreferencesUiState(),
            onAction = {},
        )
    }
}
