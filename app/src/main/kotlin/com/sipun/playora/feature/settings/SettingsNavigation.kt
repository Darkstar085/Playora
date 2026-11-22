package com.sipun.playora.settings.navigation

import android.widget.Toast
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sipun.playora.settings.Setting
import com.sipun.playora.settings.SettingsOutput
import com.sipun.playora.settings.AboutOutput
import com.sipun.playora.settings.AboutScreen
import com.sipun.playora.settings.SettingsScreen
import com.sipun.playora.settings.screens.appearance.AppearancePreferencesScreen
import com.sipun.playora.settings.screens.appearance.AppearancePreferencesViewModel
import com.sipun.playora.settings.screens.audio.AudioPreferencesScreen
import com.sipun.playora.settings.screens.audio.AudioPreferencesViewModel
import com.sipun.playora.settings.screens.general.GeneralPreferencesScreen
import com.sipun.playora.settings.screens.general.GeneralPreferencesViewModel
import com.sipun.playora.settings.screens.gesture.GesturePreferencesScreen
import com.sipun.playora.settings.screens.gesture.GesturePreferencesViewModel
import com.sipun.playora.settings.screens.medialibrary.FolderPreferencesScreen
import com.sipun.playora.settings.screens.medialibrary.FolderPreferencesViewModel
import com.sipun.playora.settings.screens.medialibrary.MediaLibraryPreferencesScreen
import com.sipun.playora.settings.screens.medialibrary.MediaLibraryPreferencesViewModel
import com.sipun.playora.settings.screens.player.PlayerPreferencesScreen
import com.sipun.playora.settings.screens.player.PlayerPreferencesViewModel
import com.sipun.playora.settings.screens.subtitle.SubtitlePreferencesScreen
import com.sipun.playora.settings.screens.subtitle.SubtitlePreferencesViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
object AppearancePreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToAppearancePreferences() {
    add(element = AppearancePreferencesRoute)
}

fun EntryProviderScope<NavKey>.appearancePreferencesEntry(onNavigateUp: () -> Unit) {
    entry<AppearancePreferencesRoute> {
        val output = AppearancePreferencesViewModel.Output(
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<AppearancePreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        AppearancePreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object AudioPreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToAudioPreferences() {
    add(element = AudioPreferencesRoute)
}

fun EntryProviderScope<NavKey>.audioPreferencesEntry(onNavigateUp: () -> Unit) {
    entry<AudioPreferencesRoute> {
        val output = AudioPreferencesViewModel.Output(
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<AudioPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        AudioPreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object GeneralPreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToGeneralPreferences() {
    add(element = GeneralPreferencesRoute)
}

fun EntryProviderScope<NavKey>.generalPreferencesEntry(
    onNavigateUp: () -> Unit,
    onFolderSettingClick: () -> Unit = {},
) {
    entry<GeneralPreferencesRoute> {
        val context = LocalContext.current
        val output = GeneralPreferencesViewModel.Output(
            navigateUp = onNavigateUp,
            openFolders = onFolderSettingClick,
            showMessage = {
                Toast.makeText(
                    context,
                    it,
                    Toast.LENGTH_LONG,
                ).show()
            },
        )
        val viewModel = koinViewModel<GeneralPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        GeneralPreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object GesturePreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToGesturePreferences() {
    add(element = GesturePreferencesRoute)
}

fun EntryProviderScope<NavKey>.gesturePreferencesEntry(onNavigateUp: () -> Unit) {
    entry<GesturePreferencesRoute> {
        val output = GesturePreferencesViewModel.Output(
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<GesturePreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        GesturePreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object MediaLibraryPreferencesRoute : NavKey

@Serializable
object FolderPreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToMediaLibraryPreferencesScreen() {
    add(element = MediaLibraryPreferencesRoute)
}

fun NavBackStack<NavKey>.navigateToFolderPreferencesScreen() {
    add(element = FolderPreferencesRoute)
}

fun EntryProviderScope<NavKey>.mediaLibraryPreferencesEntry(
    onNavigateUp: () -> Unit,
    onFolderSettingClick: () -> Unit,
) {
    entry<MediaLibraryPreferencesRoute> {
        val output = MediaLibraryPreferencesViewModel.Output(
            navigateUp = onNavigateUp,
            openFolders = onFolderSettingClick,
        )
        val viewModel = koinViewModel<MediaLibraryPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        MediaLibraryPreferencesScreen(viewModel = viewModel)
    }
}

fun EntryProviderScope<NavKey>.folderPreferencesEntry(onNavigateUp: () -> Unit) {
    entry<FolderPreferencesRoute> {
        val output = FolderPreferencesViewModel.Output(navigateUp = onNavigateUp)
        val viewModel = koinViewModel<FolderPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        FolderPreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object PlayerPreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToPlayerPreferences() {
    add(element = PlayerPreferencesRoute)
}

fun EntryProviderScope<NavKey>.playerPreferencesEntry(onNavigateUp: () -> Unit) {
    entry<PlayerPreferencesRoute> {
        val output = PlayerPreferencesViewModel.Output(
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<PlayerPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        PlayerPreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object SettingsRoute : NavKey

fun NavBackStack<NavKey>.navigateToSettings() {
    add(element = SettingsRoute)
}

fun EntryProviderScope<NavKey>.settingsEntry(
    onNavigateUp: () -> Unit,
    onItemClick: (Setting) -> Unit,
) {
    entry<SettingsRoute> {
        SettingsScreen(
            output = SettingsOutput(
                navigateUp = onNavigateUp,
                openSetting = onItemClick,
            ),
        )
    }
}

@Serializable
object SubtitlePreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToSubtitlePreferences() {
    add(element = SubtitlePreferencesRoute)
}

fun EntryProviderScope<NavKey>.subtitlePreferencesEntry(onNavigateUp: () -> Unit) {
    entry<SubtitlePreferencesRoute> {
        val output = SubtitlePreferencesViewModel.Output(
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<SubtitlePreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        SubtitlePreferencesScreen(viewModel = viewModel)
    }
}

@Serializable
object AboutRoute : NavKey

fun NavBackStack<NavKey>.navigateToAbout() {
    add(element = AboutRoute)
}

fun EntryProviderScope<NavKey>.aboutEntry(onNavigateUp: () -> Unit) {
    entry<AboutRoute> {
        AboutScreen(output = AboutOutput(navigateUp = onNavigateUp))
    }
}
