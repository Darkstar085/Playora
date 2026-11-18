package com.sipun.playora.core.media.services

import android.net.Uri

data class MediaFolder(
    val path: String,
    val name: String,
    val dateModified: Long,
    val totalSize: Long,
    val totalDuration: Long,
    val videosCount: Int,
    val foldersCount: Int,
)

data class MediaVideo(
    val id: Long,
    val uri: Uri,
    val path: String,
    val title: String,
    val parentPath: String,
    val displayName: String,
    val duration: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val dateModified: Long,
    val dateAdded: Long,
)
