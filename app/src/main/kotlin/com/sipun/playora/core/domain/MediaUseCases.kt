package com.sipun.playora.core.domain

import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.common.extensions.prettyName
import com.sipun.playora.core.data.repository.MediaRepository
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.data.repository.PrivacyRepository
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.MediaViewMode
import com.sipun.playora.core.model.Sort
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.model.isNew
import com.sipun.playora.core.model.recentPlayed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import java.io.File

@Factory
class GetHiddenVideosUseCase(
    private val privacyRepository: PrivacyRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(sort: Sort): Flow<List<Video>> = privacyRepository.observeHiddenVideos()
        .map { videos -> videos.sortedWith(sort.videoComparator()) }
        .flowOn(context = defaultDispatcher)
}

@Factory
class GetSortedVideosUseCase(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(folderPath: String? = null): Flow<List<Video>> = combine(
        mediaRepository.observeVideos(folderPath = folderPath),
        preferencesRepository.applicationPreferences,
    ) { videoItems, preferences ->
        val nonExcludedVideos = videoItems.filterNot { it.parentPath in preferences.excludeFolders }
        val sort = Sort(
            by = preferences.sortBy,
            order = preferences.sortOrder,
        )
        nonExcludedVideos.sortedWith(sort.videoComparator())
    }.flowOn(context = defaultDispatcher)
}

@Factory
class GetSortedFoldersUseCase(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(folderPath: String? = null): Flow<List<Folder>> = combine(
        mediaRepository.observeVideos(folderPath = folderPath),
        preferencesRepository.applicationPreferences,
    ) { videos, preferences ->
        val folders = videos
            .groupBy { it.parentPath }
            .map { (path, folderVideos) ->
                Folder(
                    name = File(path).prettyName,
                    path = path,
                    parentPath = File(path).parent,
                    dateModified = folderVideos.maxOfOrNull { it.dateModified } ?: 0L,
                    totalSize = folderVideos.sumOf { it.size },
                    totalDuration = folderVideos.sumOf { it.duration },
                    videosCount = folderVideos.size,
                    newVideosCount = folderVideos.count { it.isNew() },
                )
            }
            .filterNot { it.path in preferences.excludeFolders }

        val sort = Sort(
            by = preferences.sortBy,
            order = preferences.sortOrder,
        )
        folders.sortedWith(sort.folderComparator())
    }.flowOn(context = defaultDispatcher)
}

@Factory
class GetSortedMediaUseCase(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    private val getFolderTreeMediaUseCase: GetFolderTreeMediaUseCase,
    private val preferencesRepository: PreferencesRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(folderPath: String? = null): Flow<MediaHolder?> =
        preferencesRepository.applicationPreferences.flatMapLatest { preferences ->
            when (preferences.mediaViewMode) {
                MediaViewMode.FOLDER_TREE -> {
                    getFolderTreeMediaUseCase(folderPath)
                }

                MediaViewMode.FOLDERS -> {
                    if (folderPath == null) {
                        getSortedFoldersUseCase().map { folders ->
                            MediaHolder(
                                videos = emptyList(),
                                folders = folders,
                            )
                        }
                    } else {
                        getSortedVideosUseCase(folderPath).map { videos ->
                            val directVideos = videos.filter { it.parentPath == folderPath }
                            MediaHolder(
                                videos = directVideos,
                                folders = emptyList(),
                            )
                        }
                    }
                }

                MediaViewMode.VIDEOS -> {
                    getSortedVideosUseCase(folderPath).map { videos ->
                        MediaHolder(
                            videos = videos,
                            folders = emptyList(),
                        )
                    }
                }
            }
        }.flowOn(context = defaultDispatcher)
}

data class MediaHolder(val videos: List<Video>, val folders: List<Folder>)

@Factory
class GetPopularFoldersUseCase(
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(limit: Int = 5): Flow<List<Folder>> = combine(
        flow = getSortedFoldersUseCase(),
        flow2 = getSortedVideosUseCase(),
    ) { folders, videos ->
        folders.sortedWith(
            compareByDescending<Folder> { folder ->
                videos.count { it.parentPath == folder.path && it.lastPlayedAt != null }
            }.thenByDescending { folder ->
                videos.filter { it.parentPath == folder.path }.maxOfOrNull {
                    it.lastPlayedAt?.time ?: 0L
                } ?: 0L
            }.thenByDescending { folder -> folder.videosCount },
        ).take(n = limit)
    }.flowOn(context = defaultDispatcher)
}

@Factory
class GetRecentlyPlayedVideoUseCase(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val preferencesRepository: PreferencesRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(folderPath: String? = null): Flow<Video?> =
        preferencesRepository.applicationPreferences.flatMapLatest { preferences ->
            getSortedVideosUseCase(folderPath).map { videos ->
                val filteredVideos = if (folderPath != null) {
                    when (preferences.mediaViewMode) {
                        MediaViewMode.FOLDER_TREE -> videos
                        MediaViewMode.FOLDERS -> videos.filter { it.parentPath == folderPath }
                        MediaViewMode.VIDEOS -> videos
                    }
                } else {
                    videos
                }
                filteredVideos.recentPlayed()
            }
        }.flowOn(context = defaultDispatcher)
}
