package com.sipun.playora.core.model

import java.io.Serializable
import java.util.Date

sealed interface MediaItem {
    data class VideoItem(val video: Video) : MediaItem
    data class FolderItem(val folder: Folder) : MediaItem
}

data class Folder(
    val name: String,
    val path: String,
    val dateModified: Long,
    val parentPath: String? = null,
    val totalSize: Long = 0,
    val totalDuration: Long = 0,
    val videosCount: Int = 0,
    val foldersCount: Int = 0,
    val newVideosCount: Int = 0,
) : Serializable {
    companion object {
        val sample = Folder(
            name = "Folder 1",
            path = "/storage/emulated/0/DCIM/Camera/Live Photos",
            dateModified = 2000,
        )
    }
}

fun List<Folder>.findClosestFolder(videoPath: String): Folder? {
    val videoDirectory = videoPath.substringBeforeLast(delimiter = "/")
    return filter { folder ->
        videoDirectory == folder.path || videoDirectory.startsWith(prefix = folder.path + "/")
    }.maxByOrNull { folder -> folder.path.length }
}

data class Video(
    val id: Long,
    val path: String,
    val parentPath: String = "",
    val duration: Long,
    val uriString: String,
    val nameWithExtension: String,
    val width: Int,
    val height: Int,
    val size: Long,
    val playbackPosition: Long? = null,
    val dateModified: Long = 0,
    val dateAdded: Long = 0,
    val formattedDuration: String = "",
    val formattedFileSize: String = "",
    val format: String? = null,
    val lastPlayedAt: Date? = null,
    val videoStream: VideoStreamInfo? = null,
    val audioStreams: List<AudioStreamInfo> = emptyList(),
    val subtitleStreams: List<SubtitleStreamInfo> = emptyList(),
) : Serializable {

    val displayName: String = nameWithExtension.substringBeforeLast(delimiter = ".")
    val playedPercentage: Float? = playbackPosition?.let { playbackPosition ->
        when {
            duration <= 0 -> null
            playbackPosition < 0 -> 1f
            else -> playbackPosition.toFloat() / duration.toFloat()
        }
    }

    companion object {
        val sample = Video(
            id = 8,
            path = "/storage/emulated/0/Download/Avengers Endgame (2019) BluRay x264.mp4",
            parentPath = "/storage/emulated/0/Download",
            uriString = "",
            nameWithExtension = "Avengers Endgame (2019) BluRay x264.mp4",
            duration = 1000,
            width = 1920,
            height = 1080,
            size = 1000,
            formattedDuration = "29.36",
            formattedFileSize = "320KB",
            playbackPosition = 200,
        )
    }
}

fun List<Video>.recentPlayed(): Video? =
    filter { it.lastPlayedAt != null }.sortedByDescending { it.lastPlayedAt?.time }.firstOrNull()

fun Video.isNew(): Boolean =
    lastPlayedAt == null && (playbackPosition == null || playbackPosition <= 0L)
