package com.sipun.playora.settings

import android.content.Intent
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.core.net.toUri
import com.sipun.playora.BuildConfig
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.ui.theme.LocalAccentColor
import com.sipun.playora.core.update.AppUpdate
import com.sipun.playora.core.update.UpdateManager
import com.sipun.playora.feature.onboarding.WelcomeScreen
import kotlinx.coroutines.launch

private const val REPOSITORY_URL = "https://github.com/Darkstar085/playora"
private const val LICENSE_URL = "$REPOSITORY_URL/blob/main/LICENSE"
private const val CONTACT_URL = "https://t.me/Darkstar085"
private const val SUPPORT_URL = "upi://pay?pa=darkstar085@upi&pn=Playora&cu=INR"

data class AboutOutput(val navigateUp: () -> Unit)

@Composable
fun AboutScreen(output: AboutOutput) {
    val context = LocalContext.current
    val updateCheckFailedText = stringResource(R.string.update_check_failed)
    val updateInstallPermissionText = stringResource(R.string.update_install_permission)
    val updateDownloadFailedText = stringResource(R.string.update_download_failed)
    val accent = LocalAccentColor.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var updateFlowState by remember { mutableStateOf<UpdateFlowState?>(null) }
    var downloadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var showWelcomeTour by remember { mutableStateOf(false) }

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
                    modifier = Modifier.clickable {
                        // Allows instant UI review of the new updater flow
                        updateFlowState = UpdateFlowState.Available(
                            AppUpdate(
                                version = "1.0.0",
                                name = "Playora 1.0.0",
                                notes = """
                                    - Prepare Playora 1.0.0
                                    - Refine folder list layout and spacing
                                    - Migrate sliders to stable Material 3
                                    - Add in-app GitHub release updates
                                    - Add app about page
                                    - Use device accent for launcher icon
                                    - Improve playback performance
                                    - Memory optimization for video lists
                                    - Modern audio and gesture settings
                                    - Refined player gesture controls
                                """.trimIndent(),
                                downloadUrl = "https://github.com/Darkstar085/playora/releases/latest",
                                fileName = "playora-1.0.0.apk",
                                size = 51065651L,
                                digest = "sha256:0000000000000000000000000000000000000000000000000000000000000000",
                                releaseDate = "Nov 20, 2026",
                                isPrerelease = false,
                            ),
                        )
                    },
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
                            scope.launch {
                                try {
                                    val latest = UpdateManager.findLatestUpdate(context)
                                    updateFlowState = if (latest != null) {
                                        UpdateFlowState.Available(latest)
                                    } else {
                                        UpdateFlowState.UpToDate(BuildConfig.VERSION_NAME)
                                    }
                                } catch (error: Exception) {
                                    updateFlowState = UpdateFlowState.Error(
                                        error.message ?: updateCheckFailedText,
                                    )
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
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            REPOSITORY_URL.toUri(),
                        ),
                    )
                },
            )
            AboutLinkCard(
                icon = AppIcons.BugReport,
                title = stringResource(R.string.about_contact),
                description = stringResource(R.string.about_contact_description),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            CONTACT_URL.toUri(),
                        ),
                    )
                },
            )
            AboutLinkCard(
                icon = AppIcons.Headset,
                title = stringResource(R.string.about_support),
                description = stringResource(R.string.about_support_description),
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            SUPPORT_URL.toUri(),
                        ),
                    )
                },
            )
            AboutLinkCard(
                icon = AppIcons.DoubleTap,
                title = stringResource(R.string.about_welcome_tour),
                description = stringResource(R.string.about_welcome_tour_description),
                onClick = { showWelcomeTour = true },
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
                        context.startActivity(Intent(Intent.ACTION_VIEW, LICENSE_URL.toUri()))
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

    updateFlowState?.let { flowState ->
        UpdateDialog(
            state = flowState,
            onDismiss = {
                downloadJob?.cancel()
                downloadJob = null
                updateFlowState = null
            },
            onUpdateNow = { selectedUpdate ->
                downloadJob = scope.launch {
                    updateFlowState = UpdateFlowState.Downloading(
                        update = selectedUpdate,
                        downloadedBytes = 0L,
                        totalBytes = selectedUpdate.size,
                        progressPercent = 0,
                    )
                    try {
                        val apk =
                            UpdateManager.download(context, selectedUpdate) { downloaded, total ->
                                val percent = if (total > 0) {
                                    ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                                } else {
                                    0
                                }
                                updateFlowState = UpdateFlowState.Downloading(
                                    update = selectedUpdate,
                                    downloadedBytes = downloaded,
                                    totalBytes = total,
                                    progressPercent = percent,
                                )
                            }
                        updateFlowState = UpdateFlowState.Installing(selectedUpdate)
                        updateFlowState = if (UpdateManager.install(context, apk)) {
                            UpdateFlowState.Success(selectedUpdate.version)
                        } else {
                            UpdateFlowState.Error(updateInstallPermissionText)
                        }
                    } catch (_: kotlinx.coroutines.CancellationException) {
                        updateFlowState = null
                    } catch (e: Exception) {
                        updateFlowState = UpdateFlowState.Error(
                            e.message ?: updateDownloadFailedText,
                        )
                    }
                }
            },
            onCancelDownload = {
                downloadJob?.cancel()
                downloadJob = null
                updateFlowState = null
            },
        )
    }

    if (showWelcomeTour) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showWelcomeTour = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
            WelcomeScreen(onFinish = { showWelcomeTour = false })
        }
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
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
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
