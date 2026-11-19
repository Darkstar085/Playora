package com.sipun.playora.feature.player.service

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.FilteringMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import com.sipun.playora.feature.player.extensions.externalAudio
import com.sipun.nextlib.media3ext.ffdecoder.FfmpegSubtitleExtractorsFactory
import com.sipun.nextlib.media3ext.ffdecoder.withFfmpegSubtitles

@UnstableApi
internal class ApiMediaSourceFactory(
    private val context: Context,
    private val dataSourceFactory: DataSource.Factory,
) : MediaSource.Factory by DefaultMediaSourceFactory(
    context,
    FfmpegSubtitleExtractorsFactory(),
).setDataSourceFactory(dataSourceFactory).withFfmpegSubtitles(dataSourceFactory) {
    override fun createMediaSource(mediaItem: MediaItem): MediaSource =
        DefaultMediaSourceFactory(context, FfmpegSubtitleExtractorsFactory())
            .setDataSourceFactory(dataSourceFactory)
            .withFfmpegSubtitles(dataSourceFactory)
            .createMediaSource(mediaItem)
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
