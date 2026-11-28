package com.sipun.playora.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sipun.playora.feature.player.PlayerActivity
import com.sipun.playora.feature.player.utils.PlayerApi
import com.sipun.playora.feature.videopicker.navigation.mediaPickerEntry
import com.sipun.playora.feature.videopicker.navigation.navigateToMediaPickerScreen
import com.sipun.playora.feature.videopicker.navigation.navigateToPrivacy
import com.sipun.playora.feature.videopicker.navigation.navigateToSearch
import com.sipun.playora.feature.videopicker.navigation.searchEntry
import com.sipun.playora.settings.navigation.navigateToSettings

fun EntryProviderScope<NavKey>.mediaNavGraph(context: Context, backStack: NavBackStack<NavKey>) {
    mediaPickerEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
        onPlayVideo = { uri, playlist ->
            if (!playlist.isNullOrEmpty()) {
                context.startPlayback(uris = playlist, startUri = uri)
            } else {
                context.startPlayback(uri = uri)
            }
        },
        onPlayVideos = { uris -> context.startPlayback(uris = uris) },
        onFolderClick = backStack::navigateToMediaPickerScreen,
        onSettingsClick = backStack::navigateToSettings,
        onSearchClick = backStack::navigateToSearch,
        onPrivacyClick = backStack::navigateToPrivacy,
    )

    searchEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
        onPlayVideo = { uri, playlist ->
            if (!playlist.isNullOrEmpty()) {
                context.startPlayback(uris = playlist, startUri = uri)
            } else {
                context.startPlayback(uri = uri)
            }
        },
        onFolderClick = backStack::navigateToMediaPickerScreen,
    )
}

internal fun Context.startPlayback(uri: Uri, grantReadPermission: Boolean = false) {
    startPlayback(
        uri = uri,
        playlist = null,
        grantReadPermission = grantReadPermission,
    )
}

internal fun Context.startPlayback(
    uris: List<Uri>,
    startUri: Uri? = null,
    grantReadPermission: Boolean = false,
) {
    val uri = startUri ?: uris.firstOrNull() ?: return
    startPlayback(
        uri = uri,
        playlist = uris,
        grantReadPermission = grantReadPermission,
    )
}

private fun Context.startPlayback(uri: Uri, playlist: List<Uri>?, grantReadPermission: Boolean) {
    runCatching {
        sendBroadcast(Intent(PlayerActivity.ACTION_CANCEL_PIP).setPackage(packageName))
    }
    if (grantReadPermission) {
        (playlist ?: listOf(element = uri)).forEach {
            grantUriPermission(
                packageName,
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
    val intent = Intent(
        this,
        PlayerActivity::class.java,
    ).apply {
        action = Intent.ACTION_VIEW
        data = uri
        playlist?.let {
            putExtra(
                "video_list_is_explicit",
                true,
            )
            putParcelableArrayListExtra(
                PlayerApi.API_PLAYLIST,
                ArrayList(it),
            )
        }
        if (grantReadPermission) addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(intent)
}
