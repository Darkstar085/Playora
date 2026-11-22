package com.sipun.playora.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.designsystem.AppIcons

data class SettingsOutput(val navigateUp: () -> Unit, val openSetting: (Setting) -> Unit)

private sealed interface SettingsAction {
    data object NavigateUp : SettingsAction
    data class OpenSetting(val setting: Setting) : SettingsAction
}

@Composable
fun SettingsScreen(output: SettingsOutput) {
    SettingsScreenContent { action ->
        when (action) {
            is SettingsAction.NavigateUp -> output.navigateUp()
            is SettingsAction.OpenSetting -> output.openSetting(action.setting)
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
private fun SettingsScreenContent(onAction: (SettingsAction) -> Unit) {
    val settingRows = remember { SettingRow.entries }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.settings),
                navigationIcon = {
                    AppBackButton(onClick = { onAction(SettingsAction.NavigateUp) })
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            settingRows.forEachIndexed { index, row ->
                key(row) {
                    ClickablePreferenceItem(
                        title = stringResource(id = row.titleResId),
                        description = stringResource(id = row.descriptionResId),
                        icon = row.icon,
                        onClick = { onAction(SettingsAction.OpenSetting(row.setting)) },
                        isFirstItem = index == 0,
                        isLastItem = index == settingRows.lastIndex,
                    )
                }
            }
        }
    }
}

enum class Setting {
    APPEARANCE,
    PLAYER,
    GESTURES,
    SUBTITLE,
    GENERAL,
}

private enum class SettingRow(
    val titleResId: Int,
    val descriptionResId: Int,
    val icon: ImageVector,
    val setting: Setting,
) {
    APPEARANCE(
        titleResId = R.string.appearance_name,
        descriptionResId = R.string.appearance_description,
        icon = AppIcons.Appearance,
        setting = Setting.APPEARANCE,
    ),
    PLAYER(
        titleResId = R.string.player_name,
        descriptionResId = R.string.player_description,
        icon = AppIcons.Player,
        setting = Setting.PLAYER,
    ),
    GESTURES(
        titleResId = R.string.gestures,
        descriptionResId = R.string.gestures_description,
        icon = AppIcons.SwipeHorizontal,
        setting = Setting.GESTURES,
    ),
    SUBTITLE(
        titleResId = R.string.subtitle,
        descriptionResId = R.string.subtitle_desc,
        icon = AppIcons.Subtitle,
        setting = Setting.SUBTITLE,
    ),
    GENERAL(
        titleResId = R.string.general_name,
        descriptionResId = R.string.general_description,
        icon = AppIcons.ExtraSettings,
        setting = Setting.GENERAL,
    ),
}
