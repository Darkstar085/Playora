package com.sipun.playora.settings.screens.general

import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.media.extensions.clearAllCache
import com.sipun.playora.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class GeneralPreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val imageLoader: ImageLoader,
    @InjectedParam internal var output: Output,
) : MviViewModel<GeneralPreferencesUiState, GeneralPreferencesUiEvent>() {

    data class Output(
        val navigateUp: () -> Unit,
        val openFolders: () -> Unit = {},
        val showMessage: (Int) -> Unit = {},
    )

    private val stateInternal = MutableStateFlow(value = GeneralPreferencesUiState())
    override val state: StateFlow<GeneralPreferencesUiState> = stateInternal.asStateFlow()

    override fun onAction(action: GeneralPreferencesUiEvent) {
        when (action) {
            is GeneralPreferencesUiEvent.NavigateUp -> output.navigateUp()

            is GeneralPreferencesUiEvent.OpenFolders -> output.openFolders()

            is GeneralPreferencesUiEvent.ShowDialog -> showDialog(action.value)

            is GeneralPreferencesUiEvent.ClearThumbnailCache -> clearThumbnailCache()

            is GeneralPreferencesUiEvent.ResetSettings -> resetSettings()
        }
    }

    private fun showDialog(value: GeneralPreferencesDialog?) {
        stateInternal.update { it.copy(showDialog = value) }
    }

    private fun clearThumbnailCache() {
        viewModelScope.launch { imageLoader.clearAllCache() }
    }

    private fun resetSettings() {
        viewModelScope.launch { preferencesRepository.resetPreferences() }
    }
}

data class GeneralPreferencesUiState(
    val showDialog: GeneralPreferencesDialog? = null,
    val isBusy: Boolean = false,
)

sealed interface GeneralPreferencesDialog {
    data object ClearThumbnailCacheDialog : GeneralPreferencesDialog
    data object ResetSettingsDialog : GeneralPreferencesDialog
}

sealed interface GeneralPreferencesUiEvent {
    data object NavigateUp : GeneralPreferencesUiEvent

    data object OpenFolders : GeneralPreferencesUiEvent
    data class ShowDialog(val value: GeneralPreferencesDialog?) : GeneralPreferencesUiEvent
    data object ClearThumbnailCache : GeneralPreferencesUiEvent
    data object ResetSettings : GeneralPreferencesUiEvent
}
