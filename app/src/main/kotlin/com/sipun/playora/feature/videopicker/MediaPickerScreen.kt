package com.sipun.playora.feature.videopicker.screens.mediapicker

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sipun.playora.R
import com.sipun.playora.core.common.storagePermission
import com.sipun.playora.core.domain.MediaHolder
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.media.services.TransferMode
import com.sipun.playora.core.media.services.TransferProgress
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.model.MediaViewMode
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.ui.base.DataState
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.DeleteButton
import com.sipun.playora.core.ui.components.DestructiveRed
import com.sipun.playora.core.ui.components.TextIconToggleButton
import com.sipun.playora.core.ui.composables.PermissionMissingView
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.preview.DayNightPreview
import com.sipun.playora.core.ui.preview.VideoPickerPreviewParameterProvider
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.videopicker.composables.CenterCircularProgressBar
import com.sipun.playora.feature.videopicker.composables.MarkAsDialog
import com.sipun.playora.feature.videopicker.composables.MediaInfoDialog
import com.sipun.playora.feature.videopicker.composables.MediaView
import com.sipun.playora.feature.videopicker.composables.NoVideosFound
import com.sipun.playora.feature.videopicker.composables.QuickSettingsDialog
import com.sipun.playora.feature.videopicker.composables.RenameDialog
import com.sipun.playora.feature.videopicker.composables.SelectionAction
import com.sipun.playora.feature.videopicker.composables.privacy.PinDotsIndicator
import com.sipun.playora.feature.videopicker.composables.privacy.PinKeypad
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacyBiometricSetupDialog
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacyProgressDialog
import com.sipun.playora.feature.videopicker.privacy.PRIVACY_PIN_LENGTH
import com.sipun.playora.feature.videopicker.state.SelectionItem
import com.sipun.playora.feature.videopicker.state.rememberSelectionManager
import com.sipun.playora.feature.videopicker.state.toSelectedFolder
import com.sipun.playora.feature.videopicker.state.toSelectedVideo
import kotlin.math.roundToInt

