package com.sipun.playora.settings.screens.appearance

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.model.ApplicationPreferences
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

            is AppearancePreferencesEvent.SetTheme -> setTheme(
                themeConfig = action.themeConfig,
                pureBlack = action.pureBlack,
            )

            is AppearancePreferencesEvent.SetDynamicColor -> setDynamicColor(action.enabled)

            is AppearancePreferencesEvent.SetLiquidGlass -> setLiquidGlass(action.enabled)

            is AppearancePreferencesEvent.SetCustomColor -> setCustomColor(action.color)
        }
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

    private fun setLiquidGlass(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(useLiquidGlass = enabled)
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
}

@Stable
data class AppearancePreferencesUiState(
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface AppearancePreferencesEvent {
    data object NavigateUp : AppearancePreferencesEvent

    data class SetTheme(
        val themeConfig: ThemeConfig,
        val pureBlack: Boolean,
    ) : AppearancePreferencesEvent

    data class SetDynamicColor(val enabled: Boolean) : AppearancePreferencesEvent
    data class SetLiquidGlass(val enabled: Boolean) : AppearancePreferencesEvent
    data class SetCustomColor(val color: Int) : AppearancePreferencesEvent
}
