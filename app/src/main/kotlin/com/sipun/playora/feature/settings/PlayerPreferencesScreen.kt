package com.sipun.playora.settings.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.common.extensions.isPipFeatureSupported
import com.sipun.playora.core.model.ControlButtonsPosition
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.core.model.Resume
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.PreferenceSlider
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.components.RadioTextButton
import com.sipun.playora.core.ui.components.ResetIconButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.preview.DayNightPreview
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.settings.composables.OptionsDialog
import com.sipun.playora.settings.extensions.name
import com.sipun.playora.settings.screens.player.PlayerPreferencesUiEvent.UpdatePreferredControlButtonsPosition
import com.sipun.playora.settings.utils.LocalesHelper

@Composable
fun PlayerPreferencesScreen(viewModel: PlayerPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PlayerPreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun PlayerPreferencesScreenContent(
    state: PlayerPreferencesUiState,
    onAction: (PlayerPreferencesUiEvent) -> Unit,
) {
    val languages = remember {
        listOf(
            element = Pair(
                first = "None",
                second = "",
            ),
        ) + LocalesHelper.getAvailableLocales()
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.player_name),
                navigationIcon = {
                    AppBackButton(onClick = { onAction(PlayerPreferencesUiEvent.NavigateUp) })
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
                    title = stringResource(id = R.string.resume),
                    description = stringResource(id = R.string.resume_description),
                    icon = AppIcons.Resume,
                    onClick = {
                        onAction(
                            PlayerPreferencesUiEvent.ShowDialog(
                                value = PlayerPreferenceDialog.ResumeDialog,
                            ),
                        )
                    },
                    isFirstItem = true,
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.autoplay_settings),
                    description = stringResource(
                        id = R.string.autoplay_settings_description,
                    ),
                    icon = AppIcons.Player,
                    isChecked = state.preferences.autoplay,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoplay) },
                )
                if (LocalContext.current.isPipFeatureSupported) {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.pip_settings),
                        description = stringResource(
                            id = R.string.pip_settings_description,
                        ),
                        icon = AppIcons.Pip,
                        isChecked = state.preferences.autoPip,
                        onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoPip) },
                    )
                }
                PreferenceSwitch(
                    title = stringResource(id = R.string.background_play),
                    description = stringResource(
                        id = R.string.background_play_description,
                    ),
                    icon = AppIcons.Headset,
                    isChecked = state.preferences.autoBackgroundPlay,
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleAutoBackgroundPlay) },
                    isLastItem = true,
                )
            }

            ListSectionTitle(text = stringResource(id = R.string.audio))
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
                            PlayerPreferencesUiEvent.ShowDialog(
                                value = PlayerPreferenceDialog.AudioLanguageDialog,
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
                    onClick = { onAction(PlayerPreferencesUiEvent.ToggleVolumeBoost) },
                    isLastItem = true,
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                PlayerPreferenceDialog.ResumeDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.resume),
                        onDismissClick = {
                            onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(Resume.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = (it == state.preferences.resume),
                                onClick = {
                                    onAction(
                                        PlayerPreferencesUiEvent.UpdatePlaybackResume(resume = it),
                                    )
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }

                PlayerPreferenceDialog.AudioLanguageDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.preferred_audio_lang),
                        onDismissClick = {
                            onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(items = languages) {
                            RadioTextButton(
                                text = it.first,
                                selected = it.second == state.preferences.preferredAudioLanguage,
                                onClick = {
                                    onAction(
                                        PlayerPreferencesUiEvent.UpdateAudioLanguage(
                                            it.second,
                                        ),
                                    )
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }

                PlayerPreferenceDialog.ControlButtonsDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.control_buttons_alignment),
                        onDismissClick = {
                            onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(ControlButtonsPosition.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = it == state.preferences.controlButtonsPosition,
                                onClick = {
                                    onAction(
                                        UpdatePreferredControlButtonsPosition(
                                            value = it,
                                        ),
                                    )
                                    onAction(PlayerPreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@DayNightPreview
@Composable
private fun PlayerPreferencesScreenPreview() {
    PlayoraTheme {
        PlayerPreferencesScreenContent(
            state = PlayerPreferencesUiState(),
            onAction = {},
        )
    }
}
