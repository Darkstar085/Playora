package com.sipun.playora.feature.videopicker.privacy

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.sipun.playora.R
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.model.Sort
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.DeleteButton
import com.sipun.playora.core.ui.components.DestructiveRed
import com.sipun.playora.core.ui.components.MediaQuickSettingsDialog
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.videopicker.composables.CenterCircularProgressBar
import com.sipun.playora.feature.videopicker.composables.MediaInfoDialog
import com.sipun.playora.feature.videopicker.composables.SelectionAction
import com.sipun.playora.feature.videopicker.composables.VideoItem
import com.sipun.playora.feature.videopicker.composables.privacy.PinDotsIndicator
import com.sipun.playora.feature.videopicker.composables.privacy.PinKeypad
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacyBiometricButton
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacyBiometricSetupDialog
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacyProgressDialog
import com.sipun.playora.feature.videopicker.composables.privacy.PrivacySettingsDialog
import com.sipun.playora.feature.videopicker.state.SelectionItem
import com.sipun.playora.feature.videopicker.state.rememberSelectionManager

@Composable
fun PrivacyScreen(viewModel: PrivacyViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(
        key1 = lifecycleOwner,
        key2 = viewModel,
    ) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is PrivacyEvent.VideosRelocated -> {
                        val message = context.resources.getQuantityString(
                            R.plurals.videos_relocated_to_movies,
                            event.count,
                            event.count,
                        )
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
    }

    PrivacyScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
internal fun PrivacyScreenContent(state: PrivacyUiState, onAction: (PrivacyAction) -> Unit) {
    when (state.stage) {
        PrivacyStage.LOADING -> CenterCircularProgressBar()

        PrivacyStage.LOCKED -> PrivacyPinContent(
            title = stringResource(R.string.enter_privacy_pin),
            description = stringResource(R.string.enter_privacy_pin_description),
            pinErrorCount = state.pinErrorCount,
            errorMessage = stringResource(R.string.incorrect_pin),
            onSubmit = { onAction(PrivacyAction.SubmitUnlockPin(pin = it)) },
            onBiometricAuthenticated = if (state.biometricEnabled) {
                { onAction(PrivacyAction.BiometricAuthenticated) }
            } else {
                null
            },
            onNavigateUp = { onAction(PrivacyAction.NavigateUp) },
        )

        PrivacyStage.SET_PIN -> PrivacyPinContent(
            title = stringResource(R.string.set_privacy_pin),
            description = stringResource(R.string.set_privacy_pin_description),
            pinErrorCount = state.setPinGeneration,
            onSubmit = { onAction(PrivacyAction.SubmitNewPin(pin = it)) },
            onNavigateUp = { onAction(PrivacyAction.NavigateUp) },
        )

        PrivacyStage.CONFIRM_PIN -> PrivacyPinContent(
            title = stringResource(R.string.confirm_privacy_pin),
            description = stringResource(R.string.confirm_privacy_pin_description),
            pinErrorCount = state.pinErrorCount,
            errorMessage = stringResource(R.string.pins_do_not_match),
            onSubmit = { onAction(PrivacyAction.SubmitPinConfirmation(pin = it)) },
            onNavigateUp = { onAction(PrivacyAction.NavigateUp) },
        )

        PrivacyStage.BIOMETRIC_SETUP -> PrivacyBiometricSetupDialog(
            onComplete = { onAction(PrivacyAction.CompleteBiometricSetup(enabled = it)) },
        )

        PrivacyStage.UNLOCKED -> PrivacyGalleryContent(
            state = state,
            onAction = onAction,
        )
    }

    if (state.isUnhiding) {
        PrivacyProgressDialog(message = stringResource(R.string.unhiding_videos_in_progress))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPinContent(
    title: String,
    description: String,
    pinErrorCount: Int,
    onSubmit: (String) -> Unit,
    onNavigateUp: () -> Unit,
    errorMessage: String = "",
    onBiometricAuthenticated: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "",
                navigationIcon = {
                    AppBackButton(onClick = onNavigateUp)
                },
            )
        },
        bottomBar = {
            onBiometricAuthenticated?.let { onAuthenticated ->
                PrivacyBiometricButton(
                    onAuthenticated = onAuthenticated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 8.dp,
                        ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { padding ->
        PinEntryContent(
            modifier = Modifier.padding(paddingValues = padding),
            title = title,
            description = description,
            pinErrorCount = pinErrorCount,
            errorMessage = errorMessage,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun PinEntryContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    pinErrorCount: Int,
    errorMessage: String,
    onSubmit: (String) -> Unit,
) {

    var pin by rememberSaveable(pinErrorCount) { mutableStateOf(value = "") }
    var showError by rememberSaveable(pinErrorCount) { mutableStateOf(value = pinErrorCount > 0) }

    val onDigit: (Char) -> Unit = { digit ->
        if (pin.length < PRIVACY_PIN_LENGTH) {
            showError = false
            pin += digit
            if (pin.length == PRIVACY_PIN_LENGTH) {
                onSubmit(pin)
            }
        }
    }
    val onBackspace: () -> Unit = {
        if (pin.isNotEmpty()) pin = pin.dropLast(n = 1)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        PinEntryHeader(
            title = title,
            description = description,
            filledCount = pin.length,
            showError = showError,
            errorMessage = errorMessage,
        )
        Spacer(modifier = Modifier.size(32.dp))
        PinKeypad(
            modifier = Modifier.padding(horizontal = 24.dp),
            onDigit = onDigit,
            onBackspace = onBackspace,
        )
        Spacer(modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun PinEntryHeader(
    title: String,
    description: String,
    filledCount: Int,
    showError: Boolean,
    errorMessage: String,
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
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(32.dp))
        PinDotsIndicator(
            filledCount = filledCount,
            error = showError,
        )
        Spacer(modifier = Modifier.size(8.dp))
        if (showError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun PrivacyGalleryContent(state: PrivacyUiState, onAction: (PrivacyAction) -> Unit) {
    val selectionManager = rememberSelectionManager()
    var showQuickSettings by rememberSaveable { mutableStateOf(value = false) }
    var showSettings by rememberSaveable { mutableStateOf(value = false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(value = false) }
    var showUnhideConfirmation by rememberSaveable { mutableStateOf(value = false) }

    val selectedCount = selectionManager.selectionItems.size
    val totalCount = state.hiddenVideos.size

    BackHandler(enabled = selectionManager.isInSelectionMode) {
        selectionManager.exitSelectionMode()
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = {
                    val titleText =
                        stringResource(R.string.hidden_videos).takeIf {
                            !selectionManager.isInSelectionMode
                        }
                            ?: ""
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp),
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
                                    selectedCount,
                                    totalCount,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    } else {
                        AppBackButton(
                            onClick = { onAction(PrivacyAction.NavigateUp) },
                        )
                    }
                },
                actions = {
                    if (selectionManager.isInSelectionMode) {
                        FilledTonalIconButton(
                            onClick = {
                                if (selectedCount != totalCount) {
                                    state.hiddenVideos.forEach {
                                        selectionManager.selectVideo(video = it)
                                    }
                                } else {
                                    selectionManager.clearSelection()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (selectedCount != totalCount) {
                                    AppIcons.SelectAll
                                } else {
                                    AppIcons.DeselectAll
                                },
                                contentDescription = if (selectedCount != totalCount) {
                                    stringResource(R.string.select_all)
                                } else {
                                    stringResource(R.string.deselect_all)
                                },
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showQuickSettings = true },
                        ) {
                            Icon(
                                imageVector = AppIcons.Sensitivity,
                                contentDescription = stringResource(R.string.quick_settings),
                            )
                        }
                        IconButton(
                            onClick = { showSettings = true },
                        ) {
                            Icon(
                                imageVector = AppIcons.Settings,
                                contentDescription = stringResource(R.string.privacy_settings),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            PrivacySelectionActionsSheet(
                show =
                    selectionManager.isInSelectionMode &&
                            selectionManager.selectionItems.isNotEmpty(),
                onPlayAction = {
                    onAction(PrivacyAction.PlaySelected(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onInfoAction = {
                    val selected = selectionManager.selectionItems.firstOrNull()
                    val video = state.hiddenVideos.find {
                        it.uriString == (selected as? SelectionItem.Video)?.uriString
                    }
                    if (video != null) onAction(PrivacyAction.ShowMediaInfo(video = video))
                    selectionManager.exitSelectionMode()
                },
                showInfoAction = selectionManager.selectionItems.size == 1,
                onUnhideAction = { showUnhideConfirmation = true },
                onDeleteAction = { showDeleteConfirmation = true },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(start = padding.calculateStartPadding(LocalLayoutDirection.current))
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                    ),
                )
                .background(MaterialTheme.colorScheme.background),
        ) {
            val updatedPadding = padding.copy(
                top = 0.dp,
                start = 0.dp,
            )
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = updatedPadding),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }

                state.hiddenVideos.isEmpty() -> {
                    PrivacyEmptyState(contentPadding = updatedPadding)
                }

                else -> {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        columns = if (state.preferences.mediaLayoutMode == MediaLayoutMode.GRID) {
                            GridCells.Adaptive(minSize = 130.dp)
                        } else {
                            GridCells.Fixed(count = 1)
                        },
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = updatedPadding.calculateTopPadding() + 8.dp,
                            bottom = updatedPadding.calculateBottomPadding() + 8.dp,
                        ),
                    ) {
                        itemsIndexed(
                            items = state.hiddenVideos,
                            key = { _, video -> video.uriString },
                        ) { _, video ->
                            val selected = selectionManager.isVideoSelected(video = video)
                            VideoItem(
                                modifier = Modifier.padding(2.dp),
                                video = video,
                                isRecentlyPlayedVideo = false,
                                preferences = state.preferences,
                                selected = selected,
                                onClick = {
                                    if (selectionManager.isInSelectionMode) {
                                        selectionManager.toggleVideoSelection(video = video)
                                    } else {
                                        onAction(PrivacyAction.PlayVideo(video = video))
                                    }
                                },
                                onLongClick = {
                                    selectionManager.toggleVideoSelection(video = video)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        PrivacySettingsDialog(
            biometricEnabled = state.biometricEnabled,
            onBiometricEnabledChange = { onAction(PrivacyAction.SetBiometricEnabled(enabled = it)) },
            onDismiss = { showSettings = false },
        )
    }

    if (showQuickSettings) {
        MediaQuickSettingsDialog(
            layoutMode = state.preferences.mediaLayoutMode,
            sort = state.sort,
            sortOptions = listOf(
                Sort.By.TITLE,
                Sort.By.LENGTH,
                Sort.By.DATE,
                Sort.By.SIZE,
            ),
            dateLabel = R.string.date_hidden,
            onDismiss = { showQuickSettings = false },
            onConfirm = { layout, sort ->
                onAction(
                    PrivacyAction.UpdateQuickSettings(
                        layoutMode = layout,
                        sort = sort,
                    ),
                )
            },
        )
    }

    if (showDeleteConfirmation) {
        AppDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = if (selectedCount == 1) {
                        stringResource(R.string.delete_one_video_from_privacy)
                    } else {
                        stringResource(
                            R.string.delete_videos_from_privacy,
                            selectedCount,
                        )
                    },
                )
            },
            content = { Text(text = stringResource(R.string.delete_from_privacy_info)) },
            confirmButton = {
                DeleteButton(
                    onClick = {
                        onAction(PrivacyAction.DeleteSelected(selectionManager.selectionItems))
                        selectionManager.exitSelectionMode()
                        showDeleteConfirmation = false
                    },
                )
            },
            dismissButton = { CancelButton(onClick = { showDeleteConfirmation = false }) },
        )
    }

    if (showUnhideConfirmation) {
        AppDialog(
            onDismissRequest = { showUnhideConfirmation = false },
            title = { Text(text = stringResource(R.string.unhide)) },
            content = {
                Text(
                    text = if (selectedCount == 1) {
                        stringResource(R.string.unhide_one_video_confirmation)
                    } else {
                        stringResource(
                            R.string.unhide_videos_confirmation,
                            selectedCount,
                        )
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(PrivacyAction.UnhideSelected(selectionManager.selectionItems))
                        selectionManager.exitSelectionMode()
                        showUnhideConfirmation = false
                    },
                ) { Text(text = stringResource(R.string.unhide)) }
            },
            dismissButton = { CancelButton(onClick = { showUnhideConfirmation = false }) },
        )
    }

    state.mediaInfo?.let { mediaInfo ->
        MediaInfoDialog(
            mediaInfo = mediaInfo,
            onDismiss = { onAction(PrivacyAction.DismissMediaInfo) },
        )
    }
}

@Composable
private fun PrivacyEmptyState(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(paddingValues = contentPadding)
            .padding(
                horizontal = 24.dp,
                vertical = 40.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.privacy_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.privacy_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrivacySelectionActionsSheet(
    modifier: Modifier = Modifier,
    show: Boolean,
    onPlayAction: () -> Unit,
    onInfoAction: () -> Unit,
    showInfoAction: Boolean,
    onUnhideAction: () -> Unit,
    onDeleteAction: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier.padding(
            start = WindowInsets.displayCutout.asPaddingValues()
                .calculateStartPadding(LocalLayoutDirection.current),
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
                    .horizontalScroll(state = rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 12.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SelectionAction(
                    imageVector = AppIcons.Play,
                    title = stringResource(R.string.play),
                    onClick = onPlayAction,
                )
                if (showInfoAction) {
                    SelectionAction(
                        imageVector = AppIcons.Info,
                        title = stringResource(R.string.info),
                        onClick = onInfoAction,
                    )
                }
                SelectionAction(
                    imageVector = AppIcons.Lock,
                    title = stringResource(R.string.unhide),
                    onClick = onUnhideAction,
                )
                SelectionAction(
                    imageVector = AppIcons.Delete,
                    title = stringResource(R.string.delete),
                    tint = DestructiveRed,
                    onClick = onDeleteAction,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PrivacyPinContentPreview() {
    PlayoraTheme {
        PrivacyPinContent(
            title = stringResource(R.string.enter_privacy_pin),
            description = stringResource(R.string.enter_privacy_pin_description),
            pinErrorCount = 0,
            onSubmit = {},
            onNavigateUp = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PrivacyEmptyStatePreview() {
    PlayoraTheme { Surface { PrivacyEmptyState(contentPadding = PaddingValues()) } }
}
