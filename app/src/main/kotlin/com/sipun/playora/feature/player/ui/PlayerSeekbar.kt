package com.sipun.playora.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.Chapter
import com.sipun.playora.feature.player.state.currentChapterIndex

@OptIn(
    ExperimentalMaterial3Api::class,
    UnstableApi::class,
)
@Composable
internal fun PlayerSeekbar(
    modifier: Modifier = Modifier,
    position: Float,
    duration: Float,
    chapters: List<Chapter>,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnSeekFinished by rememberUpdatedState(newValue = onSeekFinished)
    var isScrubbing by remember { mutableStateOf(value = false) }
    var lastSeekChapterIndex by remember(chapters) { mutableStateOf<Int?>(value = null) }
    val onValueChange: (Float) -> Unit = { value ->
        isScrubbing = true
        val chapterIndex = chapters.currentChapterIndex(value.toLong())
        val previousChapterIndex =
            lastSeekChapterIndex ?: chapters.currentChapterIndex(position.toLong())
        if (chapterIndex != previousChapterIndex) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
        lastSeekChapterIndex = chapterIndex
        onSeek(value)
    }
    val onValueChangeFinished = {
        lastSeekChapterIndex = null
        if (isScrubbing) {
            isScrubbing = false
            currentOnSeekFinished()
        }
    }
    var isFocused by remember { mutableStateOf(value = false) }
    val focusModifier = modifier
        .fillMaxWidth()
        .onFocusChanged {
            if (isFocused && !it.isFocused) onValueChangeFinished()
            isFocused = it.isFocused
        }
    CompositionLocalProvider(value = LocalLayoutDirection provides LayoutDirection.Ltr) {
        SimpleSlider(
            modifier = focusModifier,
            isFocused = isFocused,
            chapters = chapters,
            value = position,
            valueRange = 0f..(duration.takeIf { it > 0 } ?: 0f),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleSlider(
    modifier: Modifier = Modifier,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    isFocused: Boolean = false,
    chapters: List<Chapter> = emptyList(),
) {
    val thumbSize by animateDpAsState(
        targetValue = if (isFocused) 22.dp else 16.dp,
        label = "thumbSize",
    )
    Slider(
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier.height(24.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(size = thumbSize)
                    .shadow(
                        4.dp,
                        shape = CircleShape,
                    )
                    .background(MaterialTheme.colorScheme.primary)
                    .then(
                        other = if (isFocused) {
                            Modifier.border(
                                2.dp,
                                Color.White,
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    ),
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .chapterGaps(
                        chapters = chapters,
                        valueRange.endInclusive,
                    )
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color.White.copy(alpha = 0.5f)),
            ) {
                if (valueRange.endInclusive > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = value / valueRange.endInclusive)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        },
    )
}

@OptIn(UnstableApi::class)
private fun Modifier.chapterGaps(chapters: List<Chapter>, duration: Float): Modifier =
    drawWithCache {
        val gaps = Path()
        if (duration > 0f) {
            val halfGap = 1.5.dp.toPx()
            chapters.forEach { chapter ->
                if (chapter.startTimeMs > 0 && chapter.startTimeMs < duration) {
                    val x = size.width * (chapter.startTimeMs / duration)
                    gaps.addRect(
                        rect = Rect(
                            left = x - halfGap,
                            top = 0f,
                            right = x + halfGap,
                            size.height,
                        ),
                    )
                }
            }
        }
        onDrawWithContent {
            clipPath(
                path = gaps,
                ClipOp.Difference,
            ) { this@onDrawWithContent.drawContent() }
        }
    }
