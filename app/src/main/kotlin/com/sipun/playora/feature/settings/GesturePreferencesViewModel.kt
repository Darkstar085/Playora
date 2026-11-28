package com.sipun.playora.settings.screens.gesture

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.model.DoubleTapGesture
import com.sipun.playora.core.model.PlayerPreferences
import com.sipun.playora.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class GesturePreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<GesturePreferencesUiState, GesturePreferencesUiEvent>() {

    data class Output(val navigateUp: () -> Unit)

    private val stateInternal = MutableStateFlow(
        value = GesturePreferencesUiState(
            preferences = preferencesRepository.playerPreferences.value,
        ),
    )
    override val state: StateFlow<GesturePreferencesUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.playerPreferences.collect { preferences ->
                stateInternal.update { it.copy(preferences = preferences) }
            }
        }
    }

    override fun onAction(action: GesturePreferencesUiEvent) {
        when (action) {
            is GesturePreferencesUiEvent.NavigateUp -> output.navigateUp()

            is GesturePreferencesUiEvent.ShowDialog -> showDialog(action.value)

            is GesturePreferencesUiEvent.UpdateDoubleTapGesture -> updateDoubleTapGesture(
                action.gesture,
            )

            is GesturePreferencesUiEvent.ToggleUseLongPressControls -> toggleUseLongPressControls()

            is GesturePreferencesUiEvent.ToggleDoubleTapGesture -> toggleDoubleTapGesture()

            is GesturePreferencesUiEvent.ToggleEnableBrightnessSwipeGesture ->
                toggleEnableBrightnessSwipeGesture()

            is GesturePreferencesUiEvent.ToggleEnableVolumeSwipeGesture ->
                toggleEnableVolumeSwipeGesture()

            is GesturePreferencesUiEvent.ToggleUseSeekControls -> toggleUseSeekControls()

            is GesturePreferencesUiEvent.ToggleUseZoomControls -> toggleUseZoomControls()

            is GesturePreferencesUiEvent.UpdateLongPressControlsSpeed ->
                updateLongPressControlsSpeed(
                    action.value,
                )

            is GesturePreferencesUiEvent.UpdateSeekIncrement -> updateSeekIncrement(action.value)
        }
    }

    private fun showDialog(value: GesturePreferenceDialog?) {
        stateInternal.update { it.copy(showDialog = value) }
    }

    private fun updateDoubleTapGesture(gesture: DoubleTapGesture) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences { it.copy(doubleTapGesture = gesture) }
        }
    }

    private fun toggleUseLongPressControls() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(useLongPressControls = !it.useLongPressControls)
            }
        }
    }

    private fun toggleDoubleTapGesture() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(
                    doubleTapGesture = if (it.doubleTapGesture == DoubleTapGesture.NONE) {
                        DoubleTapGesture.FAST_FORWARD_AND_REWIND
                    } else {
                        DoubleTapGesture.NONE
                    },
                )
            }
        }
    }

    private fun toggleEnableBrightnessSwipeGesture() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(enableBrightnessSwipeGesture = !it.enableBrightnessSwipeGesture)
            }
        }
    }

    private fun toggleEnableVolumeSwipeGesture() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(enableVolumeSwipeGesture = !it.enableVolumeSwipeGesture)
            }
        }
    }

    private fun toggleUseSeekControls() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(useSeekControls = !it.useSeekControls)
            }
        }
    }

    private fun toggleUseZoomControls() {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(useZoomControls = !it.useZoomControls)
            }
        }
    }

    private fun updateLongPressControlsSpeed(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences {
                it.copy(longPressControlsSpeed = value)
            }
        }
    }

    private fun updateSeekIncrement(value: Int) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences { it.copy(seekIncrement = value) }
        }
    }
}

@Stable
data class GesturePreferencesUiState(
    val showDialog: GesturePreferenceDialog? = null,
    val preferences: PlayerPreferences = PlayerPreferences(),
)

sealed interface GesturePreferenceDialog {
    data object DoubleTapDialog : GesturePreferenceDialog
    data object LongPressControlsSpeedDialog : GesturePreferenceDialog
}

sealed interface GesturePreferencesUiEvent {
    data object NavigateUp : GesturePreferencesUiEvent

    data class ShowDialog(val value: GesturePreferenceDialog?) : GesturePreferencesUiEvent
    data class UpdateDoubleTapGesture(val gesture: DoubleTapGesture) : GesturePreferencesUiEvent
    data object ToggleUseLongPressControls : GesturePreferencesUiEvent
    data object ToggleDoubleTapGesture : GesturePreferencesUiEvent
    data object ToggleEnableBrightnessSwipeGesture : GesturePreferencesUiEvent
    data object ToggleEnableVolumeSwipeGesture : GesturePreferencesUiEvent
    data object ToggleUseSeekControls : GesturePreferencesUiEvent
    data object ToggleUseZoomControls : GesturePreferencesUiEvent
    data class UpdateLongPressControlsSpeed(val value: Float) : GesturePreferencesUiEvent
    data class UpdateSeekIncrement(val value: Int) : GesturePreferencesUiEvent
}
