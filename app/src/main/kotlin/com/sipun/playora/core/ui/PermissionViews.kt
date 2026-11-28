package com.sipun.playora.core.ui.composables

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sipun.playora.R

@Composable
fun PermissionDetailView(text: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.permission_not_granted),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 5.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 5.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = ("package:" + context.packageName).toUri()
                    context.startActivity(this)
                }
            },
        ) { Text(text = stringResource(R.string.open_settings)) }
    }
}

@Composable
fun PermissionRationaleDialog(
    text: String,
    modifier: Modifier = Modifier,
    onConfirmButtonClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        modifier = modifier,
        title = {
            Text(text = stringResource(R.string.permission_request))
        },
        text = { Text(text = text) },
        confirmButton = {
            Button(onClick = onConfirmButtonClick) {
                Text(text = stringResource(R.string.grant_permission))
            }
        },
    )
}

@Composable
fun PermissionMissingView(
    isGranted: Boolean,
    showRationale: Boolean,
    permission: String,
    launchPermissionRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isGranted) {
        content()
    } else if (showRationale) {
        PermissionRationaleDialog(
            text = stringResource(
                id = R.string.permission_info,
                permission,
            ),
            onConfirmButtonClick = launchPermissionRequest,
        )
    } else {
        PermissionDetailView(
            text = stringResource(
                id = R.string.permission_settings,
                permission,
            ),
        )
        LaunchedEffect(key1 = Unit) { launchPermissionRequest() }
    }
}
