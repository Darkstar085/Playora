package com.sipun.playora.settings.screens.gesture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.common.extensions.round
import com.sipun.playora.core.model.DoubleTapGesture
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppDialogWithDoneAndCancelButtons
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ListSectionTitle
import com.sipun.playora.core.ui.components.PreferenceSlider
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.components.PreferenceSwitchWithDivider
import com.sipun.playora.core.ui.components.RadioTextButton
import com.sipun.playora.core.ui.components.ResetIconButton
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.preview.DayNightPreview
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.settings.composables.OptionsDialog
import com.sipun.playora.settings.extensions.name

@Composable
fun GesturePreferencesScreen(viewModel: GesturePreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GesturePreferencesScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun GesturePreferencesScreenContent(
    state: GesturePreferencesUiState,
    onAction: (GesturePreferencesUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.gestures),
                navigationIcon = {
                    AppBackButton(onClick = { onAction(GesturePreferencesUiEvent.NavigateUp) })
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
            ListSectionTitle(text = stringResource(id = R.string.gestures))
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                PreferenceSwitch(
                    title = stringResource(id = R.string.seek_gesture),
                    description = stringResource(id = R.string.seek_gesture_description),
                    icon = AppIcons.SwipeHorizontal,
                    isChecked = state.preferences.useSeekControls,
                    onClick = { onAction(GesturePreferencesUiEvent.ToggleUseSeekControls) },
                    isFirstItem = true,
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.brightness_gesture),
                    description = stringResource(id = R.string.brightness_gesture_description),
                    icon = AppIcons.SwipeVertical,
                    isChecked = state.preferences.enableBrightnessSwipeGesture,
                    onClick = {
                        onAction(GesturePreferencesUiEvent.ToggleEnableBrightnessSwipeGesture)
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.volume_gesture),
                    description = stringResource(id = R.string.volume_gesture_description),
                    icon = AppIcons.SwipeVertical,
                    isChecked = state.preferences.enableVolumeSwipeGesture,
                    onClick = {
                        onAction(GesturePreferencesUiEvent.ToggleEnableVolumeSwipeGesture)
                    },
                )
                PreferenceSwitch(
                    title = stringResource(id = R.string.zoom_gesture),
                    description = stringResource(id = R.string.zoom_gesture_description),
                    icon = AppIcons.Pinch,
                    isChecked = state.preferences.useZoomControls,
                    onClick = { onAction(GesturePreferencesUiEvent.ToggleUseZoomControls) },
                )
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.double_tap),
                    description = stringResource(id = R.string.double_tap_description),
                    icon = AppIcons.DoubleTap,
                    isChecked = (state.preferences.doubleTapGesture != DoubleTapGesture.NONE),
                    onChecked = { onAction(GesturePreferencesUiEvent.ToggleDoubleTapGesture) },
                    onClick = {
                        onAction(
                            GesturePreferencesUiEvent.ShowDialog(
                                value = GesturePreferenceDialog.DoubleTapDialog,
                            ),
                        )
                    },
                )
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.long_press_gesture),
                    description = stringResource(
                        id = R.string.long_press_gesture_desc,
                        state.preferences.longPressControlsSpeed,
                    ),
                    icon = AppIcons.Tap,
                    isChecked = state.preferences.useLongPressControls,
                    onChecked = { onAction(GesturePreferencesUiEvent.ToggleUseLongPressControls) },
                    onClick = {
                        onAction(
                            GesturePreferencesUiEvent.ShowDialog(
                                value = GesturePreferenceDialog.LongPressControlsSpeedDialog,
                            ),
                        )
                    },
                )
                PreferenceSlider(
                    title = stringResource(R.string.seek_increment),
                    description = stringResource(
                        R.string.seconds,
                        state.preferences.seekIncrement,
                    ),
                    isLastItem = true,
                    icon = AppIcons.Replay,
                    value = state.preferences.seekIncrement.toFloat(),
                    valueRange = 1.0f..60.0f,
                    onValueChange = {
                        onAction(GesturePreferencesUiEvent.UpdateSeekIncrement(it.toInt()))
                    },
                    onReset = {
                        onAction(
                            GesturePreferencesUiEvent.UpdateSeekIncrement(
                                PlayerPreferences.DEFAULT_SEEK_INCREMENT,
                            ),
                        )
                    },
                    trailingContent = {
                        ResetIconButton(
                            onClick = {
                                onAction(
                                    GesturePreferencesUiEvent.UpdateSeekIncrement(
                                        PlayerPreferences.DEFAULT_SEEK_INCREMENT,
                                    ),
                                )
                            },
                            contentDescription = stringResource(
                                id = R.string.reset_seek_increment,
                            ),
                        )
                    },
                )
            }
        }

        state.showDialog?.let { showDialog ->
            when (showDialog) {
                GesturePreferenceDialog.DoubleTapDialog -> {
                    OptionsDialog(
                        text = stringResource(id = R.string.double_tap),
                        onDismissClick = {
                            onAction(GesturePreferencesUiEvent.ShowDialog(value = null))
                        },
                    ) {
                        items(DoubleTapGesture.entries.toTypedArray()) {
                            RadioTextButton(
                                text = it.name(),
                                selected = (it == state.preferences.doubleTapGesture),
                                onClick = {
                                    onAction(
                                        GesturePreferencesUiEvent.UpdateDoubleTapGesture(
                                            gesture = it,
                                        ),
                                    )
                                    onAction(GesturePreferencesUiEvent.ShowDialog(value = null))
                                },
                            )
                        }
                    }
                }

                GesturePreferenceDialog.LongPressControlsSpeedDialog -> {
                    var longPressControlsSpeed by remember {
                        mutableFloatStateOf(state.preferences.longPressControlsSpeed)
                    }

                    AppDialogWithDoneAndCancelButtons(
                        title = stringResource(R.string.long_press_gesture),
                        onDoneClick = {
                            onAction(
                                GesturePreferencesUiEvent.UpdateLongPressControlsSpeed(
                                    value = longPressControlsSpeed,
                                ),
                            )
                            onAction(GesturePreferencesUiEvent.ShowDialog(value = null))
                        },
                        onDismissClick = {
                            onAction(GesturePreferencesUiEvent.ShowDialog(value = null))
                        },
                        content = {
                            Text(
                                text = "$longPressControlsSpeed",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Slider(
                                value = longPressControlsSpeed,
                                onValueChange = {
                                    longPressControlsSpeed = it.round(decimalPlaces = 1)
                                },
                                valueRange = 0.2f..4.0f,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                    )
                }
            }
        }
    }
}

@DayNightPreview
@Composable
private fun GesturePreferencesScreenPreview() {
    PlayoraTheme {
        GesturePreferencesScreenContent(
            state = GesturePreferencesUiState(),
            onAction = {},
        )
    }
}
