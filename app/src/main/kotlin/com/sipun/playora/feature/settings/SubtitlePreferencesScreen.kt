package com.sipun.playora.settings.screens.subtitle

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.model.Font
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.PreferenceSlider
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.components.PreferenceSwitchWithDivider
import com.sipun.playora.core.ui.components.RadioTextButton
import com.sipun.playora.core.ui.components.ResetIconButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.settings.composables.OptionsDialog
import com.sipun.playora.settings.extensions.name
import com.sipun.playora.settings.utils.LocalesHelper
import java.nio.charset.Charset

@Composable
fun SubtitlePreferencesScreen(viewModel: SubtitlePreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubtitlePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
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
    val charsetResource = stringArrayResource(id = R.array.charsets_list)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.subtitle),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = {
                            onAction(SubtitlePreferencesUiEvent.NavigateUp)
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
            ListSectionTitle(text = stringResource(id = R.string.playback))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
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
                )
                ClickablePreferenceItem(
                    title = stringResource(R.string.subtitle_text_encoding),
                    description = charsetResource.first {
                        it.contains(state.preferences.subtitleTextEncoding)
                    },
                    icon = AppIcons.Subtitle,
                    onClick = {
                        onAction(
                            SubtitlePreferencesUiEvent.ShowDialog(
                                value = SubtitlePreferenceDialog.SubtitleEncodingDialog,
                            ),
                        )
                    },
                    isLastItem = true,
                )
            }
            ListSectionTitle(text = stringResource(id = R.string.appearance_name))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitchWithDivider(
                    title = stringResource(R.string.system_caption_style),
                    description = stringResource(R.string.system_caption_style_desc),
                    icon = AppIcons.Caption,
                    isChecked = state.preferences.useSystemCaptionStyle,
                    onChecked = {
                        onAction(SubtitlePreferencesUiEvent.ToggleUseSystemCaptionStyle)
                    },
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_CAPTIONING_SETTINGS))
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
                                id = R.string.reset_seek_increment,
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
                PreferenceSwitch(
                    title = stringResource(R.string.embedded_styles),
                    description = stringResource(R.string.embedded_styles_desc),
                    icon = AppIcons.Style,
                    isChecked = state.preferences.applyEmbeddedStyles,
                    onClick = { onAction(SubtitlePreferencesUiEvent.ToggleApplyEmbeddedStyles) },
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

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                SubtitlePreferenceDialog.SubtitleLanguageDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.preferred_subtitle_lang),
                        onDismissClick = {
                            onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(items = languages) {
                            RadioTextButton(
                                text = it.first,
                                selected = it.second == state.preferences.preferredSubtitleLanguage,
                                onClick = {
                                    onAction(
                                        SubtitlePreferencesUiEvent.UpdateSubtitleLanguage(
                                            it.second,
                                        ),
                                    )
                                    onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }

                SubtitlePreferenceDialog.SubtitleFontDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.subtitle_font),
                        onDismissClick = {
                            onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(Font.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = it == state.preferences.subtitleFont,
                                onClick = {
                                    onAction(
                                        SubtitlePreferencesUiEvent.UpdateSubtitleFont(value = it),
                                    )
                                    onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }

                SubtitlePreferenceDialog.SubtitleEncodingDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.subtitle_text_encoding),
                        onDismissClick = {
                            onAction(SubtitlePreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(items = charsetResource) {
                            val currentCharset = it.substringAfterLast(
                                delimiter = "(",
                                missingDelimiterValue = "",
                            ).removeSuffix(suffix = ")")
                            if (currentCharset.isEmpty() || Charset.isSupported(currentCharset)) {
                                RadioTextButton(
                                    text = it,
                                    selected =
                                        currentCharset == state.preferences.subtitleTextEncoding,
                                    onClick = {
                                        onAction(
                                            SubtitlePreferencesUiEvent.UpdateSubtitleEncoding(
                                                value = currentCharset,
                                            ),
                                        )
                                        onAction(
                                            SubtitlePreferencesUiEvent.ShowDialog(value = null),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SubtitlePreferencesScreenPreview() {
    PlayoraTheme {
        SubtitlePreferencesScreenContent(
            state = SubtitlePreferencesUiState(),
            onAction = {},
        )
    }
}
