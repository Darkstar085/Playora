package com.sipun.playora.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoNotDisturbAlt
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.model.MarkType
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppOutlinedTextField
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.DoneButton
import com.sipun.playora.core.ui.theme.appGlassSurface
import com.sipun.playora.core.ui.theme.isAppGlassActive
import com.sipun.playora.core.ui.theme.rememberGlassContentColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerBorderColor
import com.sipun.playora.core.ui.theme.rememberGlassInnerContainerColor
import io.github.itshedimisawi.vitro.rememberGlassEffectState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.extraSmall.copy(all = CornerSize(2.dp)),
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
        color = contentColor,
        modifier = modifier
            .clip(shape = shape)
            .background(color = backgroundColor)
            .padding(
                vertical = 1.dp,
                horizontal = 3.dp,
            ),
    )
}

@Composable
fun RenameDialog(name: String, onDismiss: () -> Unit, onDone: (String) -> Unit) {
    var mediaName by rememberSaveable { mutableStateOf(value = name) }
    val focusRequester = remember { FocusRequester() }
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.rename_to)) },
        content = {
            AppOutlinedTextField(
                value = mediaName,
                onValueChange = { mediaName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester),
            )
        },
        confirmButton = {
            DoneButton(
                enabled = mediaName.isNotBlank(),
                onClick = { onDone(mediaName) },
            )
        },
        dismissButton = { CancelButton(onClick = onDismiss) },
    )

    LaunchedEffect(key1 = Unit) {
        delay(200.milliseconds)
        focusRequester.requestFocus()
    }
}

@Composable
internal fun SelectionAction(
    imageVector: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier
            .defaultMinSize(
                minWidth = 0.dp,
                minHeight = 56.dp,
            )
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = 2.dp,
                vertical = 6.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = title,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
        Spacer(modifier = Modifier.size(3.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = tint,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MarkAsDialog(
    onDismiss: () -> Unit,
    onConfirm: (MarkType) -> Unit,
) {
    var selectedMarkType by remember { mutableStateOf(MarkType.LAST_PLAYED) }
    val useGlass = isAppGlassActive()
    val primary = MaterialTheme.colorScheme.primary
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

    AppDialog(
        onDismissRequest = onDismiss,
        title = { },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.mark_as),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Set the watch status for the selected item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Options list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MarkType.entries.forEach { markType ->
                        val isSelected = selectedMarkType == markType
                        val (title, subtitle, icon) = when (markType) {
                            MarkType.LAST_PLAYED -> Triple(
                                stringResource(R.string.last_played),
                                "Continue from where you left off",
                                Icons.Rounded.PlayCircle,
                            )

                            MarkType.NEW -> Triple(
                                "New",
                                "Mark as newly added",
                                Icons.Rounded.AutoAwesome,
                            )

                            MarkType.FINISHED -> Triple(
                                stringResource(R.string.finished),
                                "Mark as completed",
                                Icons.Rounded.TaskAlt,
                            )

                            MarkType.NONE -> Triple(
                                stringResource(R.string.none),
                                "Clear the watch status",
                                Icons.Rounded.DoNotDisturbAlt,
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) primary.copy(alpha = 0.15f) else unselectedBg,
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) primary else unselectedBorder,
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { selectedMarkType = markType }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) primary.copy(alpha = 0.2f) else unselectedBg,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (useGlass) {
                val doneInteraction = remember { MutableInteractionSource() }
                val doneState = rememberGlassEffectState()
                val doneContentColor = rememberGlassContentColor(
                    state = doneState,
                    fallback = MaterialTheme.colorScheme.onPrimary,
                )
                Row(
                    modifier = Modifier
                        .appGlassSurface(
                            shape = CircleShape,
                            tint = primary.copy(alpha = 0.56f),
                            interactive = true,
                            state = doneState,
                            interactionSource = doneInteraction,
                            fallbackBackground = primary,
                        )
                        .clickable(
                            interactionSource = doneInteraction,
                            indication = null,
                            onClick = { onConfirm(selectedMarkType) },
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = doneContentColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.done),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = doneContentColor,
                    )
                }
            } else {
                Button(
                    onClick = { onConfirm(selectedMarkType) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.done),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                )
            }
        },
    )
}
