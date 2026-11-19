package com.sipun.playora.core.data.mappers

import com.sipun.playora.core.common.Utils
import com.sipun.playora.core.data.models.VideoState
import com.sipun.playora.core.database.converter.UriListConverter
import com.sipun.playora.core.database.entities.MediumStateEntity
import com.sipun.playora.core.media.services.MediaFolder
import com.sipun.playora.core.media.services.MediaVideo
import com.sipun.playora.core.model.AudioStreamInfo
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.SubtitleStreamInfo
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.model.VideoStreamInfo
import com.sipun.nextlib.mediainfo.AudioStream
import com.sipun.nextlib.mediainfo.SubtitleStream
import com.sipun.nextlib.mediainfo.VideoStream
import java.util.Date

internal fun MediaFolder.toFolder() = Folder(
    name = name,
    path = path,
    dateModified = dateModified,
    totalSize = totalSize,
    totalDuration = totalDuration,
    videosCount = videosCount,
    foldersCount = foldersCount,
)

internal fun MediaVideo.toVideo(mediaState: MediumStateEntity? = null) = Video(
    id = id,
    uriString = uri.toString(),
    duration = duration,
    height = height,
    width = width,
    path = path,
    size = size,
    nameWithExtension = displayName,
    parentPath = parentPath,
    dateModified = dateModified,
    dateAdded = dateAdded,
    formattedDuration = Utils.formatDurationMillis(millis = duration),
    formattedFileSize = Utils.formatFileSize(size = size),
    playbackPosition = mediaState?.playbackPosition,
    lastPlayedAt = mediaState?.lastPlayedTime?.let { Date(it) },
)

internal fun VideoStream.toVideoStreamInfo() = VideoStreamInfo(
    index = index,
    title = title,
    codecName = codecName,
    language = language,
    disposition = disposition,
    bitRate = bitRate,
    frameRate = frameRate,
    frameWidth = frameWidth,
    frameHeight = frameHeight,
)

internal fun AudioStream.toAudioStreamInfo() = AudioStreamInfo(
    index = index,
    title = title,
    codecName = codecName,
    language = language,
    disposition = disposition,
    bitRate = bitRate,
    sampleFormat = sampleFormat,
    sampleRate = sampleRate,
    channels = channels,
    channelLayout = channelLayout,
)

internal fun SubtitleStream.toSubtitleStreamInfo() = SubtitleStreamInfo(
    index = index,
    title = title,
    codecName = codecName,
    language = language,
    disposition = disposition,
)

fun MediumStateEntity.toVideoState(): VideoState = VideoState(
    path = uriString,
    position = playbackPosition.takeIf { it != 0L },
    audioTrackIndex = audioTrackIndex,
    subtitleTrackIndex = subtitleTrackIndex,
    externalSubs = UriListConverter.fromStringToList(urlString = externalSubs),
    externalAudio = UriListConverter.fromStringToList(urlString = externalAudio),
    videoScale = videoScale,
    subtitleDelayMilliseconds = subtitleDelayMilliseconds,
    subtitleSpeed = subtitleSpeed,
)
