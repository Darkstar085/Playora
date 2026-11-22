package com.sipun.playora.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sipun.playora.core.update.AppUpdate
import com.sipun.playora.core.update.UpdateManager
import kotlinx.coroutines.launch
import java.io.File
import com.sipun.playora.BuildConfig
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.ui.theme.LocalAccentColor

private const val REPOSITORY_URL = "https://github.com/Darkstar085/playora"
private const val RELEASES_URL = "$REPOSITORY_URL/releases/latest"
private const val ISSUES_URL = "$REPOSITORY_URL/issues"
private const val LICENSE_URL = "$REPOSITORY_URL/blob/main/LICENSE"
private const val CONTACT_URL = "https://t.me/Darkstar085"
private const val SUPPORT_URL = "upi://pay?pa=darkstar085@upi&pn=Playora&cu=INR"

data class AboutOutput(val navigateUp: () -> Unit)

@Composable
fun AboutScreen(output: AboutOutput) {
    val context = LocalContext.current
    val accent = LocalAccentColor.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    var downloadedApk by remember { mutableStateOf<File?>(null) }
    var updateMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(R.string.about),
                navigationIcon = { AppBackButton(onClick = output.navigateUp) },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accent.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                accent.copy(alpha = 0.10f),
                            ),
                        ),
                    )
                    .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(LocalAccentBrush.current),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Play,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (!isChecking) {
                            isChecking = true
                            updateMessage = null
                            scope.launch {
                                try {
                                    update = UpdateManager.findLatestUpdate(context)
                                    downloadedApk = null
                                    updateMessage = if (update == null) context.getString(R.string.update_up_to_date) else null
                                } catch (error: Exception) {
                                    updateMessage = error.message ?: context.getString(R.string.update_check_failed)
                                } finally {
                                    isChecking = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.82f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = AppIcons.Update,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(if (isChecking) R.string.update_checking else R.string.check_for_updates))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Info,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.about_update_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = stringResource(R.string.get_involved).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
            AboutLinkCard(
                icon = AppIcons.Link,
                title = stringResource(R.string.about_source),
                description = stringResource(R.string.about_source_description),
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL))) },
            )
            AboutLinkCard(
                icon = AppIcons.BugReport,
                title = stringResource(R.string.about_contact),
                description = stringResource(R.string.about_contact_description),
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CONTACT_URL))) },
            )
            AboutLinkCard(
                icon = AppIcons.Headset,
                title = stringResource(R.string.about_support),
                description = stringResource(R.string.about_support_description),
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL))) },
            )

            Text(
                text = stringResource(R.string.about_section).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(18.dp),
                    )
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LICENSE_URL)))
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AboutIconTile(icon = AppIcons.Info, compact = true)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.license),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.about_license_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                )
            }
        }
    }

    if (update != null || updateMessage != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isDownloading) {
                    update = null
                    updateMessage = null
                }
            },
            title = { Text(update?.name ?: stringResource(R.string.update_status)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        updateMessage ?: update?.notes?.ifBlank {
                            context.getString(R.string.update_available, update?.version.orEmpty())
                        }.orEmpty(),
                    )
                    if (isDownloading) {
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(stringResource(R.string.update_download_progress, downloadProgress))
                    }
                }
            },
            confirmButton = {
                if (update != null) {
                    TextButton(
                        enabled = !isDownloading,
                        onClick = {
                            val selectedUpdate = update ?: return@TextButton
                            scope.launch {
                                if (downloadedApk == null) {
                                    isDownloading = true
                                    downloadProgress = 0
                                    updateMessage = null
                                    try {
                                        downloadedApk = UpdateManager.download(context, selectedUpdate) {
                                            scope.launch { downloadProgress = it }
                                        }
                                    } catch (error: Exception) {
                                        updateMessage = error.message ?: context.getString(R.string.update_download_failed)
                                    } finally {
                                        isDownloading = false
                                    }
                                }
                                downloadedApk?.let { apk ->
                                    if (UpdateManager.install(context, apk)) {
                                        update = null
                                        updateMessage = null
                                    } else {
                                        updateMessage = context.getString(R.string.update_install_permission)
                                    }
                                }
                            }
                        },
                    ) {
                        Text(stringResource(if (downloadedApk == null) R.string.update_download else R.string.update_install))
                    }
                } else {
                    TextButton(onClick = { updateMessage = null }) {
                        Text(stringResource(R.string.okay))
                    }
                }
            },
            dismissButton = {
                if (update != null) {
                    TextButton(
                        enabled = !isDownloading,
                        onClick = {
                            update = null
                            updateMessage = null
                        },
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
        )
    }
}

@Composable
private fun AboutLinkCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AboutIconTile(icon = icon)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AboutIconTile(icon: ImageVector, compact: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (compact) 44.dp else 46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LocalAccentBrush.current),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}
