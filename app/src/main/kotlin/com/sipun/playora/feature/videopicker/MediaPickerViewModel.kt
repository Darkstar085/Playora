package com.sipun.playora.feature.videopicker.screens.mediapicker

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Stable
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.sipun.playora.R
import com.sipun.playora.core.common.extensions.prettyName
import com.sipun.playora.core.common.service.system.SystemService
import com.sipun.playora.core.common.storagePermission
import com.sipun.playora.core.data.repository.MediaRepository
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.data.repository.PrivacyPinRepository
import com.sipun.playora.core.data.repository.PrivacyRepository
import com.sipun.playora.core.domain.GetRecentlyPlayedVideoUseCase
import com.sipun.playora.core.domain.GetSortedMediaUseCase
import com.sipun.playora.core.domain.GetSortedVideosUseCase
import com.sipun.playora.core.domain.MediaHolder
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.media.services.TransferEvent
import com.sipun.playora.core.media.services.TransferMode
import com.sipun.playora.core.media.services.TransferProgress
import com.sipun.playora.core.media.services.TransferResult
import com.sipun.playora.core.media.sync.MediaSynchronizer
import com.sipun.playora.core.model.MarkType
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.MediaViewMode
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.model.findClosestFolder
import com.sipun.playora.core.ui.base.DataState
import com.sipun.playora.core.ui.base.MviViewModel
import com.sipun.playora.feature.videopicker.state.SelectionItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import java.io.File

