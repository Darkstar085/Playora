package com.sipun.playora.feature.videopicker.composables.privacy

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons

@Composable
internal fun PrivacyBiometricButton(onAuthenticated: () -> Unit, modifier: Modifier = Modifier) {
    if (!rememberPrivacyBiometricAvailable()) return

    var showPrompt by rememberSaveable { mutableStateOf(value = true) }
    if (showPrompt) {
        PrivacyBiometricPrompt(
            title = stringResource(R.string.open_privacy),
            negativeButtonText = stringResource(R.string.use_privacy_pin),
            onResult = { authenticated ->
                showPrompt = false
                if (authenticated) onAuthenticated()
            },
        )
    }

    TextButton(
        modifier = modifier,
        onClick = { showPrompt = true },
    ) {
        Icon(
            imageVector = AppIcons.Fingerprint,
            contentDescription = null,
        )
        Spacer(Modifier.size(8.dp))
        Text(text = stringResource(R.string.unlock_with_biometrics))
    }
}

@Composable
internal fun rememberPrivacyBiometricAvailable(): Boolean {
    val activity = LocalActivity.current as? FragmentActivity ?: return false
    val manager = remember(activity) { BiometricManager.from(activity) }
    var available by remember(manager) {
        mutableStateOf(
            value = manager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS,
        )
    }
    LifecycleResumeEffect(key1 = manager) {
        available = manager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        onPauseOrDispose {}
    }
    return available
}

@Composable
internal fun PrivacyBiometricPrompt(
    title: String,
    negativeButtonText: String,
    onResult: (Boolean) -> Unit,
) {
    val activity = LocalActivity.current as? FragmentActivity ?: return

    val currentOnResult by rememberUpdatedState(newValue = onResult)
    var active by remember(activity) { mutableStateOf(value = false) }
    val prompt = remember(activity) {
        BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    if (!active) return
                    active = false
                    currentOnResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!active) return
                    active = false
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        Toast.makeText(
                            activity,
                            errString,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    currentOnResult(false)
                }
            },
        )
    }

    DisposableEffect(prompt) {
        onDispose {
            if (active) {
                active = false
                prompt.cancelAuthentication()
            }
        }
    }

    LaunchedEffect(prompt) {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
        active = true
        prompt.authenticate(info)
    }
}