@Composable
fun MediaPickerScreen(viewModel: MediaPickerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED,
    )

    MediaPickerScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalPermissionsApi::class,
)
@Composable
internal fun MediaPickerScreenContent(
    state: MediaPickerUiState,
    onAction: (MediaPickerAction) -> Unit = {},
) {
    val selectionManager = rememberSelectionManager()
    val permissionState = rememberPermissionState(permission = storagePermission)
    var wasPermissionGranted by remember { mutableStateOf(permissionState.status.isGranted) }

    LaunchedEffect(permissionState.status.isGranted) {
        val isPermissionGranted = permissionState.status.isGranted
        if (isPermissionGranted && !wasPermissionGranted) {
            onAction(MediaPickerAction.OnPermissionAccepted)
        }
        wasPermissionGranted = isPermissionGranted
    }
    val lazyGridState = rememberLazyGridState()
    val selectVideoFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onAction(MediaPickerAction.OnPlayVideo(uri = it)) } },
    )

    var showQuickSettingsDialog by rememberSaveable { mutableStateOf(value = false) }
    var showRenameActionFor: Video? by rememberSaveable { mutableStateOf(value = null) }
    var showDeleteVideosConfirmation by rememberSaveable { mutableStateOf(value = false) }
    var showHideVideosConfirmation by rememberSaveable { mutableStateOf(value = false) }
    var showMarkDialog by rememberSaveable { mutableStateOf(value = false) }

    val mediaHolder = (state.mediaDataState as? DataState.Success)?.value
    val onFabClick = {
        val selectedItem = state.recentlyPlayedVideo?.toSelectedVideo()
            ?: mediaHolder?.folders?.firstOrNull()?.toSelectedFolder()
            ?: mediaHolder?.videos?.firstOrNull()?.toSelectedVideo()

        when (selectedItem) {
            is SelectionItem.Video -> {
                val playlist = mediaHolder?.videos?.map { it.uriString.toUri() }
                onAction(
                    MediaPickerAction.OnPlayVideo(
                        uri = selectedItem.uriString.toUri(),
                        playlist = playlist,
                    ),
                )
            }

            is SelectionItem.Folder -> onAction(
                MediaPickerAction.PlaySelectedItems(selectionItems = setOf(element = selectedItem)),
            )

            null -> selectVideoFileLauncher.launch(input = "video/*")
        }
    }

    val selectedItemsSize = selectionManager.selectionItems.size
    val totalItemsSize =
        (state.mediaDataState as? DataState.Success)?.value?.run { folders.size + videos.size } ?: 0

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = {
                    val isRoot = state.folderName == null
                    val titleText = (state.folderName ?: stringResource(R.string.app_name))
                        .takeIf { !selectionManager.isInSelectionMode } ?: ""
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold.takeIf { isRoot },
                        modifier = if (isRoot && !selectionManager.isInSelectionMode) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAction(MediaPickerAction.OnPrivacyClick) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        } else if (!isRoot) {
                            Modifier.padding(start = 12.dp)
                        } else {
                            Modifier
                        },
                    )
                },
                navigationIcon = {
                    if (selectionManager.isInSelectionMode) {
                        Row(
                            modifier = Modifier
                                .clip(shape = CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { selectionManager.exitSelectionMode() }
                                .padding(8.dp)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Close,
                                contentDescription = stringResource(id = R.string.navigate_up),
                            )
                            Text(
                                text = stringResource(
                                    R.string.m_n_selected,
                                    selectedItemsSize,
                                    totalItemsSize,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    } else if (state.folderName != null) {
                        AppBackButton(
                            onClick = { onAction(MediaPickerAction.OnNavigateUpClick) },
                        )
                    }
                },
                actions = {
                    if (selectionManager.isInSelectionMode) {
                        FilledTonalIconButton(
                            onClick = {
                                if (selectedItemsSize != totalItemsSize) {
                                    val folder = (state.mediaDataState as? DataState.Success)?.value
                                    if (folder != null) {
                                        folder.folders.forEach {
                                            selectionManager.selectFolder(folder = it)
                                        }
                                        folder.videos.forEach {
                                            selectionManager.selectVideo(video = it)
                                        }
                                    }
                                } else {
                                    selectionManager.clearSelection()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (selectedItemsSize != totalItemsSize) {
                                    AppIcons.SelectAll
                                } else {
                                    AppIcons.DeselectAll
                                },
                                contentDescription = if (selectedItemsSize != totalItemsSize) {
                                    stringResource(R.string.select_all)
                                } else {
                                    stringResource(R.string.deselect_all)
                                },
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onAction(MediaPickerAction.OnSearchClick) },
                        ) {
                            Icon(
                                imageVector = AppIcons.SearchOutline,
                                contentDescription = stringResource(id = R.string.search),
                            )
                        }
                        IconButton(
                            onClick = { showQuickSettingsDialog = true },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_split_panes),
                                contentDescription = stringResource(id = R.string.menu),
                            )
                        }
                        IconButton(
                            onClick = { onAction(MediaPickerAction.OnSettingsClick) },
                        ) {
                            Icon(
                                imageVector = AppIcons.SettingsOutline,
                                contentDescription = stringResource(id = R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            SelectionActionsSheet(
                show =
                    selectionManager.isInSelectionMode &&
                            selectionManager.selectionItems.isNotEmpty(),
                showRenameAction = selectionManager.isSingleVideoSelected,
                showInfoAction = selectionManager.isSingleVideoSelected,
                showHideAction = selectionManager.selectionItems.isNotEmpty(),
                onPlayAction = {
                    onAction(MediaPickerAction.PlaySelectedItems(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onRenameAction = {
                    val selectedVideo =
                        selectionManager.selectionItems.firstOrNull()
                            ?: return@SelectionActionsSheet
                    val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                        ?.find { it.uriString == selectedVideo.id } ?: return@SelectionActionsSheet
                    showRenameActionFor = video
                },
                onInfoAction = {
                    val selectedVideo =
                        selectionManager.selectionItems.firstOrNull()
                            ?: return@SelectionActionsSheet
                    val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                        ?.find { it.uriString == selectedVideo.id } ?: return@SelectionActionsSheet
                    onAction(MediaPickerAction.ShowMediaInfo(video = video))
                    selectionManager.exitSelectionMode()
                },
                onShareAction = {
                    onAction(MediaPickerAction.ShareSelectedItems(selectionManager.selectionItems))
                },
                onCopyAction = {
                    onAction(MediaPickerAction.CopySelectedItems(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onMoveAction = {
                    onAction(MediaPickerAction.MoveSelectedItems(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onHideAction = { showHideVideosConfirmation = true },
                onMarkAction = { showMarkDialog = true },
                onDeleteAction = { showDeleteVideosConfirmation = true },
            )
        },
        floatingActionButton = {
            if (!selectionManager.isInSelectionMode) {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        imageVector = AppIcons.Play,
                        contentDescription = stringResource(R.string.play),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { scaffoldPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding.copy(bottom = 0.dp))
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                    ),
                )
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (!permissionState.status.isGranted) {
                PermissionMissingView(
                    isGranted = permissionState.status.isGranted,
                    showRationale = permissionState.status.shouldShowRationale,
                    permission = permissionState.permission,
                    launchPermissionRequest = { permissionState.launchPermissionRequest() },
                ) {}
                return@Scaffold
            }
            when (state.mediaDataState) {
                is DataState.Error -> {
                }

                is DataState.Loading -> {
                    CenterCircularProgressBar()
                }

                is DataState.Success -> {
                    val fabBottomPadding = if (!selectionManager.isInSelectionMode) 88.dp else 0.dp
                    val updatedScaffoldPadding = scaffoldPadding.copy(
                        top = 0.dp,
                        start = 0.dp,
                        bottom = scaffoldPadding.calculateBottomPadding() + fabBottomPadding,
                    )
                    val mediaHolder = state.mediaDataState.value
                    PullToRefreshBox(
                        isRefreshing = state.refreshing,
                        onRefresh = { onAction(MediaPickerAction.Refresh) },
                    ) {
                        if (mediaHolder == null ||
                            (mediaHolder.folders.isEmpty() && mediaHolder.videos.isEmpty())
                        ) {
                            NoVideosFound(contentPadding = updatedScaffoldPadding)
                        } else {
                            MediaView(
                                recentlyPlayedVideo = state.recentlyPlayedVideo,
                                recentlyPlayedFolder = state.recentlyPlayedFolder,
                                mediaHolder = mediaHolder,
                                preferences = state.preferences,
                                onFolderClick = {
                                    onAction(MediaPickerAction.OnFolderClick(folderPath = it))
                                },
                                onVideoClick = { clickedUri ->
                                    val playlist = mediaHolder.videos.map { it.uriString.toUri() }
                                    onAction(
                                        MediaPickerAction.OnPlayVideo(
                                            uri = clickedUri,
                                            playlist = playlist,
                                        ),
                                    )
                                },
                                selectionManager = selectionManager,
                                lazyGridState = lazyGridState,
                                contentPadding = updatedScaffoldPadding,
                            )
                        }
                    }
                }
            }
        }
    }

    BackHandler(enabled = selectionManager.isInSelectionMode) {
        selectionManager.exitSelectionMode()
    }

    if (showQuickSettingsDialog) {
        QuickSettingsDialog(
            applicationPreferences = state.preferences,
            onDismiss = { showQuickSettingsDialog = false },
            updatePreferences = { onAction(MediaPickerAction.UpdateMenu(preferences = it)) },
        )
    }

    if (showMarkDialog) {
        MarkAsDialog(
            onDismiss = { showMarkDialog = false },
            onConfirm = { markType ->
                onAction(
                    MediaPickerAction.MarkSelectedItems(
                        selectionManager.selectionItems,
                        markType,
                    ),
                )
                selectionManager.exitSelectionMode()
                showMarkDialog = false
            },
        )
    }

    showRenameActionFor?.let { video ->
        RenameDialog(
            name = video.displayName,
            onDismiss = { showRenameActionFor = null },
            onDone = {
                onAction(
                    MediaPickerAction.RenameVideo(
                        video.uriString.toUri(),
                        to = it,
                    ),
                )
                selectionManager.exitSelectionMode()
                showRenameActionFor = null
            },
        )
    }

    state.mediaInfo?.let { mediaInfo ->
        MediaInfoDialog(
            mediaInfo = mediaInfo,
            onDismiss = { onAction(MediaPickerAction.DismissMediaInfo) },
        )
    }

    if (showDeleteVideosConfirmation) {
        DeleteConfirmationDialog(
            selectionItems = selectionManager.selectionItems,
            trashSupported = MediaOperationsService.supportsTrash(),
            onConfirm = { permanently ->
                onAction(
                    MediaPickerAction.DeleteSelectedItems(
                        selectionManager.selectionItems,
                        permanently = permanently,
                    ),
                )
                selectionManager.exitSelectionMode()
                showDeleteVideosConfirmation = false
            },
            onCancel = { showDeleteVideosConfirmation = false },
        )
    }

    if (showHideVideosConfirmation) {
        val count = selectionManager.selectionItems.size
        AppDialog(
            onDismissRequest = { showHideVideosConfirmation = false },
            title = {
                Text(
                    text = if (count == 1) {
                        stringResource(R.string.hide_one_video_confirmation)
                    } else {
                        stringResource(
                            R.string.hide_videos_confirmation,
                            count,
                        )
                    },
                )
            },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.hide_video_info))
                    Text(
                        text = stringResource(R.string.hide_video_uninstall_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(
                            MediaPickerAction.RequestHideSelectedItems(
                                selectionManager.selectionItems,
                            ),
                        )
                        selectionManager.exitSelectionMode()
                        showHideVideosConfirmation = false
                    },
                ) {
                    Text(text = stringResource(R.string.hide))
                }
            },
            dismissButton = {
                CancelButton(onClick = { showHideVideosConfirmation = false })
            },
        )
    }

    HideFlowDialogs(
        hideFlow = state.hideFlow,
        onConfirmHide = { onAction(MediaPickerAction.ConfirmHidePendingItems) },
        onSetPinAndHide = { onAction(MediaPickerAction.SetPrivacyPinAndHide(pin = it)) },
        onBiometricSetupComplete = {
            onAction(MediaPickerAction.CompleteBiometricSetup(enabled = it))
        },
        onDismiss = { onAction(MediaPickerAction.DismissHideFlow) },
    )

    (state.transferFlow as? TransferFlowState.Processing)?.let { transfer ->
        TransferProgressDialog(
            mode = transfer.mode,
            progress = transfer.progress,
            onCancel = { onAction(MediaPickerAction.CancelTransfer) },
        )
    }

}

@Composable
private fun TransferProgressDialog(
    mode: TransferMode,
    progress: TransferProgress,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = when (mode) {
                        TransferMode.COPY -> stringResource(R.string.copying_videos_in_progress)
                        TransferMode.MOVE -> stringResource(R.string.moving_videos_in_progress)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                ProgressSection(
                    label = progress.currentName.orEmpty(),
                    fraction = progress.currentFraction,
                )

                if (progress.totalFiles > 1) {
                    ProgressSection(
                        label = stringResource(
                            R.string.transfer_file_progress,
                            progress.currentIndex + 1,
                            progress.totalFiles,
                        ),
                        fraction = progress.overallFraction,
                    )
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.End),
                ) { Text(text = stringResource(R.string.cancel)) }
            }
        }
    }
}

@Composable
private fun ProgressSection(label: String, fraction: Float?) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction ?: 0f,
        label = "progress",
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(weight = 1f),
            )
            if (fraction != null) {
                Text(
                    text = "${(fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val barModifier = Modifier
            .fillMaxWidth()
            .clip(shape = CircleShape)
        if (fraction != null) {
            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = barModifier,
            )
        } else {
            LinearProgressIndicator(modifier = barModifier)
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    modifier: Modifier = Modifier,
    selectionItems: Set<SelectionItem>,
    trashSupported: Boolean,
    onConfirm: (Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    val selectedVideos = selectionItems.filterIsInstance<SelectionItem.Video>()
    val selectedFolders = selectionItems.filterIsInstance<SelectionItem.Folder>()
    var permanently by rememberSaveable { mutableStateOf(value = false) }

    AppDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = when {
                    selectedVideos.isEmpty() -> when (selectedFolders.size) {
                        1 -> stringResource(R.string.delete_one_folder)

                        else -> stringResource(
                            R.string.delete_folders,
                            selectedFolders.size,
                        )
                    }

                    selectedFolders.isEmpty() -> when (selectedVideos.size) {
                        1 -> stringResource(R.string.delete_one_video)

                        else -> stringResource(
                            R.string.delete_videos,
                            selectedVideos.size,
                        )
                    }

                    else -> stringResource(
                        R.string.delete_items,
                        selectedFolders.size + selectedVideos.size,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            DeleteButton(
                onClick = { onConfirm(permanently || !trashSupported) },
                modifier = modifier,
                text = stringResource(
                    id = if (permanently || !trashSupported) {
                        R.string.delete
                    } else {
                        R.string.move_to_trash
                    },
                ),
            )
        },
        dismissButton = { CancelButton(onClick = onCancel) },
        modifier = modifier,
        content = {
            Text(
                text = if (permanently || !trashSupported) {
                    if ((selectedFolders.size + selectedVideos.size) == 1) {
                        stringResource(R.string.delete_item_info)
                    } else {
                        stringResource(R.string.delete_items_info)
                    }
                } else {
                    stringResource(R.string.move_to_trash_info)
                },
                style = MaterialTheme.typography.titleSmall,
            )
            if (trashSupported) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = permanently,
                        onCheckedChange = { permanently = it },
                    )
                    Text(text = stringResource(R.string.delete_permanently))
                }
            }
        },
    )
}

@Composable
private fun HideFlowDialogs(
    hideFlow: HideFlowState,
    onConfirmHide: () -> Unit,
    onSetPinAndHide: (String) -> Unit,
    onBiometricSetupComplete: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    when (hideFlow) {
        HideFlowState.Idle -> Unit

        HideFlowState.Processing -> {
            PrivacyProgressDialog(message = stringResource(R.string.hiding_videos_in_progress))
        }

        is HideFlowState.ConfirmHide -> {
            val count = hideFlow.items.size
            AppDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = if (count == 1) {
                            stringResource(R.string.hide_one_video_confirmation)
                        } else {
                            stringResource(
                                R.string.hide_videos_confirmation,
                                count,
                            )
                        },
                    )
                },
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = stringResource(R.string.hide_video_info))
                        Text(
                            text = stringResource(R.string.hide_video_uninstall_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = onConfirmHide) {
                        Text(text = stringResource(R.string.hide))
                    }
                },
                dismissButton = { CancelButton(onClick = onDismiss) },
            )
        }

        is HideFlowState.SetupPin -> {
            SetupPrivacyPinDialog(
                onDismiss = onDismiss,
                onPinConfirmed = onSetPinAndHide,
            )
        }

        HideFlowState.BiometricSetup -> PrivacyBiometricSetupDialog(
            onComplete = onBiometricSetupComplete,
        )
    }
}

