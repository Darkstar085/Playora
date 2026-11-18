package com.sipun.playora.feature.videopicker.navigation

import android.net.Uri
import androidx.compose.runtime.SideEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sipun.playora.feature.videopicker.privacy.PrivacyScreen
import com.sipun.playora.feature.videopicker.privacy.PrivacyViewModel
import com.sipun.playora.feature.videopicker.screens.mediapicker.MediaPickerScreen
import com.sipun.playora.feature.videopicker.screens.mediapicker.MediaPickerViewModel
import com.sipun.playora.feature.videopicker.screens.search.SearchScreen
import com.sipun.playora.feature.videopicker.screens.search.SearchViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class MediaPickerRoute(val folderId: String? = null) : NavKey

fun NavBackStack<NavKey>.navigateToMediaPickerScreen(folderId: String) {
    add(element = MediaPickerRoute(folderId = folderId))
}

fun EntryProviderScope<NavKey>.mediaPickerEntry(
    onNavigateUp: () -> Unit,
    onPlayVideo: (uri: Uri) -> Unit,
    onPlayVideos: (uris: List<Uri>) -> Unit,
    onFolderClick: (folderPath: String) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    entry<MediaPickerRoute> { key ->
        val output = MediaPickerViewModel.Output(
            navigateUp = onNavigateUp,
            playVideo = onPlayVideo,
            playVideos = onPlayVideos,
            openFolder = onFolderClick,
            openSettings = onSettingsClick,
            openSearch = onSearchClick,
            openPrivacy = onPrivacyClick,
        )
        val viewModel = koinViewModel<MediaPickerViewModel>(
            parameters = {
                parametersOf(
                    MediaPickerViewModel.Input(folderId = key.folderId),
                    output,
                )
            },
        )
        SideEffect { viewModel.output = output }
        MediaPickerScreen(viewModel = viewModel)
    }
}

@Serializable
object PrivacyRoute : NavKey

fun NavBackStack<NavKey>.navigateToPrivacy() {
    add(element = PrivacyRoute)
}

fun EntryProviderScope<NavKey>.privacyEntry(
    onNavigateUp: () -> Unit,
    onPlayVideo: (uri: Uri) -> Unit,
    onPlayVideos: (uris: List<Uri>) -> Unit,
) {
    entry<PrivacyRoute> {
        val output = PrivacyViewModel.Output(
            playVideo = onPlayVideo,
            playVideos = onPlayVideos,
            navigateUp = onNavigateUp,
        )
        val viewModel = koinViewModel<PrivacyViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        PrivacyScreen(viewModel = viewModel)
    }
}

@Serializable
object SearchRoute : NavKey

fun NavBackStack<NavKey>.navigateToSearch() {
    add(element = SearchRoute)
}

fun EntryProviderScope<NavKey>.searchEntry(
    onNavigateUp: () -> Unit,
    onPlayVideo: (uri: Uri) -> Unit,
    onFolderClick: (folderPath: String) -> Unit,
) {
    entry<SearchRoute> {
        val output = SearchViewModel.Output(
            playVideo = onPlayVideo,
            navigateUp = onNavigateUp,
            openFolder = onFolderClick,
        )
        val viewModel = koinViewModel<SearchViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        SearchScreen(viewModel = viewModel)
    }
}
