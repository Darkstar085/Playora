package com.sipun.playora.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.sipun.playora.R
import com.sipun.playora.core.ui.components.CancelButton
import com.sipun.playora.core.ui.components.DoneButton
import com.sipun.playora.core.ui.components.AppDialog
import com.sipun.playora.core.ui.components.AppOutlinedTextField
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
                minWidth = 75.dp,
                minHeight = 64.dp,
            )
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
            textAlign = TextAlign.Center,
        )
    }
}
