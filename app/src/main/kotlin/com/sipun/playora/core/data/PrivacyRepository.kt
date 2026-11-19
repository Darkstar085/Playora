package com.sipun.playora.core.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.sipun.playora.core.common.Logger
import com.sipun.playora.core.common.Utils
import com.sipun.playora.core.data.mappers.toAudioStreamInfo
import com.sipun.playora.core.data.mappers.toSubtitleStreamInfo
import com.sipun.playora.core.data.mappers.toVideoStreamInfo
import com.sipun.playora.core.database.dao.HiddenVideoDao
import com.sipun.playora.core.database.dao.MediumStateDao
import com.sipun.playora.core.database.entities.HiddenVideoEntity
import com.sipun.playora.core.media.services.MediaOperationsService
import com.sipun.playora.core.model.MediaInfo
import com.sipun.playora.core.model.Video
import com.sipun.nextlib.mediainfo.MediaInfoBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File
import java.util.UUID

interface PrivacyRepository {

    fun observeHiddenVideos(): Flow<List<Video>>

    suspend fun hideVideos(videos: List<Video>)

    suspend fun unhideVideos(videos: List<Video>): UnhideResult

    suspend fun deleteHiddenVideos(videos: List<Video>)

    suspend fun getHiddenVideoInfo(id: Long): MediaInfo?
}

data class UnhideResult(val relocatedCount: Int = 0)

