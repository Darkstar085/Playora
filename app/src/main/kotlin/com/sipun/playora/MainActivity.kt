package com.sipun.playora

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sipun.playora.core.common.service.system.SystemService
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.core.update.UpdateManager
import com.sipun.playora.feature.videopicker.navigation.MediaPickerRoute
import com.sipun.playora.feature.videopicker.navigation.privacyEntry
import com.sipun.playora.navigation.ensureRoot
import com.sipun.playora.navigation.mediaNavGraph
import com.sipun.playora.navigation.navigationTransition
import com.sipun.playora.navigation.navigationTransitionModifier
import com.sipun.playora.navigation.removeLastIfNotRoot
import com.sipun.playora.navigation.settingsNavGraph
import com.sipun.playora.navigation.startPlayback
import com.sipun.playora.settings.UpdateDialog
import com.sipun.playora.settings.UpdateFlowState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {

    private val mediaOperationsService: MediaOperationsService by inject()

    private val systemService: SystemService by inject()

    private val viewModel: MainViewModel by viewModel()

    private var darkSystemBars = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemService.initialize(activity = this@MainActivity)
        mediaOperationsService.initialize(activity = this@MainActivity)

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.value is MainActivityUiState.Loading
        }

        darkSystemBars = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkSystemBars },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkSystemBars },
            ),
        )

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val preferences =
                (state as? MainActivityUiState.Success)?.preferences ?: ApplicationPreferences()
            val shouldUseDarkTheme = shouldUseDarkTheme(state = state)

            val scope = rememberCoroutineScope()
            var autoUpdateFlowState by remember { mutableStateOf<UpdateFlowState?>(null) }
            var autoDownloadJob by remember { mutableStateOf<Job?>(null) }

            LaunchedEffect(Unit) {
                delay(2500)
                try {
                    val latest = UpdateManager.findLatestUpdate(this@MainActivity)
                    if (latest != null) {
                        autoUpdateFlowState = UpdateFlowState.Available(latest)
                    }
                } catch (_: Exception) {
                    // Silently ignore network failures on automatic background check
                }
            }

            LaunchedEffect(key1 = shouldUseDarkTheme) {
                darkSystemBars = shouldUseDarkTheme
                WindowCompat.getInsetsController(
                    window,
                    window.decorView,
                ).apply {
                    isAppearanceLightStatusBars = !shouldUseDarkTheme
                    isAppearanceLightNavigationBars = !shouldUseDarkTheme
                }
            }


            PlayoraTheme(
                preferences = preferences,
                darkTheme = shouldUseDarkTheme,
                highContrastDarkTheme = shouldUseHighContrastDarkTheme(state = state),
                dynamicColor = shouldUseDynamicTheming(state = state),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    val backStack = rememberNavBackStack(MediaPickerRoute())
                    backStack.ensureRoot(MediaPickerRoute())

                    val provider = entryProvider {
                        mediaNavGraph(
                            context = this@MainActivity,
                            backStack = backStack,
                        )
                        settingsNavGraph(backStack = backStack)
                        privacyEntry(
                            onNavigateUp = { backStack.removeLastIfNotRoot() },
                            onPlayVideo = { uri, playlist ->
                                if (!playlist.isNullOrEmpty()) {
                                    this@MainActivity.startPlayback(
                                        uris = playlist,
                                        startUri = uri,
                                        grantReadPermission = true,
                                    )
                                } else {
                                    this@MainActivity.startPlayback(
                                        uri = uri,
                                        grantReadPermission = true,
                                    )
                                }
                            },
                            onPlayVideos = { uris ->
                                this@MainActivity.startPlayback(
                                    uris = uris,
                                    grantReadPermission = true,
                                )
                            },
                        )
                    }

                    val navigationDecorator = remember {
                        NavEntryDecorator<NavKey> { entry ->
                            Box(navigationTransitionModifier(entry)) {
                                entry.Content()
                            }
                        }
                    }

                    val entries = rememberDecoratedNavEntries(
                        backStack = backStack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            navigationDecorator,
                        ),
                        entryProvider = provider,
                    )

                    NavDisplay(
                        entries = entries,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastIfNotRoot()
                            } else {
                                finish()
                            }
                        },
                        transitionSpec = {
                            navigationTransition(
                                initialState = initialState,
                                targetState = targetState,
                            )
                        },
                        popTransitionSpec = {
                            navigationTransition(
                                initialState = initialState,
                                targetState = targetState,
                            )
                        },
                        predictivePopTransitionSpec = {
                            navigationTransition(
                                initialState = initialState,
                                targetState = targetState,
                                isPredictive = true,
                            )
                        },
                    )

                    autoUpdateFlowState?.let { flowState ->
                        UpdateDialog(
                            state = flowState,
                            onDismiss = {
                                autoDownloadJob?.cancel()
                                autoDownloadJob = null
                                autoUpdateFlowState = null
                            },
                            onUpdateNow = { selectedUpdate ->
                                autoDownloadJob = scope.launch {
                                    autoUpdateFlowState = UpdateFlowState.Downloading(
                                        update = selectedUpdate,
                                        downloadedBytes = 0L,
                                        totalBytes = selectedUpdate.size,
                                        progressPercent = 0,
                                    )
                                    try {
                                        val apk = UpdateManager.download(this@MainActivity, selectedUpdate) { downloaded, total ->
                                            val percent = if (total > 0) {
                                                ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                                            } else {
                                                0
                                            }
                                            autoUpdateFlowState = UpdateFlowState.Downloading(
                                                update = selectedUpdate,
                                                downloadedBytes = downloaded,
                                                totalBytes = total,
                                                progressPercent = percent,
                                            )
                                        }
                                        autoUpdateFlowState = UpdateFlowState.Installing(selectedUpdate)
                                        if (UpdateManager.install(this@MainActivity, apk)) {
                                            autoUpdateFlowState = UpdateFlowState.Success(selectedUpdate.version)
                                        } else {
                                            autoUpdateFlowState = UpdateFlowState.Error(
                                                getString(R.string.update_install_permission),
                                            )
                                        }
                                    } catch (_: CancellationException) {
                                        autoUpdateFlowState = null
                                    } catch (e: Exception) {
                                        autoUpdateFlowState = UpdateFlowState.Error(
                                            e.message ?: getString(R.string.update_download_failed),
                                        )
                                    }
                                }
                            },
                            onCancelDownload = {
                                autoDownloadJob?.cancel()
                                autoDownloadJob = null
                                autoUpdateFlowState = null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun shouldUseDarkTheme(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()

    is MainActivityUiState.Success -> when (state.preferences.themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
        ThemeConfig.OFF -> false
        ThemeConfig.ON -> true
    }
}

@Composable
fun shouldUseHighContrastDarkTheme(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> state.preferences.useHighContrastDarkTheme
}

@Composable
fun shouldUseDynamicTheming(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> state.preferences.useDynamicColors
}
