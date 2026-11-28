package com.sipun.playora.settings.screens.subtitle

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.PreferenceSectionCard
import com.sipun.playora.core.ui.components.PreferenceSlider
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.components.PreferenceSwitchWithDivider
import com.sipun.playora.core.ui.components.ResetIconButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.settings.composables.LanguageSelectionBottomSheet
import com.sipun.playora.settings.composables.SubtitleFontBottomSheet
import com.sipun.playora.settings.extensions.name
import com.sipun.playora.settings.utils.LocalesHelper
import io.github.itshedimisawi.vitro.rememberGlassBackdrop

@Composable
fun SubtitlePreferencesScreen(viewModel: SubtitlePreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubtitlePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun SubtitlePreferencesScreenContent(
    state: SubtitlePreferencesUiState,
    onAction: (SubtitlePreferencesUiEvent) -> Unit,
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
    val context = LocalContext.current
    val noAppFoundMessage = stringResource(R.string.no_app_found)
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
                        title = stringResource(id = R.string.subtitle),
                        navigationIcon = {
                            AppBackButton(onClick = { onAction(SubtitlePreferencesUiEvent.NavigateUp) })
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
                            ListSectionTitle(text = stringResource(id = R.string.playback))
                            PreferenceSectionCard {
                                ClickablePreferenceItem(
                                    title = stringResource(id = R.string.preferred_subtitle_lang),
                                    description =
                                        LocalesHelper.getLocaleDisplayLanguage(
                                            state.preferences.preferredSubtitleLanguage,
                                        )
                                            .takeIf { it.isNotBlank() }
                                            ?: stringResource(R.string.preferred_subtitle_lang_description),
                                    icon = AppIcons.Language,
                                    onClick = {
                                        onAction(
                                            SubtitlePreferencesUiEvent.ShowDialog(
                                                value = SubtitlePreferenceDialog.SubtitleLanguageDialog,
                                            ),
                                        )
                                    },
                                    isFirstItem = true,
                                    isLastItem = true,
                                )
                            }
                            ListSectionTitle(text = stringResource(id = R.string.appearance_name))
                            PreferenceSectionCard {
                                PreferenceSwitchWithDivider(
                                    title = stringResource(R.string.system_caption_style),
                                    description = stringResource(R.string.system_caption_style_desc),
                                    icon = AppIcons.Caption,
                                    isChecked = state.preferences.useSystemCaptionStyle,
                                    onChecked = {
                                        onAction(SubtitlePreferencesUiEvent.ToggleUseSystemCaptionStyle)
                                    },
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS))
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                noAppFoundMessage,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    },
                                    isFirstItem = true,
                                )
                                ClickablePreferenceItem(
                                    title = stringResource(id = R.string.subtitle_font),
                                    description = state.preferences.subtitleFont.name(),
                                    icon = AppIcons.Font,
                                    enabled = state.preferences.useSystemCaptionStyle.not(),
                                    onClick = {
                                        onAction(
                                            SubtitlePreferencesUiEvent.ShowDialog(
                                                value = SubtitlePreferenceDialog.SubtitleFontDialog,
                                            ),
                                        )
                                    },
                                )
                                PreferenceSwitch(
                                    title = stringResource(id = R.string.subtitle_text_bold),
                                    description = stringResource(id = R.string.subtitle_text_bold_desc),
                                    icon = AppIcons.Bold,
                                    enabled = state.preferences.useSystemCaptionStyle.not(),
                                    isChecked = state.preferences.subtitleTextBold,
                                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleSubtitleTextBold) },
                                )
                                PreferenceSlider(
                                    title = stringResource(id = R.string.subtitle_text_size),
                                    description = state.preferences.subtitleTextSize.toString(),
                                    icon = AppIcons.FontSize,
                                    enabled = state.preferences.useSystemCaptionStyle.not(),
                                    value = state.preferences.subtitleTextSize.toFloat(),
                                    valueRange = 10f..60f,
                                    onValueChange = {
                                        onAction(SubtitlePreferencesUiEvent.UpdateSubtitleFontSize(it.toInt()))
                                    },
                                    trailingContent = {
                                        ResetIconButton(
                                            enabled = state.preferences.useSystemCaptionStyle.not(),
                                            onClick = {
                                                onAction(
                                                    SubtitlePreferencesUiEvent.UpdateSubtitleFontSize(
                                                        PlayerPreferences.DEFAULT_SUBTITLE_TEXT_SIZE,
                                                    ),
                                                )
                                            },
                                            contentDescription = stringResource(
                                                id = R.string.reset_subtitle_text_size,
                                            ),
                                        )
                                    },
                                )
                                PreferenceSwitch(
                                    title = stringResource(id = R.string.subtitle_background),
                                    description = stringResource(id = R.string.subtitle_background_desc),
                                    icon = AppIcons.Background,
                                    enabled = state.preferences.useSystemCaptionStyle.not(),
                                    isChecked = state.preferences.subtitleBackground,
                                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleSubtitleBackground) },
                                )
                                ClickablePreferenceItem(
                                    title = stringResource(id = R.string.reset_subtitle_position),
                                    description = stringResource(id = R.string.reset_subtitle_position_desc),
                                    icon = AppIcons.History,
                                    enabled = state.preferences.subtitleOffsetY != 0f,
                                    isDestructive = true,
                                    onClick = { onAction(SubtitlePreferencesUiEvent.ResetSubtitlePosition) },
                                    isLastItem = true,
                                )
                            }
                        }
                    }
                }
            }

            state.showDialog?.let { showDialog ->
                when (showDialog) {
                    SubtitlePreferenceDialog.SubtitleLanguageDialog -> {
                        LanguageSelectionBottomSheet(
                            title = stringResource(id = R.string.preferred_subtitle_lang),
                            subtitle = stringResource(id = R.string.preferred_subtitle_lang_desc),
                            languages = languages,
                            selectedLanguage = state.preferences.preferredSubtitleLanguage,
                            onApply = {
                                onAction(
                                    SubtitlePreferencesUiEvent.UpdateSubtitleLanguage(it),
                                )
                                onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                            },
                            onDismiss = {
                                onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                            },
                        )
                    }

                    SubtitlePreferenceDialog.SubtitleFontDialog -> {
                        SubtitleFontBottomSheet(
                            selectedFont = state.preferences.subtitleFont,
                            onApply = {
                                onAction(
                                    SubtitlePreferencesUiEvent.UpdateSubtitleFont(value = it),
                                )
                                onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                            },
                            onDismiss = {
                                onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                            },
                        )
                    }
                }
            }
        }
    }
}
