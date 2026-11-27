package com.sipun.playora.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.model.MediaViewMode
import com.sipun.playora.core.model.Sort
import com.sipun.playora.core.ui.theme.AppGlassBottomSheetOverlay
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.isAppGlassActive
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerBorderColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerContainerColor
import com.sipun.playora.feature.videopicker.extensions.name
import io.github.itshedimisawi.vitro.rememberGlassEffectState

@Composable
fun QuickSettingsDialog(
    applicationPreferences: ApplicationPreferences,
    onDismiss: () -> Unit,
    updatePreferences: (ApplicationPreferences) -> Unit,
) {
    var preferences by remember { mutableStateOf(value = applicationPreferences) }
    val useGlass = isAppGlassActive()

    AppGlassBottomSheetOverlay(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .then(if (useGlass) Modifier else Modifier.navigationBarsPadding()),
        ) {
            // Header: Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.quick_settings),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // 1. Media view mode
            SectionHeader(
                icon = Icons.Rounded.Folder,
                title = stringResource(R.string.media_view_mode),
            )
            SegmentedPillRow(
                items = listOf(
                    MediaViewMode.FOLDER_TREE,
                    MediaViewMode.FOLDERS,
                    MediaViewMode.VIDEOS,
                ),
                selectedItem = preferences.mediaViewMode,
                onItemSelected = { preferences = preferences.copy(mediaViewMode = it) },
                itemLabel = { it.name() },
                itemIcon = { mode ->
                    when (mode) {
                        MediaViewMode.FOLDER_TREE -> Icons.Rounded.AccountTree
                        MediaViewMode.FOLDERS -> Icons.Rounded.Folder
                        MediaViewMode.VIDEOS -> Icons.Rounded.SmartDisplay
                    }
                },
            )

            // 2. Media Layout
            SectionHeader(
                icon = Icons.Rounded.GridView,
                title = stringResource(R.string.media_layout),
            )
            SegmentedPillRow(
                items = listOf(MediaLayoutMode.LIST, MediaLayoutMode.GRID),
                selectedItem = preferences.mediaLayoutMode,
                onItemSelected = { preferences = preferences.copy(mediaLayoutMode = it) },
                itemLabel = {
                    when (it) {
                        MediaLayoutMode.LIST -> stringResource(R.string.list)
                        MediaLayoutMode.GRID -> stringResource(R.string.grid)
                    }
                },
                itemIcon = { mode ->
                    when (mode) {
                        MediaLayoutMode.LIST -> Icons.AutoMirrored.Rounded.ViewList
                        MediaLayoutMode.GRID -> Icons.Rounded.GridView
                    }
                },
            )

            // 3. Sort
            SectionHeader(
                icon = Icons.Rounded.SwapVert,
                title = stringResource(R.string.sort),
            )
            SortCriteriaRow(
                selectedBy = preferences.sortBy,
                onSelectBy = { preferences = preferences.copy(sortBy = it) },
            )
            Spacer(modifier = Modifier.height(10.dp))
            SegmentedPillRow(
                items = listOf(Sort.Order.ASCENDING, Sort.Order.DESCENDING),
                selectedItem = preferences.sortOrder,
                onItemSelected = { preferences = preferences.copy(sortOrder = it) },
                itemLabel = { order ->
                    val arrow = if (order == Sort.Order.ASCENDING) "↑  " else "↓  "
                    arrow + order.name(preferences.sortBy)
                },
            )

            // 4. Fields
            SectionHeader(
                icon = Icons.Rounded.Layers,
                title = stringResource(R.string.fields),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickSettingFieldChip(
                    label = stringResource(R.string.duration),
                    selected = preferences.showDurationField,
                    onClick = {
                        preferences =
                            preferences.copy(showDurationField = !preferences.showDurationField)
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.folder_duration),
                    selected = preferences.showFolderDurationField,
                    onClick = {
                        preferences = preferences.copy(
                            showFolderDurationField = !preferences.showFolderDurationField,
                        )
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.extension),
                    selected = preferences.showExtensionField,
                    onClick = {
                        preferences =
                            preferences.copy(showExtensionField = !preferences.showExtensionField)
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.path),
                    selected = preferences.showPathField,
                    onClick = {
                        preferences = preferences.copy(showPathField = !preferences.showPathField)
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.played_progress),
                    selected = preferences.showPlayedProgress,
                    onClick = {
                        preferences = preferences.copy(
                            showPlayedProgress = !preferences.showPlayedProgress,
                        )
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.resolution),
                    selected = preferences.showResolutionField,
                    onClick = {
                        preferences = preferences.copy(
                            showResolutionField = !preferences.showResolutionField,
                        )
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.size),
                    selected = preferences.showSizeField,
                    onClick = {
                        preferences = preferences.copy(showSizeField = !preferences.showSizeField)
                    },
                )
                QuickSettingFieldChip(
                    label = stringResource(R.string.thumbnail),
                    selected = preferences.showThumbnailField,
                    onClick = {
                        preferences = preferences.copy(
                            showThumbnailField = !preferences.showThumbnailField,
                        )
                    },
                )
            }

            // Bottom Buttons: Cancel & Done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (useGlass) {
                    val cancelInteraction = remember { MutableInteractionSource() }
                    val cancelState = rememberGlassEffectState()
                    val innerBorder = rememberGlassInnerBorderColor()
                    Box(
                        modifier = Modifier
                            .appGlassSurface(
                                shape = CircleShape,
                                interactive = true,
                                state = cancelState,
                                interactionSource = cancelInteraction,
                                fallbackBackground = rememberGlassInnerContainerColor(),
                                fallbackBorder = androidx.compose.foundation.BorderStroke(
                                    width = 0.85.dp,
                                    color = innerBorder,
                                ),
                            )
                            .clickable(
                                interactionSource = cancelInteraction,
                                indication = null,
                                onClick = onDismiss,
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = rememberGlassContentColor(
                                state = cancelState,
                                fallback = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    val doneInteraction = remember { MutableInteractionSource() }
                    val doneState = rememberGlassEffectState()
                    Box(
                        modifier = Modifier
                            .appGlassSurface(
                                shape = CircleShape,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.56f),
                                interactive = true,
                                state = doneState,
                                interactionSource = doneInteraction,
                                fallbackBackground = MaterialTheme.colorScheme.primary,
                                fallbackBorder = androidx.compose.foundation.BorderStroke(
                                    width = 0.75.dp,
                                    color = Color.White.copy(alpha = 0.28f),
                                ),
                            )
                            .clickable(
                                interactionSource = doneInteraction,
                                indication = null,
                                onClick = {
                                    updatePreferences(preferences)
                                    onDismiss()
                                },
                            )
                            .padding(horizontal = 26.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                            color = rememberGlassContentColor(
                                state = doneState,
                                fallback = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            updatePreferences(preferences)
                            onDismiss()
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun <T> SegmentedPillRow(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: @Composable (T) -> String,
    itemIcon: @Composable ((T) -> ImageVector)? = null,
) {
    val useGlass = isAppGlassActive()
    val trackColor = if (useGlass) {
        rememberGlassInnerContainerColor()
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val trackBorder = if (useGlass) {
        rememberGlassInnerBorderColor()
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(trackColor)
            .border(
                width = 1.dp,
                color = trackBorder,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val pillInteraction = remember { MutableInteractionSource() }
            val pillState = rememberGlassEffectState()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isSelected && useGlass) {
                            Modifier.appGlassSurface(
                                shape = RoundedCornerShape(20.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.56f),
                                interactive = true,
                                state = pillState,
                                interactionSource = pillInteraction,
                                fallbackBackground = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                )
                        },
                    )
                    .clickable(
                        interactionSource = pillInteraction,
                        indication = if (isSelected && useGlass) null else androidx.compose.material3.ripple(),
                    ) { onItemSelected(item) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                val contentColor = when {
                    isSelected && useGlass -> rememberGlassContentColor(
                        state = pillState,
                        fallback = MaterialTheme.colorScheme.onPrimary,
                    )
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (itemIcon != null) {
                        Icon(
                            imageVector = itemIcon(item),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = contentColor,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = itemLabel(item),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = contentColor,
                    )
                }
            }
        }
    }
}

private sealed class SortCriterionIcon {
    data class Vector(val imageVector: ImageVector) : SortCriterionIcon()
    data class DrawableRes(val resId: Int) : SortCriterionIcon()
}

@Composable
private fun SortCriteriaRow(
    selectedBy: Sort.By,
    onSelectBy: (Sort.By) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useGlass = isAppGlassActive()
    val unselectedBg = if (useGlass) {
        rememberGlassInnerContainerColor()
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val unselectedBorder = if (useGlass) {
        rememberGlassInnerBorderColor()
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val items = listOf(
        Triple(Sort.By.TITLE, stringResource(R.string.title), null),
        Triple(
            Sort.By.LENGTH,
            stringResource(R.string.duration),
            SortCriterionIcon.Vector(Icons.Rounded.Timer),
        ),
        Triple(
            Sort.By.DATE,
            stringResource(R.string.date),
            SortCriterionIcon.Vector(Icons.Rounded.CalendarMonth),
        ),
        Triple(
            Sort.By.SIZE,
            stringResource(R.string.size),
            SortCriterionIcon.DrawableRes(R.drawable.ic_storage_disks),
        ),
        Triple(
            Sort.By.PATH,
            stringResource(R.string.location),
            SortCriterionIcon.Vector(Icons.Rounded.LocationOn),
        ),
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items.forEach { (by, label, icon) ->
            val isSelected = by == selectedBy
            val tileInteraction = remember { MutableInteractionSource() }
            val tileState = rememberGlassEffectState()
            val iconTint = when {
                isSelected && useGlass -> rememberGlassContentColor(
                    state = tileState,
                    fallback = MaterialTheme.colorScheme.onPrimary,
                )
                isSelected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .height(46.dp)
                        .then(
                            if (isSelected && useGlass) {
                                Modifier.appGlassSurface(
                                    shape = RoundedCornerShape(16.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.56f),
                                    interactive = true,
                                    state = tileState,
                                    interactionSource = tileInteraction,
                                    fallbackBackground = MaterialTheme.colorScheme.primary,
                                )
                            } else {
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else unselectedBg,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else unselectedBorder,
                                        shape = RoundedCornerShape(16.dp),
                                    )
                            },
                        )
                        .clickable(
                            interactionSource = tileInteraction,
                            indication = if (isSelected && useGlass) null else androidx.compose.material3.ripple(),
                        ) { onSelectBy(by) },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        by == Sort.By.TITLE -> {
                            Text(
                                text = "T",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                ),
                                color = iconTint,
                            )
                        }

                        icon is SortCriterionIcon.Vector -> {
                            Icon(
                                imageVector = icon.imageVector,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                        }

                        icon is SortCriterionIcon.DrawableRes -> {
                            Icon(
                                painter = painterResource(icon.resId),
                                contentDescription = label,
                                modifier = Modifier.size(20.dp),
                                tint = iconTint,
                            )
                        }
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun QuickSettingFieldChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val useGlass = isAppGlassActive()
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else if (useGlass) {
        rememberGlassInnerBorderColor()
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (useGlass) 0.22f else 0.15f)
    } else if (useGlass) {
        rememberGlassInnerContainerColor()
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(17.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (selected) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(4.dp),
                            )
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
fun QuickSettingsPreview() {
    PlayoraTheme {
        QuickSettingsDialog(
            applicationPreferences = ApplicationPreferences(),
            onDismiss = {},
            updatePreferences = {},
        )
    }
}
