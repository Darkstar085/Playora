package com.sipun.playora.feature.videopicker.composables.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.ClickablePreferenceItem
import com.sipun.playora.core.ui.components.PreferenceSwitch
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassModalOverlay

@Composable
internal fun PrivacyBiometricSetupDialog(onComplete: (Boolean) -> Unit) {
    if (!rememberPrivacyBiometricAvailable()) {
        LaunchedEffect(key1 = Unit) { onComplete(false) }
        return
    }

    var authenticating by rememberSaveable { mutableStateOf(value = false) }
    AppDialog(
        onDismissRequest = { onComplete(false) },
        title = { Text(text = stringResource(R.string.privacy_biometric_setup_title)) },
        content = { Text(text = stringResource(R.string.privacy_biometric_setup_description)) },
        confirmButton = {
            TextButton(
                onClick = { authenticating = true },
                enabled = !authenticating,
            ) {
                Text(text = stringResource(R.string.enable_biometric_unlock))
            }
        },
        dismissButton = {
            TextButton(onClick = { onComplete(false) }) {
                Text(text = stringResource(R.string.not_now))
            }
        },
    )

    if (authenticating) {
        PrivacyBiometricPrompt(
            title = stringResource(R.string.enable_biometric_unlock),
            negativeButtonText = stringResource(R.string.cancel),
            onResult = { authenticated ->
                authenticating = false
                if (authenticated) onComplete(true)
            },
        )
    }
}

@Composable
fun PrivacyProgressDialog(message: String) {
    AppGlassModalOverlay(
        onDismissRequest = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 20.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
internal fun PrivacySettingsDialog(
    biometricEnabled: Boolean,
    onBiometricEnabledChange: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val available = rememberPrivacyBiometricAvailable()
    var authenticating by rememberSaveable { mutableStateOf(value = false) }

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.privacy_settings)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PreferenceSwitch(
                    title = stringResource(R.string.unlock_with_biometrics),
                    description = stringResource(
                        id = if (available || biometricEnabled) {
                            R.string.privacy_biometric_description
                        } else {
                            R.string.privacy_biometric_unavailable
                        },
                    ),
                    isChecked = biometricEnabled,
                    enabled = !authenticating && (biometricEnabled || available),
                    isFirstItem = true,
                    isLastItem = false,
                    onClick = {
                        if (biometricEnabled) onBiometricEnabledChange(false) else authenticating = true
                    },
                )
                ClickablePreferenceItem(
                    title = stringResource(R.string.change_privacy_pin),
                    description = stringResource(R.string.change_privacy_pin_desc),
                    icon = AppIcons.Lock,
                    isFirstItem = false,
                    isLastItem = true,
                    onClick = {
                        onDismiss()
                        onChangePinClick()
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.done)) }
        },
    )

    if (authenticating) {
        PrivacyBiometricPrompt(
            title = stringResource(R.string.enable_biometric_unlock),
            negativeButtonText = stringResource(R.string.cancel),
            onResult = { authenticated ->
                authenticating = false
                if (authenticated) onBiometricEnabledChange(true)
            },
        )
    }
}