@Single
class LocalPrivacyRepository(
    private val hiddenVideoDao: HiddenVideoDao,
    private val mediumStateDao: MediumStateDao,
    private val mediaOperationsService: MediaOperationsService,
    private val context: Context,
) : PrivacyRepository {

    private val privacyMutationMutex = Mutex()
    private val pendingPrivacyPaths = MutableStateFlow<Set<String>>(value = emptySet())

    private val privacyDir: File by lazy {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        File(
            base,
            PRIVACY_DIR_NAME,
        ).apply { if (!exists()) mkdirs() }
    }

    override fun observeHiddenVideos(): Flow<List<Video>> = combine(
        hiddenVideoDao.getAll(),
        flow2 = pendingPrivacyPaths,
    ) { entities, pendingPaths ->
        entities
            .filter { it.privacyPath !in pendingPaths && File(it.privacyPath).exists() }
            .map { it.toVideo() }
    }

    override suspend fun hideVideos(videos: List<Video>): Unit = privacyMutationMutex.withLock {
        val reservations = reserveVideos(videos = videos)
        if (reservations.isEmpty()) return@withLock

        try {
            val moveResult = try {
                Result.success(
                    mediaOperationsService.moveMedia(
                        reservations.associate {
                            it.sourceUri to it.destination
                        },
                    ),
                )
            } catch (e: Exception) {
                Result.failure(exception = e)
            }
            reconcileReservations(
                reservations = reservations,
                moveResult = moveResult,
            )
            moveResult.exceptionOrNull()?.let { if (it is CancellationException) throw it }
        } finally {
            revealReservations(reservations.map { it.destination.absolutePath })
        }
    }

    private suspend fun reserveVideos(videos: List<Video>): List<HideReservation> {
        val attemptedPrivacyPaths = mutableListOf<String>()
        return try {
            videos.mapNotNull { video ->
                val destination = createPrivacyDestination(video.nameWithExtension)
                attemptedPrivacyPaths += destination.absolutePath
                pendingPrivacyPaths.update { it + destination.absolutePath }
                val rowId = try {
                    hiddenVideoDao.insert(video.toHiddenVideoEntity(destination = destination))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    cleanUpReservations(privacyPaths = listOf(destination.absolutePath))
                    return@mapNotNull null
                }
                HideReservation(
                    rowId = rowId,
                    video.uriString.toUri(),
                    destination = destination,
                )
            }
        } catch (e: Exception) {
            cleanUpReservations(privacyPaths = attemptedPrivacyPaths)
            throw e
        }
    }

    private fun Video.toHiddenVideoEntity(destination: File): HiddenVideoEntity = HiddenVideoEntity(
        privacyPath = destination.absolutePath,
        originalPath = path,
        displayName = nameWithExtension,
        duration = duration,
        size = size,
        width = width,
        height = height,
        hiddenAt = System.currentTimeMillis(),
    )

    private data class HideReservation(val rowId: Long, val sourceUri: Uri, val destination: File)

    private fun createPrivacyDestination(displayName: String): File {
        val extension = File(displayName).extension.takeIf { it.isNotBlank() }
        val suffix = extension?.let { ".$it" }.orEmpty()
        return generateSequence {
            File(
                privacyDir,
                "${UUID.randomUUID()}$suffix",
            )
        }.first { !it.exists() }
    }

    private suspend fun reconcileReservations(
        reservations: List<HideReservation>,
        moveResult: Result<Map<Uri, File?>>,
    ) {
        val movedFiles = moveResult.getOrNull()
        val (committed, failed) = reservations.partition { reservation ->
            reservation.destination.exists() &&
                    (movedFiles == null || movedFiles[reservation.sourceUri] == reservation.destination)
        }
        withContext(context = NonCancellable) {
            if (failed.isNotEmpty()) {
                runCatching { hiddenVideoDao.deleteByIds(failed.map { it.rowId }) }
                    .onFailure {
                        logCleanupFailure(
                            operation = "delete failed hide reservations",
                            throwable = it,
                        )
                    }
            }
            if (committed.isNotEmpty()) {
                mediumStateDao.clearLastPlayedTimes(committed.map { it.sourceUri.toString() })
            }
        }
    }

    private suspend fun cleanUpReservations(privacyPaths: List<String>) {
        if (privacyPaths.isEmpty()) return
        withContext(context = NonCancellable) {
            runCatching { hiddenVideoDao.deleteByPrivacyPaths(privacyPaths = privacyPaths) }
                .onFailure {
                    logCleanupFailure(
                        operation = "delete reservations by privacy path",
                        throwable = it,
                    )
                }
        }
        revealReservations(privacyPaths = privacyPaths)
    }

    private fun revealReservations(privacyPaths: List<String>) {
        pendingPrivacyPaths.update { it - privacyPaths.toSet() }
    }

    private fun logCleanupFailure(operation: String, throwable: Throwable) {
        Logger.logError(
            tag = TAG,
            message = "Failed to $operation: ${throwable.stackTraceToString()}",
        )
    }

    override suspend fun unhideVideos(videos: List<Video>): UnhideResult =
        privacyMutationMutex.withLock {
            val restored = withContext(Dispatchers.IO) {
                entitiesFor(videos = videos).mapNotNull { entity ->
                    restoreToMediaStore(entity = entity)?.let { relocated ->
                        entity.id to relocated
                    }
                }
            }
            if (restored.isNotEmpty()) hiddenVideoDao.deleteByIds(restored.map { it.first })
            UnhideResult(relocatedCount = restored.count { (_, relocated) -> relocated })
        }

    private fun restoreToMediaStore(entity: HiddenVideoEntity): Boolean? {
        val privacyFile = File(entity.privacyPath)
        if (!privacyFile.exists()) return null

        val resolver = context.contentResolver
        val inserted = insertPendingItem(entity = entity) ?: return null

        return try {
            resolver.openOutputStream(inserted.uri)?.use { output ->
                privacyFile.inputStream().use { it.copyTo(out = output) }
            } ?: error(message = "Unable to open output stream for ${inserted.uri}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(
                    inserted.uri,
                    ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.IS_PENDING,
                            0,
                        )
                    },
                    null,
                    null,
                )
            }
            privacyFile.delete()
            inserted.relocated
        } catch (e: Exception) {
            resolver.delete(
                inserted.uri,
                null,
                null,
            )
            null
        }
    }

    private fun insertPendingItem(entity: HiddenVideoEntity): InsertedItem? {
        val resolver = context.contentResolver
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(File(entity.privacyPath).extension.lowercase()) ?: "video/*"

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    entity.displayName,
                )
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mimeType,
                )
                put(
                    MediaStore.MediaColumns.DATA,
                    entity.originalPath,
                )
            }
            val uri = runCatching {
                resolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values,
                )
            }.getOrNull()
            return uri?.let {
                InsertedItem(
                    uri = it,
                    relocated = false,
                )
            }
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val originalDir = relativePathFor(entity.originalPath)
        val candidateDirs = listOfNotNull(
            originalDir,
            DEFAULT_RESTORE_DIR,
        ).distinct()

        return candidateDirs.firstNotNullOfOrNull { relativePath ->
            val values = ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    entity.displayName,
                )
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mimeType,
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    relativePath,
                )
                put(
                    MediaStore.MediaColumns.IS_PENDING,
                    1,
                )
            }
            runCatching {
                resolver.insert(
                    collection,
                    values,
                )
            }.getOrNull()
                ?.let {
                    InsertedItem(
                        uri = it,
                        relocated = relativePath != originalDir,
                    )
                }
        }
    }

    private data class InsertedItem(val uri: Uri, val relocated: Boolean)

    private fun relativePathFor(originalPath: String): String? {
        val root = Environment.getExternalStorageDirectory().path
        val parent = File(originalPath).parent ?: return null
        if (!parent.startsWith(prefix = root)) return null
        return parent.removePrefix(prefix = root).trim('/').takeIf {
            it.isNotEmpty()
        }?.let { "$it/" }
    }

    override suspend fun deleteHiddenVideos(videos: List<Video>): Unit =
        privacyMutationMutex.withLock {
            val entities = entitiesFor(videos = videos)
            if (entities.isEmpty()) return@withLock
            entities.forEach { File(it.privacyPath).delete() }
            hiddenVideoDao.deleteByIds(entities.map { it.id })
        }

    override suspend fun getHiddenVideoInfo(id: Long): MediaInfo? {
        val entity = hiddenVideoDao.getById(id = id) ?: return null
        val video = entity.toVideo()
        val mediaInfo = runCatching {
            MediaInfoBuilder().from(
                context = context,
                uri = Uri.fromFile(File(entity.privacyPath)),
            ).build()
        }.getOrNull()

        return MediaInfo(
            video = video.copy(format = mediaInfo?.format),
            videoStream = mediaInfo?.videoStream?.toVideoStreamInfo(),
            audioStreams = mediaInfo?.audioStreams?.map { it.toAudioStreamInfo() } ?: emptyList(),
            subtitleStreams =
                mediaInfo?.subtitleStreams?.map { it.toSubtitleStreamInfo() } ?: emptyList(),
        ).also { mediaInfo?.release() }
    }

    private suspend fun entitiesFor(videos: List<Video>): List<HiddenVideoEntity> {
        val ids = videos.map { it.id }.toSet()
        return hiddenVideoDao.getAll().first().filter { it.id in ids }
    }

    private fun fileProviderUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    private fun HiddenVideoEntity.toVideo(): Video {
        val uriString = fileProviderUri(file = File(privacyPath)).toString()
        return Video(
            id = id,
            path = privacyPath,
            parentPath = File(privacyPath).parent ?: "",
            duration = duration,
            uriString = uriString,
            nameWithExtension = displayName,
            width = width,
            height = height,
            size = size,
            dateModified = hiddenAt,
            dateAdded = hiddenAt / 1000L,
            formattedDuration = Utils.formatDurationMillis(millis = duration),
            formattedFileSize = Utils.formatFileSize(size = size),
        )
    }

    companion object {
        private const val TAG = "LocalPrivacyRepository"
        private const val PRIVACY_DIR_NAME = "privacy"

        private val DEFAULT_RESTORE_DIR = Environment.DIRECTORY_MOVIES + "/"
    }
}
