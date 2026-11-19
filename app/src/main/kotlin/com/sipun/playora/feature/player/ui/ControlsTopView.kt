package com.sipun.playora.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.ui.extensions.copy
import com.sipun.playora.core.ui.theme.PlayoraTheme
import com.sipun.playora.feature.player.buttons.PlayerButton
import com.sipun.playora.feature.player.model.labelRes
import com.sipun.nextlib.media3ext.ffdecoder.DecoderMode

@OptIn(UnstableApi::class)
@Composable
fun ControlsTopView(
    modifier: Modifier = Modifier,
    title: String,
    videoDecoderMode: DecoderMode?,
    onBackClick: () -> Unit = {},
    onDecoderClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onSubtitleClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
) {
    val firstControlFocusRequester = remember { FocusRequester() }
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    val decoderDescription = stringResource(R.string.select_decoders)

    val extraTopPadding = if (systemBarsPadding.calculateTopPadding() == 0.dp) 16.dp else 0.dp
    Row(
        modifier = modifier
            .padding(systemBarsPadding.copy(bottom = 0.dp))
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .padding(top = extraTopPadding)
            .focusProperties { onEnter = { firstControlFocusRequester.requestFocus() } }
            .focusGroup(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlayerButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(weight = 1f),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayerButton(
                modifier = Modifier
                    .semantics { contentDescription = decoderDescription }
                    .focusRequester(focusRequester = firstControlFocusRequester),
                onClick = onDecoderClick,
            ) {
                Text(
                    text = stringResource((videoDecoderMode ?: DecoderMode.HARDWARE).labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
            PlayerButton(onClick = onPlaylistClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    contentDescription = null,
                )
            }
            PlayerButton(onClick = onAudioClick) {
                Icon(
                    imageVector = Icons.Rounded.Audiotrack,
                    contentDescription = null,
                )
            }
            PlayerButton(onClick = onSubtitleClick) {
                Icon(
                    imageVector = Icons.Rounded.Subtitles,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ControlsTopViewPreview() {
    PlayoraTheme(darkTheme = true) {
        Surface {
            ControlsTopView(
                title = "Title",
                videoDecoderMode = DecoderMode.HARDWARE,
                onBackClick = {},
            )
        }
    }
}
