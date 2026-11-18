package com.sipun.playora.core.media.sync

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil3.ImageLoader
import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.common.extensions.getStorageVolumes
import com.sipun.playora.core.common.extensions.scanPaths
import com.sipun.playora.core.common.extensions.scanStorage
import com.sipun.playora.core.common.storagePermission
import com.sipun.playora.core.database.converter.UriListConverter
import com.sipun.playora.core.database.dao.MediumStateDao
import com.sipun.playora.core.media.services.MediaService
import com.sipun.playora.core.media.services.MediaVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface MediaSynchronizer {
    suspend fun refresh(path: String? = null): Boolean
    fun startSync()
    fun stopSync()
}

@Single
class LocalMediaSynchronizer(
    private val mediumStateDao: MediumStateDao,
    private val imageLoader: ImageLoader,
    private val mediaService: MediaService,
    @Named(DiQualifiers.APPLICATION_SCOPE) private val applicationScope: CoroutineScope,
    private val context: Context,
) : MediaSynchronizer {

    private var mediaSyncingJob: Job? = null

    override suspend fun refresh(path: String?): Boolean = path?.let {
        context.scanPaths(paths = listOf(element = path))
    }
        ?: context.getStorageVolumes().all { context.scanStorage(it.path) }

    override fun startSync() {
        if (mediaSyncingJob?.isActive == true) return
        mediaSyncingJob = mediaService.observeVideos()
            .onEach { media -> updateMedia(media = media) }
            .catch {

            }
            .launchIn(scope = applicationScope)
    }

    override fun stopSync() {
        mediaSyncingJob?.cancel()
    }

    private fun Context.hasStoragePermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        storagePermission,
    ) ==
            PackageManager.PERMISSION_GRANTED

    private suspend fun updateMedia(media: List<MediaVideo>) = withContext(Dispatchers.Default) {
        if (!context.hasStoragePermission()) return@withContext

        val currentMediaUris = media.mapTo(destination = mutableSetOf()) { it.uri.toString() }

        val (wantedMediaStates, unwantedMediaStates) = mediumStateDao.getAll().first().partition {
            it.uriString in currentMediaUris ||
                    !ContentResolver.SCHEME_CONTENT.equals(
                        it.uriString.toUri().scheme,
                        ignoreCase = true,
                    )
        }

        mediumStateDao.delete(unwantedMediaStates.map { it.uriString })

        unwantedMediaStates.forEach { mediaState ->
            try {
                imageLoader.diskCache?.remove(mediaState.uriString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        launch {
            val currentMediaExternalSubs = wantedMediaStates.flatMap {
                UriListConverter.fromStringToList(it.externalSubs)
            }.toSet()

            unwantedMediaStates.onEach { mediaState ->
                for (sub in UriListConverter.fromStringToList(mediaState.externalSubs)) {
                    if (sub !in currentMediaExternalSubs) {
                        try {
                            context.contentResolver.releasePersistableUriPermission(
                                sub,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