@Composable
private fun SetupPrivacyPinDialog(onDismiss: () -> Unit, onPinConfirmed: (String) -> Unit) {
    var firstPin: String? by rememberSaveable { mutableStateOf(value = null) }
    var currentPin by rememberSaveable { mutableStateOf(value = "") }
    var errorCount by rememberSaveable { mutableStateOf(value = 0) }

    val isConfirmStep = firstPin != null
    val showError = errorCount > 0

    val onDigit: (Char) -> Unit = { digit ->
        if (currentPin.length < PRIVACY_PIN_LENGTH) {
            currentPin += digit
            if (currentPin.length == PRIVACY_PIN_LENGTH) {
                val pinJustEntered = currentPin
                if (!isConfirmStep) {
                    firstPin = pinJustEntered
                    currentPin = ""
                    errorCount = 0
                } else if (pinJustEntered == firstPin) {
                    onPinConfirmed(pinJustEntered)
                } else {
                    errorCount++
                    currentPin = ""
                }
            }
        }
    }
    val onBackspace: () -> Unit = {
        if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(n = 1)
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 28.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SetupPinHeader(
                    isConfirmStep = isConfirmStep,
                    filledCount = currentPin.length,
                    showError = showError,
                )
                Spacer(modifier = Modifier.height(32.dp))
                PinKeypad(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                )
                Spacer(modifier = Modifier.height(16.dp))
                CancelButton(onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun SetupPinHeader(
    isConfirmStep: Boolean,
    filledCount: Int,
    showError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isConfirmStep) {
                stringResource(R.string.confirm_privacy_pin)
            } else {
                stringResource(R.string.set_privacy_pin)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isConfirmStep) {
                stringResource(R.string.confirm_privacy_pin_description)
            } else {
                stringResource(R.string.set_privacy_pin_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        PinDotsIndicator(
            filledCount = filledCount,
            error = showError,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (showError) {
            Text(
                text = stringResource(R.string.pins_do_not_match),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectionActionsSheet(
    modifier: Modifier = Modifier,
    show: Boolean,
    showRenameAction: Boolean,
    showInfoAction: Boolean,
    showHideAction: Boolean,
    onPlayAction: () -> Unit,
    onRenameAction: () -> Unit,
    onShareAction: () -> Unit,
    onCopyAction: () -> Unit,
    onMoveAction: () -> Unit,
    onInfoAction: () -> Unit,
    onHideAction: () -> Unit,
    onMarkAction: () -> Unit,
    onDeleteAction: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier.windowInsetsPadding(
            WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
        ),
        visible = show,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
    ) {
        val shape = MaterialTheme.shapes.largeIncreased.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = shape,
                    )
                    .clip(shape = shape)
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 4.dp,
                        vertical = 12.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SelectionAction(
                    imageVector = AppIcons.Play,
                    title = stringResource(R.string.play),
                    onClick = onPlayAction,
                    modifier = Modifier.weight(1f),
                )
                if (showRenameAction) {
                    SelectionAction(
                        imageVector = AppIcons.Edit,
                        title = stringResource(R.string.rename),
                        onClick = onRenameAction,
                        modifier = Modifier.weight(1f),
                    )
                }
                SelectionAction(
                    imageVector = AppIcons.Share,
                    title = stringResource(R.string.share),
                    onClick = onShareAction,
                    modifier = Modifier.weight(1f),
                )
                SelectionAction(
                    imageVector = AppIcons.Copy,
                    title = stringResource(R.string.copy),
                    onClick = onCopyAction,
                    modifier = Modifier.weight(1f),
                )
                SelectionAction(
                    imageVector = AppIcons.Move,
                    title = stringResource(R.string.move),
                    onClick = onMoveAction,
                    modifier = Modifier.weight(1f),
                )
                SelectionAction(
                    imageVector = AppIcons.Check,
                    title = stringResource(R.string.mark),
                    onClick = onMarkAction,
                    modifier = Modifier.weight(1f),
                )
                if (showInfoAction) {
                    SelectionAction(
                        imageVector = AppIcons.Info,
                        title = stringResource(id = R.string.info),
                        onClick = onInfoAction,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (showHideAction) {
                    SelectionAction(
                        imageVector = AppIcons.HideSource,
                        title = stringResource(id = R.string.hide),
                        onClick = onHideAction,
                        modifier = Modifier.weight(1f),
                    )
                }
                SelectionAction(
                    imageVector = AppIcons.Delete,
                    title = stringResource(id = R.string.delete),
                    tint = DestructiveRed,
                    onClick = onDeleteAction,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun MediaPickerScreenPreview(
    @PreviewParameter(VideoPickerPreviewParameterProvider::class)
    videos: List<Video>,
) {
    PlayoraTheme {
        MediaPickerScreenContent(
            state = MediaPickerUiState(
                folderName = null,
                mediaDataState = DataState.Success(
                    value = MediaHolder(
                        folders = listOf(
                            Folder(
                                name = "Folder 1",
                                path = "/root/folder1",
                                dateModified = System.currentTimeMillis(),
                            ),
                            Folder(
                                name = "Folder 2",
                                path = "/root/folder2",
                                dateModified = System.currentTimeMillis(),
                            ),
                        ),
                        videos = videos,
                    ),
                ),
                preferences = ApplicationPreferences().copy(
                    mediaViewMode = MediaViewMode.FOLDER_TREE,
                    mediaLayoutMode = MediaLayoutMode.GRID,
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    Surface {
        TextIconToggleButton(
            text = "Title",
            icon = AppIcons.Title,
            onClick = {},
        )
    }
}

@DayNightPreview
@Composable
private fun MediaPickerNoVideosFoundPreview() {
    PlayoraTheme {
        Surface {
            MediaPickerScreenContent(
                state = MediaPickerUiState(
                    folderName = null,
                    mediaDataState = DataState.Success(value = null),
                    preferences = ApplicationPreferences(),
                ),
            )
        }
    }
}

@DayNightPreview
@Composable
private fun MediaPickerLoadingPreview() {
    PlayoraTheme {
        Surface {
            MediaPickerScreenContent(
                state = MediaPickerUiState(
                    folderName = null,
                    mediaDataState = DataState.Loading,
                    preferences = ApplicationPreferences(),
                ),
            )
        }
    }
}
