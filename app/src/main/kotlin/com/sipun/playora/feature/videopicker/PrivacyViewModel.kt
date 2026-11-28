package com.sipun.playora.feature.videopicker.privacy

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.sipun.playora.core.data.repository.PinVerificationResult
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration.Companion.seconds

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
    private var lockoutJob: Job? = null
    private var hasPrivacySortOverride = false

    init {
        viewModelScope.launch {
            val hasPin = privacyPinRepository.hasPinSet()
            val biometricEnabled = hasPin && privacyPinRepository.isBiometricEnabled()
            val lockoutSeconds = if (hasPin) privacyPinRepository.getLockoutRemainingSeconds() else 0
            stateInternal.update {
                it.copy(
                    stage = if (hasPin) PrivacyStage.LOCKED else PrivacyStage.SET_PIN,
                    biometricEnabled = biometricEnabled,
                    lockoutRemainingSeconds = lockoutSeconds,
                )
            }
            if (lockoutSeconds > 0) {
                startLockoutCountdown(lockoutSeconds)
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

            is PrivacyAction.StartChangePin -> startChangePin()

            is PrivacyAction.CancelPinChange -> cancelPinChange()

            is PrivacyAction.SubmitCurrentPin -> submitCurrentPin(action.pin)

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

    private fun startChangePin() {
        if (stateInternal.value.stage != PrivacyStage.UNLOCKED) return
        viewModelScope.launch {
            val lockoutSeconds = privacyPinRepository.getLockoutRemainingSeconds()
            stateInternal.update {
                it.copy(
                    stage = PrivacyStage.VERIFY_CURRENT_PIN,
                    pendingPin = null,
                    pinErrorCount = 0,
                    setPinGeneration = 0,
                    isChangingPin = true,
                    lockoutRemainingSeconds = lockoutSeconds,
                )
            }
            if (lockoutSeconds > 0) {
                startLockoutCountdown(lockoutSeconds)
            }
        }
    }

    private fun cancelPinChange() {
        lockoutJob?.cancel()
        if (stateInternal.value.isChangingPin) {
            stateInternal.update {
                it.copy(
                    stage = PrivacyStage.UNLOCKED,
                    pendingPin = null,
                    pinErrorCount = 0,
                    setPinGeneration = 0,
                    isChangingPin = false,
                    lockoutRemainingSeconds = 0,
                )
            }
        } else {
            output.navigateUp()
        }
    }

    private fun submitCurrentPin(pin: String) {
        if (stateInternal.value.lockoutRemainingSeconds > 0) return
        viewModelScope.launch {
            when (val result = privacyPinRepository.verifyPinWithLockout(pin = pin)) {
                PinVerificationResult.Success -> {
                    lockoutJob?.cancel()
                    stateInternal.update {
                        it.copy(
                            stage = PrivacyStage.SET_PIN,
                            pendingPin = null,
                            pinErrorCount = 0,
                            setPinGeneration = 0,
                            lockoutRemainingSeconds = 0,
                            isChangingPin = true,
                        )
                    }
                }

                is PinVerificationResult.Invalid -> {
                    stateInternal.update { it.copy(pinErrorCount = it.pinErrorCount + 1) }
                }

                is PinVerificationResult.LockedOut -> {
                    startLockoutCountdown(result.remainingSeconds)
                }
            }
        }
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
        val changingPin = stateInternal.value.isChangingPin
        viewModelScope.launch {
            privacyPinRepository.setPin(pin = pin, preserveBiometric = changingPin)
            if (changingPin) {
                stateInternal.update {
                    it.copy(
                        stage = PrivacyStage.UNLOCKED,
                        pendingPin = null,
                        pinErrorCount = 0,
                        setPinGeneration = 0,
                        isChangingPin = false,
                    )
                }
            } else {
                stateInternal.update {
                    it.copy(
                        stage = PrivacyStage.BIOMETRIC_SETUP,
                        pendingPin = null,
                        pinErrorCount = 0,
                        setPinGeneration = 0,
                    )
                }
            }
        }
    }

    private fun submitUnlockPin(pin: String) {
        if (stateInternal.value.lockoutRemainingSeconds > 0) return
        viewModelScope.launch {
            when (val result = privacyPinRepository.verifyPinWithLockout(pin = pin)) {
                PinVerificationResult.Success -> {
                    lockoutJob?.cancel()
                    unlockPrivacy()
                }

                is PinVerificationResult.Invalid -> {
                    stateInternal.update { it.copy(pinErrorCount = it.pinErrorCount + 1) }
                }

                is PinVerificationResult.LockedOut -> {
                    startLockoutCountdown(result.remainingSeconds)
                }
            }
        }
    }

    private fun startLockoutCountdown(initialSeconds: Int) {
        lockoutJob?.cancel()
        stateInternal.update {
            it.copy(
                lockoutRemainingSeconds = initialSeconds,
                pinErrorCount = it.pinErrorCount + 1,
            )
        }
        lockoutJob = viewModelScope.launch {
            var remaining = initialSeconds
            while (remaining > 0) {
                delay(1.seconds)
                remaining = privacyPinRepository.getLockoutRemainingSeconds()
                stateInternal.update { it.copy(lockoutRemainingSeconds = remaining) }
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
                lockoutRemainingSeconds = 0,
                isChangingPin = false,
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
            val result = try {
                privacyRepository.unhideVideos(selectionItems.toVideos())
            } finally {
                stateInternal.update { it.copy(isUnhiding = false) }
            }
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
    VERIFY_CURRENT_PIN,
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
    val lockoutRemainingSeconds: Int = 0,
    val isChangingPin: Boolean = false,
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
    data object StartChangePin : PrivacyAction
    data object CancelPinChange : PrivacyAction
    data class SubmitCurrentPin(val pin: String) : PrivacyAction
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
