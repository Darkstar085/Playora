package com.sipun.playora.core.media.services

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface MediaService {

    fun observeFolders(folderPath: String? = null): Flow<List<MediaFolder>>

    fun observeVideos(folderPath: String? = null): Flow<List<MediaVideo>>

    fun observeTrashVideos(): Flow<List<MediaVideo>>

    suspend fun fetchFolders(folderPath: String? = null): List<MediaFolder>

    suspend fun fetchVideos(folderPath: String? = null): List<MediaVideo>

    suspend fun findVideo(uri: Uri): MediaVideo?

    suspend fun findFolder(path: String): MediaFolder?
}
