package com.sipun.playora.settings.screens.appearance

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.DynamicPalette
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AppearancePreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<AppearancePreferencesUiState, AppearancePreferencesEvent>() {

    data class Output(val navigateUp: () -> Unit)

    private val stateInternal = MutableStateFlow(
        value = AppearancePreferencesUiState(
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    override val state: StateFlow<AppearancePreferencesUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                stateInternal.update { it.copy(preferences = preferences) }
            }
        }
    }

    override fun onAction(action: AppearancePreferencesEvent) {
        when (action) {
            is AppearancePreferencesEvent.NavigateUp -> output.navigateUp()

            is AppearancePreferencesEvent.ShowDialog -> showDialog(action.value)

            is AppearancePreferencesEvent.SetTheme -> setTheme(
                themeConfig = action.themeConfig,
                pureBlack = action.pureBlack,
            )

            is AppearancePreferencesEvent.SetDynamicColor -> setDynamicColor(action.enabled)

            is AppearancePreferencesEvent.SetDynamicPalette -> setDynamicPalette(action.palette)

            is AppearancePreferencesEvent.SetCustomColor -> setCustomColor(action.color)

            is AppearancePreferencesEvent.ToggleDarkTheme -> toggleDarkTheme()

            is AppearancePreferencesEvent.UpdateThemeConfig -> updateThemeConfig(action.themeConfig)

            is AppearancePreferencesEvent.ToggleUseDynamicColors -> toggleUseDynamicColors()

            is AppearancePreferencesEvent.ToggleUseHighContrastDarkTheme ->
                toggleUseHighContrastDarkTheme()
        }
    }

    private fun showDialog(value: AppearancePreferenceDialog?) {
        stateInternal.update { it.copy(showDialog = value) }
    }

    private fun setTheme(themeConfig: ThemeConfig, pureBlack: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    themeConfig = themeConfig,
                    useHighContrastDarkTheme = pureBlack,
                )
            }
        }
    }

    private fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(useDynamicColors = enabled)
            }
        }
    }

    private fun setDynamicPalette(palette: DynamicPalette) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(dynamicPalette = palette)
            }
        }
    }

    private fun setCustomColor(color: Int) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(customColor = color)
            }
        }
    }

    private fun toggleDarkTheme() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    themeConfig = if (it.themeConfig == ThemeConfig.ON) {
                        ThemeConfig.OFF
                    } else {
                        ThemeConfig.ON
                    },
                )
            }
        }
    }

    private fun updateThemeConfig(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(themeConfig = themeConfig)
            }
        }
    }

    private fun toggleUseDynamicColors() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(useDynamicColors = !it.useDynamicColors)
            }
        }
    }

    private fun toggleUseHighContrastDarkTheme() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(useHighContrastDarkTheme = !it.useHighContrastDarkTheme)
            }
        }
    }
}

@Stable
data class AppearancePreferencesUiState(
    val showDialog: AppearancePreferenceDialog? = null,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface AppearancePreferencesEvent {
    data object NavigateUp : AppearancePreferencesEvent

    data class ShowDialog(val value: AppearancePreferenceDialog?) : AppearancePreferencesEvent

    data class SetTheme(
        val themeConfig: ThemeConfig,
        val pureBlack: Boolean,
    ) : AppearancePreferencesEvent

    data class SetDynamicColor(val enabled: Boolean) : AppearancePreferencesEvent
    data class SetDynamicPalette(val palette: DynamicPalette) : AppearancePreferencesEvent
    data class SetCustomColor(val color: Int) : AppearancePreferencesEvent

    data object ToggleDarkTheme : AppearancePreferencesEvent
    data class UpdateThemeConfig(val themeConfig: ThemeConfig) : AppearancePreferencesEvent
    data object ToggleUseDynamicColors : AppearancePreferencesEvent
    data object ToggleUseHighContrastDarkTheme : AppearancePreferencesEvent
}

sealed interface AppearancePreferenceDialog {
    data object Theme : AppearancePreferenceDialog
}
