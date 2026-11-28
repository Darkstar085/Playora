package com.sipun.playora.feature.videopicker.screens.mediapicker

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.sipun.playora.R
import com.sipun.playora.core.common.storagePermission
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.media.services.TransferMode
import com.sipun.playora.core.media.services.TransferProgress
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.ui.base.DataState
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.DeleteButton
import com.sipun.playora.core.ui.components.DestructiveRed
import com.sipun.playora.core.ui.composables.PermissionMissingView
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.AppGlassModalOverlay
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassContainer
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.rememberGlassBarBorderStroke
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
import com.sipun.playora.feature.videopicker.composables.CenterCircularProgressBar
import com.sipun.playora.feature.videopicker.composables.MarkAsDialog
import com.sipun.playora.feature.videopicker.composables.MediaInfoDialog
import com.sipun.playora.feature.videopicker.composables.MediaView
import com.sipun.playora.feature.videopicker.composables.NoVideosFound
import com.sipun.playora.feature.videopicker.composables.QuickSettingsDialog
import io.github.itshedimisawi.vitro.rememberGlassBackdrop
import io.github.itshedimisawi.vitro.rememberGlassEffectState
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
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalPermissionsApi::class)
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

    val isRoot = state.folderName == null
    var showVaultTooltip by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.preferences.hasSeenVaultHint, isRoot) {
        if (!state.preferences.hasSeenVaultHint && isRoot) {
            delay(800)
            showVaultTooltip = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            repeat(3) {
                shakeOffset.animateTo(-7f, animationSpec = tween(50, easing = LinearEasing))
                shakeOffset.animateTo(7f, animationSpec = tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(
                0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            delay(5500)
            showVaultTooltip = false
            onAction(MediaPickerAction.UpdateMenu(state.preferences.copy(hasSeenVaultHint = true)))
        }
    }

    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val backdrop = rememberGlassBackdrop()
    val pullToRefreshState = rememberPullToRefreshState()

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) backdrop else null,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = {
                        val titleText = (state.folderName ?: stringResource(R.string.app_name))
                            .takeIf { !selectionManager.isInSelectionMode } ?: ""
                        if (titleText.isNotEmpty()) {
                            val titleGlassState = rememberGlassEffectState()
                            val titleInteractionSource = remember { MutableInteractionSource() }
                            val titleContentColor = rememberGlassContentColor(
                                state = titleGlassState,
                                fallback = MaterialTheme.colorScheme.onSurface,
                            )
                            Box {
                                Text(
                                    text = titleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold.takeIf { isRoot },
                                    color = if (liquidGlassEnabled) {
                                        titleContentColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = if (isRoot && !selectionManager.isInSelectionMode) {
                                        Modifier
                                            .graphicsLayer { translationX = shakeOffset.value }
                                            .then(
                                                if (liquidGlassEnabled) {
                                                    Modifier
                                                        .appGlassSurface(
                                                            shape = CircleShape,
                                                            interactive = true,
                                                            state = titleGlassState,
                                                            interactionSource = titleInteractionSource,
                                                        )
                                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                                } else {
                                                    Modifier.clip(RoundedCornerShape(8.dp))
                                                },
                                            )
                                            .clickable(
                                                interactionSource = titleInteractionSource,
                                                indication = if (liquidGlassEnabled) {
                                                    null
                                                } else {
                                                    androidx.compose.material3.ripple()
                                                },
                                            ) {
                                                showVaultTooltip = false
                                                if (!state.preferences.hasSeenVaultHint) {
                                                    onAction(
                                                        MediaPickerAction.UpdateMenu(
                                                            state.preferences.copy(
                                                                hasSeenVaultHint = true,
                                                            ),
                                                        ),
                                                    )
                                                }
                                                onAction(MediaPickerAction.OnPrivacyClick)
                                            }
                                            .padding(
                                                horizontal = if (liquidGlassEnabled) 14.dp else 4.dp,
                                                vertical = if (liquidGlassEnabled) 6.dp else 2.dp,
                                            )
                                    } else {
                                        Modifier
                                            .graphicsLayer { translationX = shakeOffset.value }
                                            .padding(start = 12.dp)
                                            .then(
                                                if (liquidGlassEnabled) {
                                                    Modifier
                                                        .appGlassSurface(
                                                            shape = CircleShape,
                                                            state = titleGlassState,
                                                        )
                                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                                        .padding(
                                                            horizontal = 14.dp,
                                                            vertical = 6.dp,
                                                        )
                                                } else {
                                                    Modifier
                                                },
                                            )
                                    },
                                )

                                if (showVaultTooltip && !selectionManager.isInSelectionMode) {
                                    Popup(
                                        alignment = Alignment.BottomStart,
                                        offset = IntOffset(
                                            x = 0,
                                            y = with(LocalDensity.current) { 6.dp.roundToPx() },
                                        ),
                                        onDismissRequest = {
                                            showVaultTooltip = false
                                            if (!state.preferences.hasSeenVaultHint) {
                                                onAction(
                                                    MediaPickerAction.UpdateMenu(
                                                        state.preferences.copy(
                                                            hasSeenVaultHint = true,
                                                        ),
                                                    ),
                                                )
                                            }
                                        },
                                        properties = PopupProperties(
                                            focusable = false,
                                            dismissOnClickOutside = true,
                                        ),
                                    ) {
                                        VaultHintBubble(
                                            isFromHide = false,
                                            onClick = {
                                                showVaultTooltip = false
                                                if (!state.preferences.hasSeenVaultHint) {
                                                    onAction(
                                                        MediaPickerAction.UpdateMenu(
                                                            state.preferences.copy(
                                                                hasSeenVaultHint = true,
                                                            ),
                                                        ),
                                                    )
                                                }
                                                onAction(MediaPickerAction.OnPrivacyClick)
                                            },
                                            onDismiss = {
                                                showVaultTooltip = false
                                                if (!state.preferences.hasSeenVaultHint) {
                                                    onAction(
                                                        MediaPickerAction.UpdateMenu(
                                                            state.preferences.copy(
                                                                hasSeenVaultHint = true,
                                                            ),
                                                        ),
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (selectionManager.isInSelectionMode) {
                            val pillGlassState = rememberGlassEffectState()
                            val pillInteractionSource = remember { MutableInteractionSource() }
                            val pillContentColor = rememberGlassContentColor(
                                state = pillGlassState,
                                fallback = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Row(
                                modifier = Modifier
                                    .appGlassSurface(
                                        shape = CircleShape,
                                        interactive = true,
                                        state = pillGlassState,
                                        interactionSource = pillInteractionSource,
                                        fallbackBackground = MaterialTheme.colorScheme.secondaryContainer,
                                    )
                                    .let { mod ->
                                        if (liquidGlassEnabled) {
                                            mod.border(rememberGlassBarBorderStroke(), CircleShape)
                                        } else {
                                            mod
                                        }
                                    }
                                    .clickable(
                                        interactionSource = pillInteractionSource,
                                        indication = if (liquidGlassEnabled) {
                                            null
                                        } else {
                                            androidx.compose.material3.ripple()
                                        },
                                    ) { selectionManager.exitSelectionMode() }
                                    .padding(8.dp)
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = AppIcons.Close,
                                    contentDescription = stringResource(id = R.string.navigate_up),
                                    tint = pillContentColor,
                                )
                                Text(
                                    text = stringResource(
                                        R.string.m_n_selected,
                                        selectedItemsSize,
                                        totalItemsSize,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = pillContentColor,
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
                            val toggleSelectAll = {
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
                            }
                            if (liquidGlassEnabled) {
                                val selectGlassState = rememberGlassEffectState()
                                val selectInteractionSource = remember { MutableInteractionSource() }
                                val selectContentColor = rememberGlassContentColor(
                                    state = selectGlassState,
                                    fallback = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .appGlassSurface(
                                            shape = CircleShape,
                                            interactive = true,
                                            state = selectGlassState,
                                            interactionSource = selectInteractionSource,
                                        )
                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                        .clickable(
                                            interactionSource = selectInteractionSource,
                                            indication = null,
                                            onClick = toggleSelectAll,
                                        ),
                                    contentAlignment = Alignment.Center,
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
                                        tint = selectContentColor,
                                    )
                                }
                            } else {
                                FilledTonalIconButton(onClick = toggleSelectAll) {
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
                            }
                        } else if (liquidGlassEnabled) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .appGlassContainer(enabled = true),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val searchInteraction = remember { MutableInteractionSource() }
                                val searchState = rememberGlassEffectState()
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .appGlassSurface(
                                            shape = CircleShape,
                                            interactive = true,
                                            state = searchState,
                                            interactionSource = searchInteraction,
                                        )
                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                        .clickable(
                                            interactionSource = searchInteraction,
                                            indication = null,
                                            onClick = { onAction(MediaPickerAction.OnSearchClick) },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.SearchOutline,
                                        contentDescription = stringResource(id = R.string.search),
                                        tint = rememberGlassContentColor(searchState),
                                    )
                                }

                                val menuInteraction = remember { MutableInteractionSource() }
                                val menuState = rememberGlassEffectState()
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .appGlassSurface(
                                            shape = CircleShape,
                                            interactive = true,
                                            state = menuState,
                                            interactionSource = menuInteraction,
                                        )
                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                        .clickable(
                                            interactionSource = menuInteraction,
                                            indication = null,
                                            onClick = { showQuickSettingsDialog = true },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_split_panes),
                                        contentDescription = stringResource(id = R.string.menu),
                                        tint = rememberGlassContentColor(menuState),
                                    )
                                }

                                val settingsInteraction = remember { MutableInteractionSource() }
                                val settingsState = rememberGlassEffectState()
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .appGlassSurface(
                                            shape = CircleShape,
                                            interactive = true,
                                            state = settingsState,
                                            interactionSource = settingsInteraction,
                                        )
                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                        .clickable(
                                            interactionSource = settingsInteraction,
                                            indication = null,
                                            onClick = { onAction(MediaPickerAction.OnSettingsClick) },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.SettingsOutline,
                                        contentDescription = stringResource(id = R.string.settings),
                                        tint = rememberGlassContentColor(settingsState),
                                    )
                                }
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
                            ?.find { it.uriString == selectedVideo.id }
                            ?: return@SelectionActionsSheet
                        showRenameActionFor = video
                    },
                    onInfoAction = {
                        val selectedVideo =
                            selectionManager.selectionItems.firstOrNull()
                                ?: return@SelectionActionsSheet
                        val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                            ?.find { it.uriString == selectedVideo.id }
                            ?: return@SelectionActionsSheet
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
                    if (liquidGlassEnabled) {
                        val fabInteraction = remember { MutableInteractionSource() }
                        val fabGlassState = rememberGlassEffectState()
                        val fabShape = RoundedCornerShape(18.dp)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .appGlassSurface(
                                    shape = fabShape,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
                                    clear = true,
                                    interactive = true,
                                    state = fabGlassState,
                                    interactionSource = fabInteraction,
                                    fallbackBackground = MaterialTheme.colorScheme.primary,
                                )
                                .border(
                                    width = 0.85.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.55f),
                                            Color.White.copy(alpha = 0.14f),
                                        ),
                                    ),
                                    shape = fabShape,
                                )
                                .clickable(
                                    interactionSource = fabInteraction,
                                    indication = null,
                                    onClick = onFabClick,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = AppIcons.Play,
                                contentDescription = stringResource(R.string.play),
                                tint = Color.White,
                            )
                        }
                    } else {
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
                }
            },
            containerColor = if (liquidGlassEnabled) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ) { scaffoldPadding ->
            val mediaContent: @Composable () -> Unit = {
                if (!permissionState.status.isGranted) {
                    PermissionMissingView(
                        isGranted = permissionState.status.isGranted,
                        showRationale = permissionState.status.shouldShowRationale,
                        permission = permissionState.permission,
                        launchPermissionRequest = { permissionState.launchPermissionRequest() },
                    ) {}
                } else {
                    when (state.mediaDataState) {
                        is DataState.Error -> {
                        }

                        is DataState.Loading -> {
                            CenterCircularProgressBar()
                        }

                        is DataState.Success -> {
                            val fabBottomPadding = if (!selectionManager.isInSelectionMode) 88.dp else 0.dp
                            val updatedScaffoldPadding = scaffoldPadding.copy(
                                top = if (liquidGlassEnabled) scaffoldPadding.calculateTopPadding() else 0.dp,
                                start = 0.dp,
                                bottom = scaffoldPadding.calculateBottomPadding() + fabBottomPadding,
                            )
                            val mediaHolder = state.mediaDataState.value
                            PullToRefreshBox(
                                isRefreshing = state.refreshing,
                                onRefresh = { onAction(MediaPickerAction.Refresh) },
                                state = pullToRefreshState,
                                indicator = {
                                    if (state.refreshing || pullToRefreshState.distanceFraction > 0f) {
                                        PullToRefreshDefaults.Indicator(
                                            state = pullToRefreshState,
                                            isRefreshing = state.refreshing,
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(
                                                    top = if (liquidGlassEnabled) {
                                                        scaffoldPadding.calculateTopPadding()
                                                    } else {
                                                        0.dp
                                                    },
                                                ),
                                        )
                                    }
                                },
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

            if (liquidGlassEnabled) {
                AppGlassBackdropBox(
                    backdrop = backdrop,
                    enabled = true,
                    pageColor = MaterialTheme.colorScheme.background,
                    topPadding = scaffoldPadding.calculateTopPadding(),
                    bottomPadding = scaffoldPadding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    mediaContent()
                }
            } else {
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
                    mediaContent()
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
    }
}

@Composable
private fun TransferProgressDialog(
    mode: TransferMode,
    progress: TransferProgress,
    onCancel: () -> Unit,
) {
    AppGlassModalOverlay(
        onDismissRequest = onCancel,
        modifier = Modifier.fillMaxWidth(0.90f),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dismissOnClickOutside = false,
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
    var errorCount by rememberSaveable { mutableIntStateOf(value = 0) }

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

    AppGlassModalOverlay(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .widthIn(max = 420.dp),
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
            Spacer(modifier = Modifier.height(28.dp))
            PinKeypad(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onDigit = onDigit,
                onBackspace = onBackspace,
            )
            Spacer(modifier = Modifier.height(16.dp))
            CancelButton(onClick = onDismiss)
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
        val useGlass = LocalLiquidGlassEnabled.current && LocalGlassBackdrop.current != null
        val shape = if (useGlass) {
            RoundedCornerShape(24.dp)
        } else {
            MaterialTheme.shapes.largeIncreased.copy(
                bottomStart = ZeroCornerSize,
                bottomEnd = ZeroCornerSize,
            )
        }
        val sheetGlassState = rememberGlassEffectState()
        val actionTint = rememberGlassContentColor(
            state = sheetGlassState,
            fallback = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
            ) {
                val isNarrow = maxWidth < 440.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (useGlass) {
                                Modifier
                                    .navigationBarsPadding()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .appGlassSurface(
                                        shape = shape,
                                        state = sheetGlassState,
                                        fallbackBackground = MaterialTheme.colorScheme.surfaceContainer,
                                    )
                                    .border(rememberGlassBarBorderStroke(), shape)
                                    .padding(horizontal = 4.dp, vertical = 10.dp)
                            } else {
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        shape = shape,
                                    )
                                    .clip(shape = shape)
                                    .navigationBarsPadding()
                                    .padding(
                                        horizontal = 4.dp,
                                        vertical = 12.dp,
                                    )
                            },
                        )
                        .then(
                            if (isNarrow) {
                                Modifier.horizontalScroll(rememberScrollState())
                            } else {
                                Modifier
                            },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isNarrow) {
                        Arrangement.spacedBy(2.dp)
                    } else {
                        Arrangement.SpaceBetween
                    },
                ) {
                    val itemModifier = if (isNarrow) {
                        Modifier.width(56.dp)
                    } else {
                        Modifier.weight(1f)
                    }
                    SelectionAction(
                        imageVector = AppIcons.Play,
                        title = stringResource(R.string.play),
                        tint = actionTint,
                        onClick = onPlayAction,
                        modifier = itemModifier,
                    )
                    if (showRenameAction) {
                        SelectionAction(
                            imageVector = AppIcons.Edit,
                            title = stringResource(R.string.rename),
                            tint = actionTint,
                            onClick = onRenameAction,
                            modifier = itemModifier,
                        )
                    }
                    SelectionAction(
                        imageVector = AppIcons.Share,
                        title = stringResource(R.string.share),
                        tint = actionTint,
                        onClick = onShareAction,
                        modifier = itemModifier,
                    )
                    SelectionAction(
                        imageVector = AppIcons.Copy,
                        title = stringResource(R.string.copy),
                        tint = actionTint,
                        onClick = onCopyAction,
                        modifier = itemModifier,
                    )
                    SelectionAction(
                        imageVector = AppIcons.Move,
                        title = stringResource(R.string.move),
                        tint = actionTint,
                        onClick = onMoveAction,
                        modifier = itemModifier,
                    )
                    SelectionAction(
                        imageVector = AppIcons.Check,
                        title = stringResource(R.string.mark),
                        tint = actionTint,
                        onClick = onMarkAction,
                        modifier = itemModifier,
                    )
                    if (showInfoAction) {
                        SelectionAction(
                            imageVector = AppIcons.Info,
                            title = stringResource(id = R.string.info),
                            tint = actionTint,
                            onClick = onInfoAction,
                            modifier = itemModifier,
                        )
                    }
                    if (showHideAction) {
                        SelectionAction(
                            imageVector = AppIcons.HideSource,
                            title = stringResource(id = R.string.hide),
                            tint = actionTint,
                            onClick = onHideAction,
                            modifier = itemModifier,
                        )
                    }
                    SelectionAction(
                        imageVector = AppIcons.Delete,
                        title = stringResource(id = R.string.delete),
                        tint = DestructiveRed,
                        onClick = onDeleteAction,
                        modifier = itemModifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultHintBubble(
    isFromHide: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bubbleColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val borderColor = primaryColor.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .padding(start = 2.dp, end = 16.dp)
            .widthIn(max = 290.dp),
    ) {
        Canvas(
            modifier = Modifier
                .padding(start = 20.dp)
                .size(width = 14.dp, height = 7.dp),
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = bubbleColor)
        }

        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = bubbleColor,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Lock,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.vault_hint_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = primaryColor,
                    )
                    Text(
                        text = if (isFromHide) {
                            stringResource(R.string.vault_hint_after_hide)
                        } else {
                            stringResource(R.string.vault_hint_first_launch)
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.5.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
