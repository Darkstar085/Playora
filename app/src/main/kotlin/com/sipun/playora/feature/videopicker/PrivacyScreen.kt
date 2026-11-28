package com.sipun.playora.feature.videopicker.privacy

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.appGlassBackdrop
import com.sipun.playora.core.ui.theme.appGlassContainer
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.rememberGlassBarBorderStroke
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
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
import io.github.itshedimisawi.vitro.rememberGlassBackdrop
import io.github.itshedimisawi.vitro.rememberGlassEffectState

@Composable
fun PrivacyScreen(viewModel: PrivacyViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(
        key1 = lifecycleOwner,
        key2 = viewModel,
    ) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is PrivacyEvent.VideosRelocated -> {
                        val message = resources.getQuantityString(
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
            lockoutRemainingSeconds = state.lockoutRemainingSeconds,
            errorMessage = if (state.lockoutRemainingSeconds > 0) {
                stringResource(R.string.privacy_pin_locked_seconds, state.lockoutRemainingSeconds)
            } else {
                stringResource(R.string.incorrect_pin)
            },
            onSubmit = { onAction(PrivacyAction.SubmitUnlockPin(pin = it)) },
            onBiometricAuthenticated = if (state.biometricEnabled) {
                { onAction(PrivacyAction.BiometricAuthenticated) }
            } else {
                null
            },
            onNavigateUp = { onAction(PrivacyAction.NavigateUp) },
        )

        PrivacyStage.VERIFY_CURRENT_PIN -> PrivacyPinContent(
            title = stringResource(R.string.enter_current_pin),
            description = stringResource(R.string.enter_current_pin_description),
            pinErrorCount = state.pinErrorCount,
            lockoutRemainingSeconds = state.lockoutRemainingSeconds,
            errorMessage = if (state.lockoutRemainingSeconds > 0) {
                stringResource(R.string.privacy_pin_locked_seconds, state.lockoutRemainingSeconds)
            } else {
                stringResource(R.string.incorrect_pin)
            },
            onSubmit = { onAction(PrivacyAction.SubmitCurrentPin(pin = it)) },
            onNavigateUp = { onAction(PrivacyAction.CancelPinChange) },
            interceptSystemBack = true,
        )

        PrivacyStage.SET_PIN -> PrivacyPinContent(
            title = stringResource(R.string.set_privacy_pin),
            description = stringResource(R.string.set_privacy_pin_description),
            pinErrorCount = state.setPinGeneration,
            errorMessage = if (state.setPinGeneration > 0) {
                stringResource(R.string.pins_do_not_match)
            } else {
                ""
            },
            onSubmit = { onAction(PrivacyAction.SubmitNewPin(pin = it)) },
            onNavigateUp = {
                if (state.isChangingPin) {
                    onAction(PrivacyAction.CancelPinChange)
                } else {
                    onAction(PrivacyAction.NavigateUp)
                }
            },
            interceptSystemBack = state.isChangingPin,
        )

        PrivacyStage.CONFIRM_PIN -> PrivacyPinContent(
            title = stringResource(R.string.confirm_privacy_pin),
            description = stringResource(R.string.confirm_privacy_pin_description),
            pinErrorCount = state.pinErrorCount,
            errorMessage = stringResource(R.string.pins_do_not_match),
            onSubmit = { onAction(PrivacyAction.SubmitPinConfirmation(pin = it)) },
            onNavigateUp = {
                if (state.isChangingPin) {
                    onAction(PrivacyAction.CancelPinChange)
                } else {
                    onAction(PrivacyAction.NavigateUp)
                }
            },
            interceptSystemBack = state.isChangingPin,
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

@Composable
private fun PrivacyPinContent(
    title: String,
    description: String,
    pinErrorCount: Int,
    onSubmit: (String) -> Unit,
    onNavigateUp: () -> Unit,
    lockoutRemainingSeconds: Int = 0,
    errorMessage: String = "",
    interceptSystemBack: Boolean = false,
    onBiometricAuthenticated: (() -> Unit)? = null,
) {
    if (interceptSystemBack) {
        BackHandler(onBack = onNavigateUp)
    }
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val backdrop = rememberGlassBackdrop()
    val containerColor = if (liquidGlassEnabled) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) backdrop else null,
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
            containerColor = containerColor,
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                val accent = MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .appGlassBackdrop(backdrop, enabled = liquidGlassEnabled)
                        .background(containerColor)
                        .then(
                            if (liquidGlassEnabled) {
                                Modifier.background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accent.copy(alpha = 0.14f),
                                            accent.copy(alpha = 0.04f),
                                            Color.Transparent,
                                        ),
                                        radius = 900f,
                                    ),
                                )
                            } else {
                                Modifier
                            },
                        ),
                )
                PinEntryContent(
                    modifier = Modifier.padding(paddingValues = padding),
                    title = title,
                    description = description,
                    pinErrorCount = pinErrorCount,
                    lockoutRemainingSeconds = lockoutRemainingSeconds,
                    errorMessage = errorMessage,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
private fun PinEntryContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    pinErrorCount: Int,
    lockoutRemainingSeconds: Int,
    errorMessage: String,
    onSubmit: (String) -> Unit,
) {
    val isLockedOut = lockoutRemainingSeconds > 0
    var pin by rememberSaveable(pinErrorCount) { mutableStateOf(value = "") }
    var showError by rememberSaveable(pinErrorCount) { mutableStateOf(value = pinErrorCount > 0) }
    var wasLockedOut by rememberSaveable { mutableStateOf(value = isLockedOut) }

    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            wasLockedOut = true
            pin = ""
            showError = true
        } else if (wasLockedOut) {
            wasLockedOut = false
            showError = false
        }
    }

    val onDigit: (Char) -> Unit = { digit ->
        if (!isLockedOut && pin.length < PRIVACY_PIN_LENGTH) {
            showError = false
            pin += digit
            if (pin.length == PRIVACY_PIN_LENGTH) {
                onSubmit(pin)
            }
        }
    }
    val onBackspace: () -> Unit = {
        if (!isLockedOut && pin.isNotEmpty()) pin = pin.dropLast(n = 1)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val useHorizontalSplit = maxWidth >= 600.dp && maxHeight < 620.dp
        if (useHorizontalSplit) {
            val landscapeKeypadMaxWidth = ((maxHeight - 48.dp) * 0.68f).coerceIn(180.dp, 260.dp)
            Row(
                modifier = Modifier
                    .widthIn(max = 820.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                PinEntryHeader(
                    title = title,
                    description = description,
                    filledCount = pin.length,
                    showError = showError || isLockedOut,
                    errorMessage = errorMessage,
                    compact = true,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    PinKeypad(
                        modifier = Modifier
                            .widthIn(max = landscapeKeypadMaxWidth)
                            .fillMaxWidth(),
                        enabled = !isLockedOut,
                        onDigit = onDigit,
                        onBackspace = onBackspace,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PinEntryHeader(
                    title = title,
                    description = description,
                    filledCount = pin.length,
                    showError = showError || isLockedOut,
                    errorMessage = errorMessage,
                )
                Spacer(modifier = Modifier.size(28.dp))
                PinKeypad(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = !isLockedOut,
                    onDigit = onDigit,
                    onBackspace = onBackspace,
                )
            }
        }
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
    compact: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(if (compact) 14.dp else 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 26.dp else 32.dp),
            )
        }
        Spacer(modifier = Modifier.size(if (compact) 12.dp else 20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(if (compact) 4.dp else 8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.size(if (compact) 16.dp else 32.dp))
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

@Composable
private fun PrivacyGalleryContent(state: PrivacyUiState, onAction: (PrivacyAction) -> Unit) {
    val selectionManager = rememberSelectionManager()
    var showQuickSettings by rememberSaveable { mutableStateOf(value = false) }
    var showSettings by rememberSaveable { mutableStateOf(value = false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(value = false) }
    var showUnhideConfirmation by rememberSaveable { mutableStateOf(value = false) }

    val selectedCount = selectionManager.selectionItems.size
    val totalCount = state.hiddenVideos.size
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val backdrop = rememberGlassBackdrop()

    BackHandler(enabled = selectionManager.isInSelectionMode) {
        selectionManager.exitSelectionMode()
    }

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) backdrop else null,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                AppTopAppBar(
                    title = {
                        val titleText =
                            stringResource(R.string.hidden_videos).takeIf {
                                !selectionManager.isInSelectionMode
                            }
                                ?: ""
                        if (titleText.isNotEmpty()) {
                            val titleGlassState = rememberGlassEffectState()
                            val titleContentColor = rememberGlassContentColor(
                                state = titleGlassState,
                                fallback = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = titleText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = if (liquidGlassEnabled) {
                                    titleContentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier
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
                                    ),
                            )
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
                                        selectedCount,
                                        totalCount,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = pillContentColor,
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
                            val toggleSelectAll = {
                                if (selectedCount != totalCount) {
                                    state.hiddenVideos.forEach {
                                        selectionManager.selectVideo(video = it)
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
                                        tint = selectContentColor,
                                    )
                                }
                            } else {
                                FilledTonalIconButton(onClick = toggleSelectAll) {
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
                            }
                        } else if (liquidGlassEnabled) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .appGlassContainer(enabled = true),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val quickInteraction = remember { MutableInteractionSource() }
                                val quickState = rememberGlassEffectState()
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .appGlassSurface(
                                            shape = CircleShape,
                                            interactive = true,
                                            state = quickState,
                                            interactionSource = quickInteraction,
                                        )
                                        .border(rememberGlassBarBorderStroke(), CircleShape)
                                        .clickable(
                                            interactionSource = quickInteraction,
                                            indication = null,
                                            onClick = { showQuickSettings = true },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Sensitivity,
                                        contentDescription = stringResource(R.string.quick_settings),
                                        tint = rememberGlassContentColor(quickState),
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
                                            onClick = { showSettings = true },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Settings,
                                        contentDescription = stringResource(R.string.privacy_settings),
                                        tint = rememberGlassContentColor(settingsState),
                                    )
                                }
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
            containerColor = if (liquidGlassEnabled) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ) { padding ->
            val galleryContent: @Composable () -> Unit = {
                val updatedPadding = padding.copy(
                    top = if (liquidGlassEnabled) padding.calculateTopPadding() else 0.dp,
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
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val listColumns = (maxWidth / 320.dp).toInt().coerceIn(
                                minimumValue = 1,
                                maximumValue = 3,
                            )
                            LazyVerticalGrid(
                                modifier = Modifier.fillMaxSize(),
                                columns = if (state.preferences.mediaLayoutMode == MediaLayoutMode.GRID) {
                                    GridCells.Adaptive(minSize = 130.dp)
                                } else {
                                    GridCells.Fixed(count = listColumns)
                                },
                                horizontalArrangement = Arrangement.spacedBy(
                                    if (state.preferences.mediaLayoutMode == MediaLayoutMode.LIST && listColumns > 1) {
                                        8.dp
                                    } else {
                                        0.dp
                                    },
                                ),
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

            if (liquidGlassEnabled) {
                AppGlassBackdropBox(
                    backdrop = backdrop,
                    enabled = true,
                    pageColor = MaterialTheme.colorScheme.background,
                    topPadding = padding.calculateTopPadding(),
                    bottomPadding = padding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    galleryContent()
                }
            } else {
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
                    galleryContent()
                }
            }
        }

    if (showSettings) {
        PrivacySettingsDialog(
            biometricEnabled = state.biometricEnabled,
            onBiometricEnabledChange = { onAction(PrivacyAction.SetBiometricEnabled(enabled = it)) },
            onChangePinClick = {
                showSettings = false
                onAction(PrivacyAction.StartChangePin)
            },
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
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
                                .horizontalScroll(state = rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        } else {
                            Modifier
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
                                )
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SelectionAction(
                    imageVector = AppIcons.Play,
                    title = stringResource(R.string.play),
                    tint = actionTint,
                    onClick = onPlayAction,
                )
                if (showInfoAction) {
                    SelectionAction(
                        imageVector = AppIcons.Info,
                        title = stringResource(R.string.info),
                        tint = actionTint,
                        onClick = onInfoAction,
                    )
                }
                SelectionAction(
                    imageVector = AppIcons.Lock,
                    title = stringResource(R.string.unhide),
                    tint = actionTint,
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
