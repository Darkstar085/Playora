package com.sipun.playora.feature.videopicker.privacy

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.data.repository.PrivacyPinRepository
import com.sipun.playora.core.data.repository.PrivacyRepository
import com.sipun.playora.core.domain.GetHiddenVideosUseCase
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.MediaInfo
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.model.Sort
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.ui.base.MviViewModel
import com.sipun.playora.feature.videopicker.state.SelectionItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

const val PRIVACY_PIN_LENGTH = 4

@KoinViewModel
class PrivacyViewModel(
    private val privacyRepository: PrivacyRepository,
    private val privacyPinRepository: PrivacyPinRepository,
    private val getHiddenVideosUseCase: GetHiddenVideosUseCase,
    private val preferencesRepository: PreferencesRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<PrivacyUiState, PrivacyAction>() {

    data class Output(
        val navigateUp: () -> Unit,
        val playVideo: (uri: Uri, playlist: List<Uri>?) -> Unit,
        val playVideos: (List<Uri>) -> Unit,
    )

    private val stateInternal = MutableStateFlow(value = PrivacyUiState())
    override val state: StateFlow<PrivacyUiState> = stateInternal.asStateFlow()

    private val eventsInternal = Channel<PrivacyEvent>()
    val events = eventsInternal.receiveAsFlow()

    private var hiddenVideosJob: Job? = null
    private var hasPrivacySortOverride = false

    init {
        viewModelScope.launch {
            val hasPin = privacyPinRepository.hasPinSet()
            val biometricEnabled = hasPin && privacyPinRepository.isBiometricEnabled()
            stateInternal.update {
                it.copy(
                    stage = if (hasPin) PrivacyStage.LOCKED else PrivacyStage.SET_PIN,
                    biometricEnabled = biometricEnabled,
                )
            }
        }
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { prefs ->
                val inheritedSort = Sort(
                    by = prefs.sortBy,
                    order = prefs.sortOrder,
                )
                val shouldRefresh = !hasPrivacySortOverride &&
                        stateInternal.value.stage == PrivacyStage.UNLOCKED &&
                        stateInternal.value.sort != inheritedSort
                stateInternal.update {
                    it.copy(
                        preferences = prefs,
                        sort = if (hasPrivacySortOverride) it.sort else inheritedSort,
                    )
                }
                if (shouldRefresh) collectHiddenVideos()
            }
        }
    }

    override fun onAction(action: PrivacyAction) {
        when (action) {
            is PrivacyAction.NavigateUp -> output.navigateUp()

            is PrivacyAction.SubmitNewPin -> submitNewPin(action.pin)

            is PrivacyAction.SubmitPinConfirmation -> submitPinConfirmation(action.pin)

            is PrivacyAction.SubmitUnlockPin -> submitUnlockPin(action.pin)

            is PrivacyAction.BiometricAuthenticated -> {
                if (stateInternal.value.stage == PrivacyStage.LOCKED &&
                    stateInternal.value.biometricEnabled
                ) {
                    unlockPrivacy()
                }
            }

            is PrivacyAction.CompleteBiometricSetup -> completeBiometricSetup(action.enabled)

            is PrivacyAction.SetBiometricEnabled -> setBiometricEnabled(action.enabled)

            is PrivacyAction.PlayVideo -> playVideo(action.video)

            is PrivacyAction.PlaySelected -> playSelected(action.selectionItems)

            is PrivacyAction.UnhideSelected -> unhideVideos(action.selectionItems)

            is PrivacyAction.DeleteSelected -> deleteVideos(action.selectionItems)

            is PrivacyAction.ShowMediaInfo -> showMediaInfo(action.video)

            is PrivacyAction.DismissMediaInfo -> stateInternal.update { it.copy(mediaInfo = null) }

            is PrivacyAction.UpdateSort -> updateSort(action.sort)

            is PrivacyAction.UpdateQuickSettings -> {
                updateSort(action.sort)
                viewModelScope.launch {
                    preferencesRepository.updateApplicationPreferences {
                        it.copy(mediaLayoutMode = action.layoutMode)
                    }
                }
            }
        }
    }

    private fun updateSort(sort: Sort) {
        hasPrivacySortOverride = true
        val shouldRefresh = stateInternal.value.stage == PrivacyStage.UNLOCKED &&
                stateInternal.value.sort != sort
        stateInternal.update { it.copy(sort = sort) }
        if (shouldRefresh) collectHiddenVideos()
    }

    private fun submitNewPin(pin: String) {
        stateInternal.update {
            it.copy(
                pendingPin = pin,
                stage = PrivacyStage.CONFIRM_PIN,
                pinErrorCount = 0,
            )
        }
    }

    private fun submitPinConfirmation(pin: String) {
        val pendingPin = stateInternal.value.pendingPin
        if (pin != pendingPin) {

            stateInternal.update {
                it.copy(
                    stage = PrivacyStage.SET_PIN,
                    pendingPin = null,
                    pinErrorCount = 0,
                    setPinGeneration = it.setPinGeneration + 1,
                )
            }
            return
        }
        viewModelScope.launch {
            privacyPinRepository.setPin(pin = pin)
            stateInternal.update {
                it.copy(
                    stage = PrivacyStage.BIOMETRIC_SETUP,
                    pendingPin = null,
                    pinErrorCount = 0,
                )
            }
        }
    }

    private fun submitUnlockPin(pin: String) {
        viewModelScope.launch {
            val isValid = privacyPinRepository.verifyPin(pin = pin)
            if (isValid) {
                unlockPrivacy()
            } else {
                stateInternal.update { it.copy(pinErrorCount = it.pinErrorCount + 1) }
            }
        }
    }

    private fun completeBiometricSetup(enabled: Boolean) {
        if (stateInternal.value.stage != PrivacyStage.BIOMETRIC_SETUP) return
        viewModelScope.launch {
            privacyPinRepository.setBiometricEnabled(enabled = enabled)
            stateInternal.update { it.copy(biometricEnabled = enabled) }
            unlockPrivacy()
        }
    }

    private fun setBiometricEnabled(enabled: Boolean) {
        if (stateInternal.value.stage != PrivacyStage.UNLOCKED) return
        viewModelScope.launch {
            privacyPinRepository.setBiometricEnabled(enabled = enabled)
            stateInternal.update { it.copy(biometricEnabled = enabled) }
        }
    }

    private fun unlockPrivacy() {
        stateInternal.update {
            it.copy(
                stage = PrivacyStage.UNLOCKED,
                pinErrorCount = 0,
            )
        }
        collectHiddenVideos()
    }

    private fun collectHiddenVideos() {
        hiddenVideosJob?.cancel()
        hiddenVideosJob = viewModelScope.launch {
            stateInternal.update { it.copy(isLoading = true) }
            getHiddenVideosUseCase(stateInternal.value.sort).collect { videos ->
                stateInternal.update {
                    it.copy(
                        hiddenVideos = videos,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun playVideo(video: Video) {
        val playlist = stateInternal.value.hiddenVideos.map { it.uriString.toUri() }
        output.playVideo(video.uriString.toUri(), playlist.takeIf { it.isNotEmpty() })
    }

    private fun playSelected(selectionItems: Set<SelectionItem>) {
        val uris = selectionItems.toVideos().map { it.uriString.toUri() }
        if (uris.isNotEmpty()) {
            output.playVideos(uris)
        }
    }

    private fun unhideVideos(selectionItems: Set<SelectionItem>) {
        viewModelScope.launch {
            stateInternal.update { it.copy(isUnhiding = true) }
            val result = privacyRepository.unhideVideos(selectionItems.toVideos())
            stateInternal.update { it.copy(isUnhiding = false) }
            if (result.relocatedCount > 0) {
                eventsInternal.send(PrivacyEvent.VideosRelocated(result.relocatedCount))
            }
        }
    }

    private fun deleteVideos(selectionItems: Set<SelectionItem>) {
        viewModelScope.launch { privacyRepository.deleteHiddenVideos(selectionItems.toVideos()) }
    }

    private fun showMediaInfo(video: Video) {
        viewModelScope.launch {
            val mediaInfo = privacyRepository.getHiddenVideoInfo(video.id)
            if (mediaInfo != null) {
                stateInternal.update { it.copy(mediaInfo = mediaInfo) }
            }
        }
    }

    private fun Set<SelectionItem>.toVideos(): List<Video> {
        val selectedUris = filterIsInstance<SelectionItem.Video>().map { it.uriString }.toSet()
        return stateInternal.value.hiddenVideos.filter { it.uriString in selectedUris }
    }
}

enum class PrivacyStage {
    LOADING,
    LOCKED,
    SET_PIN,
    CONFIRM_PIN,
    BIOMETRIC_SETUP,
    UNLOCKED,
}

@Stable
data class PrivacyUiState(
    val stage: PrivacyStage = PrivacyStage.LOADING,
    val pendingPin: String? = null,
    val pinErrorCount: Int = 0,
    val setPinGeneration: Int = 0,
    val biometricEnabled: Boolean = false,
    val hiddenVideos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val isUnhiding: Boolean = false,
    val sort: Sort = Sort(
        by = Sort.By.DATE,
        order = Sort.Order.DESCENDING,
    ),
    val mediaInfo: MediaInfo? = null,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface PrivacyAction {
    data object NavigateUp : PrivacyAction

    data class SubmitNewPin(val pin: String) : PrivacyAction
    data class SubmitPinConfirmation(val pin: String) : PrivacyAction
    data class SubmitUnlockPin(val pin: String) : PrivacyAction
    data object BiometricAuthenticated : PrivacyAction
    data class CompleteBiometricSetup(val enabled: Boolean) : PrivacyAction
    data class SetBiometricEnabled(val enabled: Boolean) : PrivacyAction
    data class PlayVideo(val video: Video) : PrivacyAction
    data class PlaySelected(val selectionItems: Set<SelectionItem>) : PrivacyAction
    data class UnhideSelected(val selectionItems: Set<SelectionItem>) : PrivacyAction
    data class DeleteSelected(val selectionItems: Set<SelectionItem>) : PrivacyAction
    data class ShowMediaInfo(val video: Video) : PrivacyAction
    data object DismissMediaInfo : PrivacyAction
    data class UpdateSort(val sort: Sort) : PrivacyAction
    data class UpdateQuickSettings(val layoutMode: MediaLayoutMode, val sort: Sort) : PrivacyAction
}

sealed interface PrivacyEvent {
    data class VideosRelocated(val count: Int) : PrivacyEvent
}
