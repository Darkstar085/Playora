package com.sipun.playora.core.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.sipun.playora.R
import com.sipun.playora.core.ui.designsystem.AppIcons

val DestructiveRed = Color(0xFFE53935)

@Composable
fun DoneButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) { Text(text = stringResource(R.string.done)) }
}

@Composable
fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) { Text(text = stringResource(R.string.cancel)) }
}

@Composable
fun DeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = stringResource(R.string.delete),
) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = DestructiveRed,
        ),
    ) {
        Text(text = text, color = DestructiveRed)
    }
}

@Composable
fun ResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = stringResource(R.string.reset),
) {
    TextButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = DestructiveRed,
        ),
    ) {
        Text(text = text, color = DestructiveRed)
    }
}

@Composable
fun ResetIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = DestructiveRed,
            contentColor = Color.White,
            disabledContainerColor = DestructiveRed.copy(alpha = 0.38f),
            disabledContentColor = Color.White.copy(alpha = 0.38f),
        ),
    ) {
        Icon(
            imageVector = AppIcons.History,
            contentDescription = contentDescription,
        )
    }
}
