package com.sipun.playora.feature.player.extensions

import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.CropLandscape
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.WidthNormal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.media3.common.C
import androidx.media3.common.TrackGroup
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import com.sipun.playora.R
import com.sipun.playora.core.model.Font
import com.sipun.playora.core.model.VideoContentScale
import java.util.Locale
import kotlin.time.Duration

fun Duration.formatted(): String = toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) {
        "$hours:${minutes.padStartWith0()}:${seconds.padStartWith0()}"
    } else {
        "${minutes.padStartWith0()}:${seconds.padStartWith0()}"
    }
}

private fun Number.padStartWith0() = this.toString().padStart(
    length = 2,
    padChar = '0',
)

inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

fun Font.toTypeface(): Typeface = when (this) {
    Font.DEFAULT -> Typeface.DEFAULT
    Font.MONOSPACE -> Typeface.MONOSPACE
    Font.SANS_SERIF -> Typeface.SANS_SERIF
    Font.SERIF -> Typeface.SERIF
}

@Composable
fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    enabled = enabled,
    onClick = onClick,
)

@UnstableApi
fun TrackGroup.getName(trackType: @C.TrackType Int, index: Int): String {
    val format = this.getFormat(0)
    val language = format.language
    val label = format.label
    return buildString {
        if (label != null) {
            append(label)
        }
        if (isEmpty()) {
            if (trackType == C.TRACK_TYPE_TEXT) {
                append("Subtitle Track #${index + 1}")
            } else {
                append("Audio Track #${index + 1}")
            }
        }

        @Suppress("DEPRECATION")
        if (language != null && language != "und") {
            append(" - ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                append(Locale.of(language).displayLanguage)
            } else {
                append(Locale(language).displayLanguage)
            }
        }
    }
}

@get:UnstableApi
val TrackGroup.externalAudioIndex: Int?
    get() = getFormat(
        0,
    ).id?.substringBefore(delimiter = ":")?.toIntOrNull()?.minus(other = 1)?.takeIf { it >= 0 }

fun VideoContentScale.nameRes(): Int = when (this) {
    VideoContentScale.BEST_FIT -> R.string.best_fit
    VideoContentScale.STRETCH -> R.string.stretch
    VideoContentScale.CROP -> R.string.crop
    VideoContentScale.HUNDRED_PERCENT -> R.string.hundred_percent
}

fun VideoContentScale.descriptionRes(): Int = when (this) {
    VideoContentScale.BEST_FIT -> R.string.best_fit_description
    VideoContentScale.STRETCH -> R.string.stretch_description
    VideoContentScale.CROP -> R.string.crop_description
    VideoContentScale.HUNDRED_PERCENT -> R.string.hundred_percent_description
}

fun VideoContentScale.imageVector(): ImageVector = when (this) {
    VideoContentScale.BEST_FIT -> Icons.Rounded.FitScreen
    VideoContentScale.STRETCH -> Icons.Rounded.AspectRatio
    VideoContentScale.CROP -> Icons.Rounded.CropLandscape
    VideoContentScale.HUNDRED_PERCENT -> Icons.Rounded.WidthNormal
}

fun VideoContentScale.toContentScale(): ContentScale = when (this) {
    VideoContentScale.BEST_FIT -> ContentScale.Fit
    VideoContentScale.STRETCH -> ContentScale.FillBounds
    VideoContentScale.CROP -> ContentScale.Crop
    VideoContentScale.HUNDRED_PERCENT -> FixedScale(value = 1.0f)
}

val VideoSize.isPortrait: Boolean
    get() = this.height > this.width