@KoinViewModel
class MediaPickerViewModel(
    private val getSortedMediaUseCase: GetSortedMediaUseCase,
    private val getRecentlyPlayedVideoUseCase: GetRecentlyPlayedVideoUseCase,
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val mediaOperationsService: MediaOperationsService,
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    private val mediaSynchronizer: MediaSynchronizer,
    private val privacyRepository: PrivacyRepository,
    private val privacyPinRepository: PrivacyPinRepository,
    private val systemService: SystemService,
    private val context: Context,
    @InjectedParam private val input: Input,
    @InjectedParam internal var output: Output,
) : MviViewModel<MediaPickerUiState, MediaPickerAction>() {

    data class Input(val folderId: String?)

    data class Output(
        val navigateUp: () -> Unit,
        val playVideo: (uri: Uri, playlist: List<Uri>?) -> Unit,
        val playVideos: (List<Uri>) -> Unit,
        val openFolder: (String) -> Unit,
        val openSettings: () -> Unit,
        val openSearch: () -> Unit,
        val openPrivacy: () -> Unit,
    )

    val folderPath = input.folderId

    private val stateInternal = MutableStateFlow(
        value = MediaPickerUiState(
            folderName = folderPath?.let { File(folderPath).prettyName },
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    override val state: StateFlow<MediaPickerUiState> = stateInternal.asStateFlow()

    private var mediaCollectJob: Job? = null
    private var transferJob: Job? = null

    init {
        if (ContextCompat.checkSelfPermission(
                context,
                storagePermission,
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startMediaCollection()
        }
        collectPreferences()
    }

    override fun onAction(action: MediaPickerAction) {
        when (action) {
            is MediaPickerAction.OnNavigateUpClick -> output.navigateUp()

            is MediaPickerAction.OnPlayVideo -> output.playVideo(action.uri, action.playlist)

            is MediaPickerAction.OnFolderClick -> output.openFolder(action.folderPath)

            is MediaPickerAction.OnSettingsClick -> output.openSettings()

            is MediaPickerAction.OnSearchClick -> output.openSearch()

            is MediaPickerAction.OnPrivacyClick -> output.openPrivacy()

            is MediaPickerAction.Refresh -> refresh()

            is MediaPickerAction.RenameVideo -> renameVideo(
                action.uri,
                action.to,
            )

            is MediaPickerAction.UpdateMenu -> updateMenu(action.preferences)

            is MediaPickerAction.OnPermissionAccepted -> startMediaCollection()

            is MediaPickerAction.PlaySelectedItems -> playSelectedItems(action.selectionItems)

            is MediaPickerAction.DeleteSelectedItems -> deleteSelectedItems(
                action.selectionItems,
                action.permanently,
            )

            is MediaPickerAction.ShareSelectedItems -> shareSelectedItems(action.selectionItems)

            is MediaPickerAction.ShowMediaInfo -> showMediaInfo(action.video)

            is MediaPickerAction.DismissMediaInfo -> stateInternal.update {
                it.copy(mediaInfo = null)
            }

            is MediaPickerAction.CopySelectedItems -> transferSelectedItems(
                action.selectionItems,
                TransferMode.COPY,
            )

            is MediaPickerAction.MoveSelectedItems -> transferSelectedItems(
                action.selectionItems,
                TransferMode.MOVE,
            )

            is MediaPickerAction.CancelTransfer -> cancelTransfer()

            is MediaPickerAction.RequestHideSelectedItems -> requestHideSelectedItems(
                action.selectionItems,
            )

            is MediaPickerAction.SetPrivacyPinAndHide -> setPrivacyPinAndHide(action.pin)

            is MediaPickerAction.CompleteBiometricSetup -> completeBiometricSetup(action.enabled)

            is MediaPickerAction.ConfirmHidePendingItems -> confirmHidePendingItems()

            is MediaPickerAction.DismissHideFlow -> stateInternal.update {
                it.copy(hideFlow = HideFlowState.Idle)
            }

            is MediaPickerAction.MarkSelectedItems -> markSelectedItems(
                action.selectionItems,
                action.markType,
            )
        }
    }

    private fun markSelectedItems(selectionItems: Set<SelectionItem>, markType: MarkType) {
        viewModelScope.launch {
            val videos = selectionItems.toVideos()
            if (videos.isNotEmpty()) {
                mediaRepository.markVideos(uris = videos.map { it.uriString }, markType = markType)
            }
        }
    }

    private fun startMediaCollection() {
        mediaSynchronizer.startSync()
        collectMedia()
    }

    private fun collectMedia() {
        mediaCollectJob?.cancel()
        stateInternal.update { currentState ->
            currentState.copy(mediaDataState = DataState.Loading)
        }
        mediaCollectJob = viewModelScope.launch {
            launch {
                getSortedMediaUseCase(folderPath).collect { media ->
                    stateInternal.update {
                        it.copy(mediaDataState = DataState.Success(value = media))
                    }
                }
            }
            launch {
                getRecentlyPlayedVideoUseCase(folderPath).collect { recentlyPlayed ->
                    stateInternal.update { it.copy(recentlyPlayedVideo = recentlyPlayed) }
                }
            }
        }
    }

    private fun collectPreferences() {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect {
                stateInternal.update { currentState -> currentState.copy(preferences = it) }
            }
        }
    }

    private fun playSelectedItems(selectedItems: Set<SelectionItem>) {
        viewModelScope.launch {
            val videoUris = selectedItems.toVideoUris()
            output.playVideos(videoUris)
        }
    }

    private fun deleteSelectedItems(selectedItems: Set<SelectionItem>, permanently: Boolean) {
        viewModelScope.launch {
            val videoUris = selectedItems.toVideoUris()
            mediaOperationsService.deleteMedia(
                uris = videoUris,
                permanently = permanently,
            )
        }
    }

    private fun shareSelectedItems(selectedItems: Set<SelectionItem>) {
        viewModelScope.launch {
            val videoUris = selectedItems.toVideoUris()
            mediaOperationsService.shareMedia(uris = videoUris)
        }
    }

    private fun transferSelectedItems(selectedItems: Set<SelectionItem>, mode: TransferMode) {
        transferJob = viewModelScope.launch {
            val activeJob = currentCoroutineContext()[Job]
            val treeUri = systemService.pickFolder() ?: return@launch
            val videoUris = selectedItems.toVideoUris()
            if (videoUris.isEmpty()) return@launch

            stateInternal.update {
                it.copy(
                    transferFlow = TransferFlowState.Processing(
                        mode = mode,
                        progress = TransferProgress(totalFiles = videoUris.size),
                    ),
                )
            }

            mediaOperationsService.transferMedia(
                uris = videoUris,
                folderUri = treeUri,
                mode = mode,
            ).catch { error ->
                if (error is CancellationException) throw error
                showTransferCompleteToast(
                    mode = mode,
                    result = TransferResult(
                        succeeded = 0,
                        failed = videoUris.size,
                    ),
                )
            }.onCompletion {
                if (transferJob === activeJob) {
                    stateInternal.update { it.copy(transferFlow = TransferFlowState.Idle) }
                }
            }.collect { event ->
                when (event) {
                    is TransferEvent.Progress -> stateInternal.update {
                        (it.transferFlow as? TransferFlowState.Processing)?.let { state ->
                            it.copy(transferFlow = state.copy(progress = event.progress))
                        } ?: it
                    }

                    is TransferEvent.Completed -> {
                        stateInternal.update { it.copy(transferFlow = TransferFlowState.Idle) }
                        showTransferCompleteToast(
                            mode = mode,
                            event.result,
                        )
                    }
                }
            }
        }
    }

    private fun showTransferCompleteToast(mode: TransferMode, result: TransferResult) {
        val message = when {
            result.sameFolderSkipped > 0 && result.succeeded == 0 && result.failed == 0 ->
                systemService.getString(R.string.cannot_move_to_same_folder)

            result.failed > 0 -> systemService.getQuantityString(
                pluralsResId = if (mode == TransferMode.MOVE) {
                    R.plurals.move_failed
                } else {
                    R.plurals.copy_failed
                },
                result.failed,
                result.failed,
            )

            result.originalsNotDeleted -> systemService.getQuantityString(
                R.plurals.moved_videos_originals_remain,
                result.succeeded,
                result.succeeded,
            )

            mode == TransferMode.MOVE -> systemService.getQuantityString(
                R.plurals.moved_videos_result,
                result.succeeded,
                result.succeeded,
            )

            else -> systemService.getQuantityString(
                R.plurals.copied_videos_result,
                result.succeeded,
                result.succeeded,
            )
        }
        systemService.showToast(
            text = message,
            Toast.LENGTH_SHORT,
        )
    }

    private fun cancelTransfer() {
        transferJob?.cancel()
        transferJob = null
        stateInternal.update { it.copy(transferFlow = TransferFlowState.Idle) }
    }

    private fun showMediaInfo(video: Video) {
        viewModelScope.launch {
            val mediaInfo = mediaRepository.getMediaInfo(video.uriString)
            if (mediaInfo != null) {
                stateInternal.update { it.copy(mediaInfo = mediaInfo) }
            }
        }
    }

    private fun renameVideo(uri: Uri, to: String) {
        viewModelScope.launch {
            mediaOperationsService.renameMedia(
                uri = uri,
                to = to,
            )
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            stateInternal.update { it.copy(refreshing = true) }
            mediaSynchronizer.refresh()
            stateInternal.update { it.copy(refreshing = false) }
        }
    }

    private fun updateMenu(preferences: ApplicationPreferences) {
        viewModelScope.launch { preferencesRepository.updateApplicationPreferences { preferences } }
    }

    private fun requestHideSelectedItems(selectedItems: Set<SelectionItem>) {
        viewModelScope.launch {
            val videoItems = selectedItems.toVideos()
            if (videoItems.isEmpty()) return@launch
            val hasPin = privacyPinRepository.hasPinSet()
            when {
                !hasPin -> {
                    stateInternal.update {
                        it.copy(hideFlow = HideFlowState.SetupPin(items = videoItems))
                    }
                }

                !privacyPinRepository.hasShownHideConfirmation() -> {
                    stateInternal.update {
                        it.copy(hideFlow = HideFlowState.ConfirmHide(items = videoItems))
                    }
                }

                else -> {
                    hideVideoItems(videos = videoItems)
                }
            }
        }
    }

    private fun setPrivacyPinAndHide(pin: String) {
        val pending = (stateInternal.value.hideFlow as? HideFlowState.SetupPin)?.items ?: return
        viewModelScope.launch {
            privacyPinRepository.setPin(pin = pin)
            hideVideoItems(videos = pending)
            privacyPinRepository.setHideConfirmationShown()
            stateInternal.update { it.copy(hideFlow = HideFlowState.BiometricSetup) }
        }
    }

    private fun completeBiometricSetup(enabled: Boolean) {
        if (stateInternal.value.hideFlow != HideFlowState.BiometricSetup) return
        viewModelScope.launch {
            privacyPinRepository.setBiometricEnabled(enabled = enabled)
            stateInternal.update { it.copy(hideFlow = HideFlowState.Idle) }
        }
    }

    private fun confirmHidePendingItems() {
        val pending = (stateInternal.value.hideFlow as? HideFlowState.ConfirmHide)?.items ?: return
        viewModelScope.launch {
            hideVideoItems(videos = pending)
            privacyPinRepository.setHideConfirmationShown()
            stateInternal.update { it.copy(hideFlow = HideFlowState.Idle) }
        }
    }

    private suspend fun hideVideoItems(videos: List<Video>) {
        stateInternal.update { it.copy(hideFlow = HideFlowState.Processing) }
        privacyRepository.hideVideos(videos = videos)
        stateInternal.update { it.copy(hideFlow = HideFlowState.Idle) }
    }

    private suspend fun Set<SelectionItem>.toVideos(): List<Video> {
        val preferences = stateInternal.value.preferences
        return flatMap { selectionItem ->
            when (selectionItem) {
                is SelectionItem.Video -> listOfNotNull(
                    mediaRepository.getVideoByUri(selectionItem.uriString),
                )

                is SelectionItem.Folder -> {
                    val videos = getSortedVideosUseCase(selectionItem.path).first()
                    val filteredVideos = if (preferences.mediaViewMode == MediaViewMode.FOLDERS) {
                        videos.filter { it.parentPath == selectionItem.path }
                    } else {
                        videos
                    }
                    filteredVideos
                }
            }
        }.distinctBy(selector = Video::uriString)
    }

    private suspend fun Set<SelectionItem>.toVideoUris(): List<Uri> = toVideos().map {
        it.uriString.toUri()
    }
}

@Stable
data class MediaPickerUiState(
    val folderName: String?,
    val refreshing: Boolean = false,
    val recentlyPlayedVideo: Video? = null,
    val mediaDataState: DataState<MediaHolder?> = DataState.Loading,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
    val mediaInfo: com.sipun.playora.core.model.MediaInfo? = null,
    val hideFlow: HideFlowState = HideFlowState.Idle,
    val transferFlow: TransferFlowState = TransferFlowState.Idle,
) {
    val recentlyPlayedFolder: Folder?
        get() = recentlyPlayedVideo?.let { video ->
            (mediaDataState as? DataState.Success)?.value?.folders?.findClosestFolder(video.path)
        }
}

sealed interface TransferFlowState {
    data object Idle : TransferFlowState
    data class Processing(val mode: TransferMode, val progress: TransferProgress) :
        TransferFlowState
}

sealed interface HideFlowState {
    data object Idle : HideFlowState
    data class ConfirmHide(val items: List<Video>) : HideFlowState
    data class SetupPin(val items: List<Video>) : HideFlowState
    data object BiometricSetup : HideFlowState

    data object Processing : HideFlowState
}

sealed interface MediaPickerAction {
    data object OnNavigateUpClick : MediaPickerAction
    data class OnPlayVideo(val uri: Uri, val playlist: List<Uri>? = null) : MediaPickerAction
    data class OnFolderClick(val folderPath: String) : MediaPickerAction
    data object OnSettingsClick : MediaPickerAction
    data object OnSearchClick : MediaPickerAction
    data object OnPrivacyClick : MediaPickerAction
    data object Refresh : MediaPickerAction
    data class RenameVideo(val uri: Uri, val to: String) : MediaPickerAction
    data class UpdateMenu(val preferences: ApplicationPreferences) : MediaPickerAction
    data object OnPermissionAccepted : MediaPickerAction
    data class PlaySelectedItems(val selectionItems: Set<SelectionItem>) : MediaPickerAction
    data class DeleteSelectedItems(
        val selectionItems: Set<SelectionItem>,
        val permanently: Boolean = false,
    ) : MediaPickerAction

    data class ShareSelectedItems(val selectionItems: Set<SelectionItem>) : MediaPickerAction
    data class CopySelectedItems(val selectionItems: Set<SelectionItem>) : MediaPickerAction
    data class MoveSelectedItems(val selectionItems: Set<SelectionItem>) : MediaPickerAction
    data object CancelTransfer : MediaPickerAction
    data class ShowMediaInfo(val video: Video) : MediaPickerAction
    data object DismissMediaInfo : MediaPickerAction
    data class RequestHideSelectedItems(val selectionItems: Set<SelectionItem>) : MediaPickerAction
    data class SetPrivacyPinAndHide(val pin: String) : MediaPickerAction
    data class CompleteBiometricSetup(val enabled: Boolean) : MediaPickerAction
    data object ConfirmHidePendingItems : MediaPickerAction
    data object DismissHideFlow : MediaPickerAction
    data class MarkSelectedItems(
        val selectionItems: Set<SelectionItem>,
        val markType: MarkType,
    ) : MediaPickerAction
}
