package com.sipun.playora.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.update.AppUpdate
import java.util.Locale

sealed interface UpdateFlowState {
    data class Available(val update: AppUpdate) : UpdateFlowState
    data class Downloading(
        val update: AppUpdate,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int,
    ) : UpdateFlowState
    data class Installing(val update: AppUpdate) : UpdateFlowState
    data class Success(val version: String) : UpdateFlowState
    data class UpToDate(val version: String) : UpdateFlowState
    data class Error(val message: String) : UpdateFlowState
}

private enum class UpdateStep {
    AVAILABLE,
    DOWNLOADING,
    INSTALLING,
    SUCCESS,
    UP_TO_DATE,
    ERROR,
}

private val UpdateFlowState.step: UpdateStep
    get() = when (this) {
        is UpdateFlowState.Available -> UpdateStep.AVAILABLE
        is UpdateFlowState.Downloading -> UpdateStep.DOWNLOADING
        is UpdateFlowState.Installing -> UpdateStep.INSTALLING
        is UpdateFlowState.Success -> UpdateStep.SUCCESS
        is UpdateFlowState.UpToDate -> UpdateStep.UP_TO_DATE
        is UpdateFlowState.Error -> UpdateStep.ERROR
    }

private val DialogBackground = Color(0xFF16181E)
private val DialogBorder = Color(0xFF262B38)
private val CardBackground = Color(0xFF191D27)
private val CardBorder = Color(0xFF262C3A)
private val SectionBackground = Color(0xFF12141A)
private val SectionBorder = Color(0xFF222632)
private val AccentBlue = Color(0xFF1D68FE)
private val BulletBlue = Color(0xFF388AF6)
private val SuccessGreen = Color(0xFF22C55E)
private val PillGreenBg = Color(0xFF132E20)
private val PillGreenText = Color(0xFF4ADE80)
private val NeutralPillBg = Color(0xFF212532)

@Composable
fun UpdateDialog(
    state: UpdateFlowState,
    onDismiss: () -> Unit,
    onUpdateNow: (AppUpdate) -> Unit,
    onCancelDownload: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (state !is UpdateFlowState.Downloading && state !is UpdateFlowState.Installing) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = state !is UpdateFlowState.Downloading && state !is UpdateFlowState.Installing,
            dismissOnClickOutside = state !is UpdateFlowState.Downloading && state !is UpdateFlowState.Installing,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(28.dp),
            color = DialogBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, DialogBorder),
        ) {
            AnimatedContent(
                targetState = state.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "update_flow_animation",
            ) { targetStep ->
                when (targetStep) {
                    UpdateStep.AVAILABLE -> (state as? UpdateFlowState.Available)?.let {
                        UpdateAvailableContent(
                            update = it.update,
                            onDismiss = onDismiss,
                            onUpdateNow = { onUpdateNow(it.update) },
                        )
                    }

                    UpdateStep.DOWNLOADING -> (state as? UpdateFlowState.Downloading)?.let {
                        UpdateDownloadingContent(
                            state = it,
                            onCancel = onCancelDownload,
                        )
                    }

                    UpdateStep.INSTALLING -> UpdateInstallingContent()

                    UpdateStep.SUCCESS -> (state as? UpdateFlowState.Success)?.let {
                        UpdateSuccessContent(
                            version = it.version,
                            onDismiss = onDismiss,
                        )
                    }

                    UpdateStep.UP_TO_DATE -> (state as? UpdateFlowState.UpToDate)?.let {
                        UpdateUpToDateContent(
                            version = it.version,
                            onDismiss = onDismiss,
                        )
                    }

                    UpdateStep.ERROR -> (state as? UpdateFlowState.Error)?.let {
                        UpdateErrorContent(
                            message = it.message,
                            onDismiss = onDismiss,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    update: AppUpdate,
    onDismiss: () -> Unit,
    onUpdateNow: () -> Unit,
) {
    val changelogItems = remember(update.notes) {
        val parsed = update.notes
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { line ->
                line.removePrefix("-")
                    .removePrefix("*")
                    .removePrefix("•")
                    .trim()
            }
            .filter { it.isNotEmpty() }

        parsed.ifEmpty {
            listOf(
                "Prepare Playora ${update.version}",
                "Refine folder list layout and spacing",
                "Performance optimizations and bug fixes",
            )
        }
    }

    val formattedSize = remember(update.size) { formatFileSize(update.size) }
    val displayDate = if (update.releaseDate.isNotBlank()) update.releaseDate else "Nov 20, 2026"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top row with close X button
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeutralPillBg)
                    .clickable(onClick = onDismiss)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // App icon with download badge
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(LocalAccentBrush.current),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Play,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen)
                    .border(2.dp, DialogBackground, CircleShape)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.ArrowDownward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Title and Subtitle
        Text(
            text = stringResource(R.string.update_available_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.update_available_subtitle),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        // Version & Status Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = NeutralPillBg,
            ) {
                Text(
                    text = "v${update.version}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }

            Surface(
                shape = CircleShape,
                color = PillGreenBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = stringResource(if (update.isPrerelease) R.string.update_prerelease else R.string.update_stable),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PillGreenText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Info Cards (Download size & Release date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InfoCard(
                icon = AppIcons.ArrowDownward,
                title = formattedSize,
                subtitle = stringResource(R.string.update_download_size),
                modifier = Modifier.weight(1f),
            )
            InfoCard(
                icon = AppIcons.Calendar,
                title = displayDate,
                subtitle = stringResource(R.string.update_release_date),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // What's new section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.update_whats_new),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Surface(
                shape = CircleShape,
                color = NeutralPillBg,
            ) {
                Text(
                    text = stringResource(R.string.update_changes, changelogItems.size),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Changelog Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SectionBackground)
                .border(1.dp, SectionBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 180.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                changelogItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            color = BulletBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                            ),
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = onUpdateNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = AppIcons.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.update_now),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CardBackground,
                contentColor = Color.White.copy(alpha = 0.85f),
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        ) {
            Text(
                text = stringResource(R.string.update_later),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeutralPillBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp),
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun UpdateDownloadingContent(
    state: UpdateFlowState.Downloading,
    onCancel: () -> Unit,
) {
    val downloadedMB = formatFileSize(state.downloadedBytes)
    val totalMB = formatFileSize(state.totalBytes.takeIf { it > 0 } ?: state.update.size)
    val progress = state.progressPercent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LocalAccentBrush.current),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Play,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.update_downloading),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = Color.White,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "v${state.update.version} • $totalMB",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(20.dp))

        // Linear progress bar
        LinearProgressIndicator(
            progress = { (progress / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = AccentBlue,
            trackColor = NeutralPillBg,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "$downloadedMB of $totalMB ($progress%)",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.7f),
        )

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier.height(38.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeutralPillBg,
                contentColor = Color.White.copy(alpha = 0.85f),
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        ) {
            Text(
                text = stringResource(R.string.cancel),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun UpdateInstallingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(52.dp),
            color = AccentBlue,
            strokeWidth = 4.dp,
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.update_installing),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = Color.White,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.update_installing_desc),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun UpdateSuccessContent(
    version: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PillGreenBg)
                .border(2.dp, SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.update_success_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
            color = Color.White,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.update_success_desc, version),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.65f),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.okay),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun UpdateUpToDateContent(
    version: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PillGreenBg)
                .border(2.dp, SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.update_up_to_date),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Playora v$version",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.65f),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.okay),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun UpdateErrorContent(
    message: String,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF351717))
                .border(2.dp, Color(0xFFEF4444), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Info,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.update_failed),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = Color.White,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.okay),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", mb)
}
