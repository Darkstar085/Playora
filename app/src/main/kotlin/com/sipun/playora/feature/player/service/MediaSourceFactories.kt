package com.sipun.playora.feature.player.service

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.ParserException
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.FilteringMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException
import com.sipun.nextlib.media3ext.ffdecoder.FfmpegMediaSourceFactory
import com.sipun.nextlib.media3ext.ffdecoder.FfmpegSubtitleExtractorsFactory
import com.sipun.nextlib.media3ext.ffdecoder.withFfmpegSubtitles
import com.sipun.playora.feature.player.extensions.copy
import com.sipun.playora.feature.player.extensions.externalAudio
import com.sipun.playora.feature.player.extensions.ffmpegExtraction
import com.sipun.playora.feature.player.extensions.videoDecoderMode
import com.sipun.playora.feature.player.model.DecoderMode

@UnstableApi
internal class ApiMediaSourceFactory(
    private val context: Context,
    private val dataSourceFactory: DataSource.Factory,
) : MediaSource.Factory by DefaultMediaSourceFactory(
    context,
    FfmpegSubtitleExtractorsFactory(),
).setDataSourceFactory(dataSourceFactory).withFfmpegSubtitles(dataSourceFactory) {
    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        if (mediaItem.usesFfmpegExtractor) {
            return FfmpegMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem)
        }
        return DefaultMediaSourceFactory(context, FfmpegSubtitleExtractorsFactory())
            .setDataSourceFactory(dataSourceFactory)
            .withFfmpegSubtitles(dataSourceFactory)
            .createMediaSource(mediaItem)
    }
}

@UnstableApi
internal class ExternalAudioMediaSourceFactory(private val delegate: MediaSource.Factory) :
    MediaSource.Factory by delegate {
    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        val video = delegate.createMediaSource(mediaItem)
        val audio = mediaItem.mediaMetadata.externalAudio
        if (audio.isEmpty()) return video
        return MergingMediaSource(
            true,
            false,
            video,
            *audio.map { uri ->
                FilteringMediaSource(
                    delegate.createMediaSource(MediaItem.fromUri(uri)),
                    C.TRACK_TYPE_AUDIO,
                )
            }.toTypedArray(),
        )
    }
}

@get:UnstableApi
internal val MediaItem.usesFfmpegExtractor: Boolean
    @Suppress("DEPRECATION")
    get() {
        val config = localConfiguration ?: return false
        return mediaMetadata.ffmpegExtraction && config.drmConfiguration == null &&
                Util.inferContentType(
                    config.uri,
                    config.mimeType,
                ) == C.CONTENT_TYPE_OTHER
    }

internal val MediaItem.videoDecoderBackend: DecoderMode
    get() = when {
        mediaMetadata.videoDecoderMode == DecoderMode.FFMPEG -> DecoderMode.FFMPEG
        usesFfmpegExtractor -> DecoderMode.HW_PLUS
        else -> DecoderMode.HARDWARE
    }

internal fun MediaItem.withVideoDecoder(
    mode: DecoderMode,
    useFfmpegForSoftware: Boolean,
): MediaItem = copy(
    videoDecoderMode = mode,
    ffmpegExtraction = when (mode) {
        DecoderMode.HW_PLUS -> true
        DecoderMode.FFMPEG -> useFfmpegForSoftware
        DecoderMode.HARDWARE -> false
        DecoderMode.AUTO -> mediaMetadata.ffmpegExtraction
    },
)

internal fun MediaItem.hasPrimaryParserError(error: Throwable): Boolean {
    val config = localConfiguration ?: return false
    val parserError = generateSequence(error) {
        it.cause
    }.filterIsInstance<ParserException>().firstOrNull()
    return when (parserError) {
        null -> false
        is UnrecognizedInputFormatException -> parserError.uri == config.uri
        else -> mediaMetadata.externalAudio.isEmpty() && config.subtitleConfigurations.isEmpty()
    }
}
