package com.sipun.playora.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
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
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons
import com.sipun.playora.core.ui.theme.AppGlassModalOverlay
import com.sipun.playora.core.ui.theme.LocalAccentBrush
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.isAppGlassActive
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerBorderColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerContainerColor
import com.sipun.playora.core.update.AppUpdate
import io.github.itshedimisawi.vitro.rememberGlassEffectState
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
    data class UpToDate(val version: String) : UpdateFlowState
    data class Error(val message: String) : UpdateFlowState
}

private enum class UpdateStep {
    AVAILABLE,
    DOWNLOADING,
    INSTALLING,
    UP_TO_DATE,
    ERROR,
}

private val UpdateFlowState.step: UpdateStep
    get() = when (this) {
        is UpdateFlowState.Available -> UpdateStep.AVAILABLE
        is UpdateFlowState.Downloading -> UpdateStep.DOWNLOADING
        is UpdateFlowState.Installing -> UpdateStep.INSTALLING
        is UpdateFlowState.UpToDate -> UpdateStep.UP_TO_DATE
        is UpdateFlowState.Error -> UpdateStep.ERROR
    }

private val SuccessGreen = Color(0xFF22C55E)
private val ErrorRed = Color(0xFFEF4444)

@Composable
fun UpdateDialog(
    state: UpdateFlowState,
    onDismiss: () -> Unit,
    onUpdateNow: (AppUpdate) -> Unit,
    onCancelDownload: () -> Unit,
) {
    val canDismiss = state !is UpdateFlowState.Downloading && state !is UpdateFlowState.Installing
    val useGlass = isAppGlassActive()

    AppGlassModalOverlay(
        onDismissRequest = {
            if (canDismiss) {
                onDismiss()
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 520.dp)
            .heightIn(max = 680.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        border = if (useGlass) {
            null
        } else {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
        },
        dismissOnBackPress = canDismiss,
        dismissOnClickOutside = canDismiss,
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

@Composable
private fun UpdateAvailableContent(
    update: AppUpdate,
    onDismiss: () -> Unit,
    onUpdateNow: () -> Unit,
) {
    val useGlass = isAppGlassActive()
    val innerBg = if (useGlass) rememberGlassInnerContainerColor() else MaterialTheme.colorScheme.surfaceContainerLow
    val innerBorder = if (useGlass) rememberGlassInnerBorderColor() else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val pillBg = if (useGlass) rememberGlassInnerContainerColor() else MaterialTheme.colorScheme.surfaceContainerHighest

    val changelogItems = remember(update.notes) {
        update.notes
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
    }

    val formattedSize = remember(update.size) { formatFileSize(update.size) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val closeInteraction = remember { MutableInteractionSource() }
            val closeState = rememberGlassEffectState()
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .then(
                        if (useGlass) {
                            Modifier.appGlassSurface(
                                shape = CircleShape,
                                interactive = true,
                                state = closeState,
                                interactionSource = closeInteraction,
                                fallbackBackground = innerBg,
                                fallbackBorder = BorderStroke(0.85.dp, innerBorder),
                            )
                        } else {
                            Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        },
                    )
                    .clickable(
                        interactionSource = closeInteraction,
                        indication = if (useGlass) null else androidx.compose.material3.ripple(),
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = if (useGlass) {
                        rememberGlassContentColor(closeState, MaterialTheme.colorScheme.onSurface)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(16.dp),
                )
            }
        }

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
                    .border(2.dp, MaterialTheme.colorScheme.surfaceContainer, CircleShape)
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

        Text(
            text = stringResource(R.string.update_available_title),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.update_available_subtitle),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = pillBg,
                border = if (useGlass) BorderStroke(1.dp, innerBorder) else null,
            ) {
                Text(
                    text = "v${update.version}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }

            Surface(
                shape = CircleShape,
                color = SuccessGreen.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = stringResource(if (update.isPrerelease) R.string.update_prerelease else R.string.update_stable),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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
            if (update.releaseDate.isNotBlank()) {
                InfoCard(
                    icon = AppIcons.Calendar,
                    title = update.releaseDate,
                    subtitle = stringResource(R.string.update_release_date),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (changelogItems.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.update_whats_new),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = CircleShape,
                    color = pillBg,
                    border = if (useGlass) BorderStroke(1.dp, innerBorder) else null,
                ) {
                    Text(
                        text = stringResource(R.string.update_changes, changelogItems.size),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(innerBg)
                    .border(
                        1.dp,
                        innerBorder,
                        RoundedCornerShape(16.dp),
                    )
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
                                color = MaterialTheme.colorScheme.primary,
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
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        UpdatePrimaryButton(
            text = stringResource(R.string.update_now),
            icon = AppIcons.ArrowDownward,
            onClick = onUpdateNow,
        )

        Spacer(Modifier.height(8.dp))

        UpdateSecondaryButton(
            text = stringResource(R.string.update_later),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val useGlass = isAppGlassActive()
    val cardBg = if (useGlass) rememberGlassInnerContainerColor() else MaterialTheme.colorScheme.surfaceContainerHighest
    val cardBorder = if (useGlass) rememberGlassInnerBorderColor() else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val iconCircleBg = if (useGlass) rememberGlassInnerContainerColor() else MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                1.dp,
                cardBorder,
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconCircleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UpdateDownloadingContent(
    state: UpdateFlowState.Downloading,
    onCancel: () -> Unit,
) {
    val useGlass = isAppGlassActive()
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
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "v${state.update.version} • $totalMB",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        LinearProgressIndicator(
            progress = { (progress / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = if (useGlass) rememberGlassInnerContainerColor() else MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "$downloadedMB of $totalMB ($progress%)",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(22.dp))

        if (useGlass) {
            val cancelInteraction = remember { MutableInteractionSource() }
            val cancelState = rememberGlassEffectState()
            val innerBorder = rememberGlassInnerBorderColor()
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .appGlassSurface(
                        shape = CircleShape,
                        interactive = true,
                        state = cancelState,
                        interactionSource = cancelInteraction,
                        fallbackBackground = rememberGlassInnerContainerColor(),
                        fallbackBorder = BorderStroke(0.85.dp, innerBorder),
                    )
                    .clickable(
                        interactionSource = cancelInteraction,
                        indication = null,
                        onClick = onCancel,
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = rememberGlassContentColor(cancelState, MaterialTheme.colorScheme.onSurface),
                )
            }
        } else {
            Button(
                onClick = onCancel,
                modifier = Modifier.height(38.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                ),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
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
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.update_installing),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.update_installing_desc),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                .background(SuccessGreen.copy(alpha = 0.15f))
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
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Playora v$version",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        UpdatePrimaryButton(
            text = stringResource(R.string.okay),
            onClick = onDismiss,
        )
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
                .background(ErrorRed.copy(alpha = 0.15f))
                .border(2.dp, ErrorRed, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Info,
                contentDescription = null,
                tint = ErrorRed,
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
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        UpdatePrimaryButton(
            text = stringResource(R.string.okay),
            onClick = onDismiss,
        )
    }
}

@Composable
private fun UpdatePrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val useGlass = isAppGlassActive()
    if (useGlass) {
        val interaction = remember { MutableInteractionSource() }
        val glassState = rememberGlassEffectState()
        val contentColor = rememberGlassContentColor(
            state = glassState,
            fallback = MaterialTheme.colorScheme.onPrimary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .appGlassSurface(
                    shape = CircleShape,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.56f),
                    interactive = true,
                    state = glassState,
                    interactionSource = interaction,
                    fallbackBackground = MaterialTheme.colorScheme.primary,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = contentColor,
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun UpdateSecondaryButton(
    text: String,
    onClick: () -> Unit,
) {
    val useGlass = isAppGlassActive()
    if (useGlass) {
        val interaction = remember { MutableInteractionSource() }
        val glassState = rememberGlassEffectState()
        val innerBorder = rememberGlassInnerBorderColor()
        val contentColor = rememberGlassContentColor(
            state = glassState,
            fallback = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .appGlassSurface(
                    shape = CircleShape,
                    interactive = true,
                    state = glassState,
                    interactionSource = interaction,
                    fallbackBackground = rememberGlassInnerContainerColor(),
                    fallbackBorder = BorderStroke(0.85.dp, innerBorder),
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = contentColor,
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
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
