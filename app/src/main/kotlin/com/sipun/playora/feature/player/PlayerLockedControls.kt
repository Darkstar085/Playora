package com.sipun.playora.feature.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.feature.player.buttons.PlayerButton

@Composable
fun PlayerLockedControls(
    onUnlockControls: () -> Unit,
    modifier: Modifier = Modifier,
    unlockButtonModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(top = 24.dp),
    ) {
        PlayerButton(
            modifier = unlockButtonModifier,
            containerColor = Color.Black.copy(alpha = 0.5f),
            onClick = onUnlockControls,
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = stringResource(R.string.controls_unlock),
            )
        }
    }
}
