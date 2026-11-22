package com.sipun.playora.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.sipun.playora.settings.Setting
import com.sipun.playora.settings.navigation.appearancePreferencesEntry
import com.sipun.playora.settings.navigation.aboutEntry
import com.sipun.playora.settings.navigation.navigateToAbout
import com.sipun.playora.settings.navigation.folderPreferencesEntry
import com.sipun.playora.settings.navigation.generalPreferencesEntry
import com.sipun.playora.settings.navigation.gesturePreferencesEntry
import com.sipun.playora.settings.navigation.navigateToAppearancePreferences
import com.sipun.playora.settings.navigation.navigateToFolderPreferencesScreen
import com.sipun.playora.settings.navigation.navigateToGeneralPreferences
import com.sipun.playora.settings.navigation.navigateToGesturePreferences
import com.sipun.playora.settings.navigation.navigateToPlayerPreferences
import com.sipun.playora.settings.navigation.navigateToSubtitlePreferences
import com.sipun.playora.settings.navigation.playerPreferencesEntry
import com.sipun.playora.settings.navigation.settingsEntry
import com.sipun.playora.settings.navigation.subtitlePreferencesEntry

fun EntryProviderScope<NavKey>.settingsNavGraph(backStack: NavBackStack<NavKey>) {
    settingsEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
        onItemClick = { setting ->
            when (setting) {
                Setting.APPEARANCE -> backStack.navigateToAppearancePreferences()
                Setting.PLAYER -> backStack.navigateToPlayerPreferences()
                Setting.GESTURES -> backStack.navigateToGesturePreferences()
                Setting.SUBTITLE -> backStack.navigateToSubtitlePreferences()
                Setting.GENERAL -> backStack.navigateToGeneralPreferences()
                Setting.ABOUT -> backStack.navigateToAbout()
            }
        },
    )
    appearancePreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
    )
    folderPreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
    )
    playerPreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
    )
    gesturePreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
    )
    subtitlePreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
    )
    aboutEntry(onNavigateUp = { backStack.removeLastIfNotRoot() })
    generalPreferencesEntry(
        onNavigateUp = { backStack.removeLastIfNotRoot() },
        onFolderSettingClick = backStack::navigateToFolderPreferencesScreen,
    )
}
