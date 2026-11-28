package com.sipun.playora.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sipun.playora.BuildConfig
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.AppBackButton
import com.sipun.playora.core.ui.components.AppTopAppBar
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassBackdropBox
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.ui.theme.LocalAccentColor
import com.sipun.playora.core.ui.theme.LocalGlassBackdrop
import com.sipun.playora.core.ui.theme.LocalLiquidGlassEnabled
import com.sipun.playora.core.ui.theme.rememberGlassCardBorderBrush
import com.sipun.playora.core.ui.theme.rememberGlassCardTint
import com.sipun.playora.core.update.UpdateManager
import io.github.itshedimisawi.vitro.LocalGlassDarkTheme
import io.github.itshedimisawi.vitro.rememberGlassBackdrop
import kotlinx.coroutines.launch

private const val REPOSITORY_URL = "https://github.com/Darkstar085/playora"
private const val LICENSE_URL = "$REPOSITORY_URL/blob/main/LICENSE"
private const val CONTACT_URL = "https://t.me/Darkstar085"
private const val SUPPORT_URL = "upi://pay?pa=darkstar085@upi&pn=Playora&cu=INR"

data class AboutOutput(val navigateUp: () -> Unit)

@Composable
fun AboutScreen(output: AboutOutput) {
    AboutScreenContent(navigateUp = output.navigateUp)
}

@Composable
internal fun AboutScreenContent(navigateUp: () -> Unit) {
    val context = LocalContext.current
    val updateCheckFailedText = stringResource(R.string.update_check_failed)
    val updateInstallPermissionText = stringResource(R.string.update_install_permission)
    val updateDownloadFailedText = stringResource(R.string.update_download_failed)
    val accent = LocalAccentColor.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var updateFlowState by remember { mutableStateOf<UpdateFlowState?>(null) }
    var downloadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val backdrop = rememberGlassBackdrop()
    val containerColor = if (liquidGlassEnabled) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val startUpdateCheck = {
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
    }

    CompositionLocalProvider(
        LocalGlassBackdrop provides if (liquidGlassEnabled) backdrop else null,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    AppTopAppBar(
                        title = stringResource(R.string.about),
                        navigationIcon = { AppBackButton(onClick = navigateUp) },
                    )
                },
                containerColor = containerColor,
            ) { innerPadding ->
                AppGlassBackdropBox(
                    backdrop = backdrop,
                    enabled = liquidGlassEnabled,
                    pageColor = containerColor,
                    topPadding = innerPadding.calculateTopPadding(),
                    bottomPadding = innerPadding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxWidth()
                                .padding(innerPadding)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                        val isDark = LocalGlassDarkTheme.current ?: isSystemInDarkTheme()
                        val heroShape = RoundedCornerShape(24.dp)
                        val cardBorderBrush = rememberGlassCardBorderBrush()
                        val cardTint = rememberGlassCardTint()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(heroShape)
                                .then(
                                    if (liquidGlassEnabled) {
                                        Modifier
                                            .background(cardTint, heroShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        accent.copy(alpha = if (isDark) 0.16f else 0.10f),
                                                        Color.Transparent,
                                                    ),
                                                ),
                                                heroShape,
                                            )
                                            .border(0.85.dp, cardBorderBrush, heroShape)
                                    } else {
                                        Modifier
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        accent.copy(alpha = 0.16f),
                                                        MaterialTheme.colorScheme.surfaceContainerHigh,
                                                        accent.copy(alpha = 0.08f),
                                                    ),
                                                ),
                                                heroShape,
                                            )
                                            .border(
                                                0.85.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                                heroShape,
                                            )
                                    },
                                )
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
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = accent.copy(alpha = if (isDark) 0.18f else 0.12f),
                                border = BorderStroke(
                                    0.75.dp,
                                    accent.copy(alpha = if (isDark) 0.32f else 0.24f),
                                ),
                            ) {
                                Text(
                                    text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
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
                            if (liquidGlassEnabled) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(0.84f)
                                        .height(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                                                ),
                                            ),
                                            CircleShape,
                                        )
                                        .border(
                                            width = 0.85.dp,
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.55f),
                                                    Color.White.copy(alpha = 0.14f),
                                                ),
                                            ),
                                            shape = CircleShape,
                                        )
                                        .clickable(
                                            enabled = !isChecking,
                                            onClick = startUpdateCheck,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    if (isChecking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = AppIcons.Update,
                                            contentDescription = null,
                                            tint = Color.White,
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(
                                            if (isChecking) R.string.update_checking else R.string.check_for_updates,
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                    )
                                }
                            } else {
                                Button(
                                    onClick = startUpdateCheck,
                                    enabled = !isChecking,
                                    modifier = Modifier.fillMaxWidth(0.84f),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    if (isChecking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = AppIcons.Update,
                                            contentDescription = null,
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        stringResource(
                                            if (isChecking) R.string.update_checking else R.string.check_for_updates,
                                        ),
                                    )
                                }
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
                            icon = ImageVector.vectorResource(R.drawable.ic_github),
                            title = stringResource(R.string.about_source),
                            description = stringResource(R.string.about_source_description),
                            onClick = { openExternalUri(context, REPOSITORY_URL) },
                        )
                        AboutLinkCard(
                            icon = AppIcons.BugReport,
                            title = stringResource(R.string.about_contact),
                            description = stringResource(R.string.about_contact_description),
                            onClick = { openExternalUri(context, CONTACT_URL) },
                        )
                        AboutLinkCard(
                            icon = AppIcons.Favorite,
                            title = stringResource(R.string.about_support),
                            description = stringResource(R.string.about_support_description),
                            onClick = { openExternalUri(context, SUPPORT_URL) },
                        )
                        val licenseShape = RoundedCornerShape(22.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(licenseShape)
                                .then(
                                    if (liquidGlassEnabled) {
                                        Modifier
                                            .background(cardTint, licenseShape)
                                            .border(0.85.dp, cardBorderBrush, licenseShape)
                                    } else {
                                        Modifier
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, licenseShape)
                                            .border(
                                                0.85.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                                licenseShape,
                                            )
                                    },
                                )
                                .clickable { openExternalUri(context, LICENSE_URL) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AboutIconTile(icon = AppIcons.Info)
                            Spacer(Modifier.width(12.dp))
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
                                    null
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
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (liquidGlassEnabled) {
                    Modifier
                        .background(rememberGlassCardTint(), shape)
                        .border(0.85.dp, rememberGlassCardBorderBrush(), shape)
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
                        .border(
                            0.85.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape,
                        )
                },
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
                fontWeight = FontWeight.SemiBold,
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
private fun AboutIconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LocalAccentBrush.current),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

private fun openExternalUri(context: Context, uri: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }.onFailure {
        Toast.makeText(
            context,
            context.getString(R.string.no_app_found),
            Toast.LENGTH_SHORT,
        ).show()
    }
}
