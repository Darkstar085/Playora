package com.sipun.playora.core.media.services

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.LruCache
import com.sipun.nextlib.mediainfo.MediaInfoBuilder
import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.common.extensions.VIDEO_COLLECTION_URI
import com.sipun.playora.core.common.extensions.prettyName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Single
class MediaStoreMediaService(
    private val context: Context,
    @Named(DiQualifiers.APPLICATION_SCOPE) private val applicationScope: CoroutineScope,
) : MediaService {

    private data class MetadataKey(val uri: Uri, val size: Long, val modified: Long)
    private data class VideoMetadata(val duration: Long, val width: Int, val height: Int)

    private val missingMetadata = LruCache<MetadataKey, VideoMetadata>(256)

    companion object {
        private const val OBSERVER_DEBOUNCE_MS = 250L

        private val VIDEO_PROJECTION = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DATE_ADDED,
        )
    }

    @OptIn(FlowPreview::class)
    private val mediaChanges: Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(element = Unit)
            }
        }
        context.contentResolver.registerContentObserver(
            VIDEO_COLLECTION_URI,
            true,
            observer,
        )
        trySend(element = Unit)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }
        .debounce(OBSERVER_DEBOUNCE_MS.milliseconds)
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 1,
        )

    override fun observeFolders(folderPath: String?): Flow<List<MediaFolder>> = observeMedia {
        fetchFolders(folderPath = folderPath)
    }

    override fun observeVideos(folderPath: String?): Flow<List<MediaVideo>> = observeMedia {
        fetchVideos(folderPath = folderPath)
    }

    override fun observeTrashVideos(): Flow<List<MediaVideo>> = observeMedia {
        queryTrashVideos()
    }

    private fun <T> observeMedia(query: suspend () -> List<T>): Flow<List<T>> = mediaChanges
        .map {
            try {
                query()
            } catch (e: IllegalArgumentException) {

                if (e.message?.startsWith(prefix = "Volume ") == true &&
                    e.message?.endsWith(suffix = " not found") == true
                ) {
                    emptyList()
                } else {
                    throw e
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

    override suspend fun fetchFolders(folderPath: String?): List<MediaFolder> =
        withContext(Dispatchers.IO) {
            val videos = fetchVideos(folderPath = folderPath)

            val videosByFolder = videos.groupBy { File(it.path).parentFile }

            return@withContext videosByFolder.mapNotNull { (folder, folderVideos) ->
                folder?.let {
                    MediaFolder(
                        path = it.path,
                        name = it.prettyName,
                        dateModified =
                            folderVideos.maxOfOrNull { video -> video.dateModified } ?: 0L,
                        totalSize = folderVideos.sumOf { video -> video.size },
                        totalDuration = folderVideos.sumOf { video -> video.duration },
                        videosCount = folderVideos.size,
                        foldersCount = 0,
                    )
                }
            }
        }

    override suspend fun fetchVideos(folderPath: String?): List<MediaVideo> =
        withContext(Dispatchers.IO) { queryVideos(folderPath = folderPath) }

    override suspend fun findVideo(uri: Uri): MediaVideo? = withContext(Dispatchers.IO) {
        return@withContext try {
            context.contentResolver.query(
                uri,
                VIDEO_PROJECTION,
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@withContext null
                cursor.toMediaVideo()
            }
        } catch (e: Exception) {

            null
        }
    }

    override suspend fun findFolder(path: String): MediaFolder? = withContext(Dispatchers.IO) {
        val folderFile = File(path)
        if (!folderFile.exists()) return@withContext null

        val allVideos = fetchVideos(folderPath = path)
        if (allVideos.isEmpty()) return@withContext null

        val directVideos = allVideos.filter { File(it.path).parent == path }
        val directSubfolders = allVideos
            .mapNotNull { video ->
                File(video.path).parentFile?.walkUp()?.firstOrNull { it.parent == path }
            }
            .distinctBy { it.path }

        return@withContext MediaFolder(
            path = folderFile.path,
            name = folderFile.prettyName,
            dateModified = allVideos.maxOfOrNull { it.dateModified } ?: folderFile.lastModified(),
            totalSize = allVideos.sumOf { it.size },
            totalDuration = allVideos.sumOf { it.duration },
            videosCount = directVideos.size,
            foldersCount = directSubfolders.size,
        )
    }

    private fun File.walkUp() = generateSequence(seed = parentFile) { it.parentFile }

    private fun queryTrashVideos(): List<MediaVideo> {
        val queryArgs = Bundle().apply {
            putInt(
                MediaStore.QUERY_ARG_MATCH_TRASHED,
                MediaStore.MATCH_ONLY,
            )
            putStringArray(
                android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Video.Media.DATE_MODIFIED),
            )
            putInt(
                android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION,
                android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            )
        }
        return queryVideoCursor {
            context.contentResolver.query(
                VIDEO_COLLECTION_URI,
                VIDEO_PROJECTION,
                queryArgs,
                null,
            )
        }
    }

    private fun queryVideos(folderPath: String?): List<MediaVideo> {

        val selection = if (folderPath == null) {
            null
        } else {
            "${MediaStore.Video.Media.DATA} LIKE ? ESCAPE '\\'"
        }
        val selectionArgs = if (folderPath == null) {
            null
        } else {
            arrayOf("${folderPath.escapeLike()}/%")
        }
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
        return queryVideoCursor {
            context.contentResolver.query(
                VIDEO_COLLECTION_URI,
                VIDEO_PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
            )
        }
    }

    private fun queryVideoCursor(query: () -> Cursor?): List<MediaVideo> {
        val mediaVideos = mutableListOf<MediaVideo>()
        val cursor = checkNotNull(value = query()) {
            "MediaStore returned no cursor for the video query"
        }
        cursor.use {
            while (cursor.moveToNext()) {
                val video = cursor.toMediaVideo() ?: continue
                mediaVideos.add(element = video)
            }
        }
        return mediaVideos
    }

    private fun String.escapeLike(): String = replace(
        oldValue = "\\",
        newValue = "\\\\",
    ).replace(
        oldValue = "%",
        newValue = "\\%",
    ).replace(
        oldValue = "_",
        newValue = "\\_",
    )

    private fun Cursor.toMediaVideo(): MediaVideo? {
        val dataIndex =
            getColumnIndex(MediaStore.Video.Media.DATA).takeIf { it >= 0 } ?: return null
        val idIndex = getColumnIndex(MediaStore.Video.Media._ID).takeIf { it >= 0 } ?: return null
        val displayNameIndex =
            getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME).takeIf { it >= 0 } ?: return null
        val durationIndex =
            getColumnIndex(MediaStore.Video.Media.DURATION).takeIf { it >= 0 } ?: return null
        val widthIndex =
            getColumnIndex(MediaStore.Video.Media.WIDTH).takeIf { it >= 0 } ?: return null
        val heightIndex =
            getColumnIndex(MediaStore.Video.Media.HEIGHT).takeIf { it >= 0 } ?: return null
        val sizeIndex =
            getColumnIndex(MediaStore.Video.Media.SIZE).takeIf { it >= 0 } ?: return null
        val dateModifiedIndex =
            getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED).takeIf { it >= 0 } ?: return null
        val dateAddedIndex =
            getColumnIndex(MediaStore.Video.Media.DATE_ADDED).takeIf { it >= 0 } ?: return null

        val path = getString(dataIndex) ?: return null
        val file = File(path)
        if (!file.exists()) return null
        val id = getLong(idIndex)
        return MediaVideo(
            id = id,
            path = path,
            title = file.name,
            parentPath = file.parent ?: "/",
            uri = ContentUris.withAppendedId(
                VIDEO_COLLECTION_URI,
                id,
            ),
            displayName = getString(displayNameIndex) ?: file.name,
            duration = getLong(durationIndex),
            width = getInt(widthIndex),
            height = getInt(heightIndex),
            size = getLong(sizeIndex),
            dateModified = getLong(dateModifiedIndex),
            dateAdded = getLong(dateAddedIndex),
        ).withMissingMetadata()
    }

    private fun MediaVideo.withMissingMetadata(): MediaVideo {
        // shortcut: probe on IO; use background indexing if incomplete files slow initial scans.
        if (duration > 0 && width > 0 && height > 0) return this
        val key = MetadataKey(
            uri,
            size,
            dateModified,
        )
        val metadata = synchronized(missingMetadata) {
            missingMetadata.get(key) ?: try {
                context.contentResolver.openFileDescriptor(
                    uri,
                    "r",
                )?.use { descriptor ->
                    val info = MediaInfoBuilder().from(descriptor).build() ?: return@use null
                    try {
                        VideoMetadata(
                            duration = info.duration.coerceAtLeast(0),
                            width = info.videoStream?.frameWidth ?: 0,
                            height = info.videoStream?.frameHeight ?: 0,
                        ).also {
                            if (it.duration > 0 && it.width > 0 && it.height > 0) {
                                missingMetadata.put(
                                    key,
                                    it,
                                )
                            }
                        }
                    } finally {
                        info.release()
                    }
                }
            } catch (_: Exception) {
                null
            }
        } ?: return this
        return copy(
            duration = duration.takeIf { it > 0 } ?: metadata.duration,
            width = width.takeIf { it > 0 } ?: metadata.width,
            height = height.takeIf { it > 0 } ?: metadata.height,
        )
    }
}
