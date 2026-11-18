package com.sipun.playora.core.domain

import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.common.extensions.prettyName
import com.sipun.playora.core.data.repository.MediaRepository
import com.sipun.playora.core.data.repository.PreferencesRepository
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.Sort
import com.sipun.playora.core.model.Video
import com.sipun.playora.core.model.isNew
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import java.io.File

@Factory
class GetFolderTreeMediaUseCase(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(folderPath: String? = null): Flow<MediaHolder> = combine(
        mediaRepository.observeVideos(folderPath = folderPath),
        preferencesRepository.applicationPreferences,
    ) { videos, preferences ->
        val included = videos.filterNot { it.parentPath in preferences.excludeFolders }
        val sort = Sort(
            by = preferences.sortBy,
            order = preferences.sortOrder,
        )

        if (folderPath != null) {
            mediaUnder(
                root = folderPath,
                videos = included,
                excludedFolders = preferences.excludeFolders,
                sort = sort,
            )
        } else {
            topLevelMedia(
                videos = included,
                excludedFolders = preferences.excludeFolders,
                sort = sort,
            )
        }
    }.flowOn(context = defaultDispatcher)

    private fun topLevelMedia(
        videos: List<Video>,
        excludedFolders: Collection<String>,
        sort: Sort,
    ): MediaHolder {
        val volumeRoots = videos.mapNotNull { volumeRootOf(it.path) }.distinct()
        if (volumeRoots.size <= 1) {
            val root = volumeRoots.firstOrNull()
                ?: return MediaHolder(
                    videos = emptyList(),
                    folders = emptyList(),
                )
            return mediaUnder(
                root = root,
                videos = videos,
                excludedFolders = excludedFolders,
                sort = sort,
            )
        }
        val folders = volumeRoots
            .filterNot { it in excludedFolders }
            .map { volumeRoot ->
                summarize(
                    path = volumeRoot,
                    descendantVideos = videosUnder(
                        path = volumeRoot,
                        videos = videos,
                    ),
                )
            }
        return MediaHolder(
            videos = emptyList(),
            folders = folders.sortedWith(sort.folderComparator()),
        )
    }

    private fun mediaUnder(
        root: String,
        videos: List<Video>,
        excludedFolders: Collection<String>,
        sort: Sort,
    ): MediaHolder {
        val descendants = videosUnder(
            path = root,
            videos = videos,
        )
        val directVideos = descendants.filter { it.parentPath == root }
        val folders = immediateChildFolders(
            path = root,
            videos = descendants,
        )
            .filterNot { it in excludedFolders }
            .map { childPath ->
                summarize(
                    path = childPath,
                    descendantVideos = videosUnder(
                        path = childPath,
                        videos = descendants,
                    ),
                )
            }

        return MediaHolder(
            videos = directVideos.sortedWith(sort.videoComparator()),
            folders = folders.sortedWith(sort.folderComparator()),
        )
    }

    private fun summarize(
        path: String,
        descendantVideos: List<Video>,
    ): Folder {
        val file = File(path)
        return Folder(
            name = file.prettyName,
            path = path,
            parentPath = file.parent,
            dateModified = descendantVideos.maxOfOrNull { it.dateModified } ?: 0L,
            totalSize = descendantVideos.sumOf { it.size },
            totalDuration = descendantVideos.sumOf { it.duration },
            videosCount = descendantVideos.count { it.parentPath == path },
            foldersCount = immediateChildFolders(
                path = path,
                videos = descendantVideos,
            ).size,
            newVideosCount = descendantVideos.count { it.isNew() },
        )
    }

    private fun immediateChildFolders(path: String, videos: List<Video>): List<String> {
        val prefix = path + File.separator
        return videos
            .filter { it.parentPath != path }
            .map {
                prefix +
                        it.parentPath.removePrefix(prefix = prefix).substringBefore(File.separator)
            }
            .distinct()
    }

    private fun videosUnder(path: String, videos: List<Video>): List<Video> {
        val prefix = path + File.separator
        return videos.filter { it.path.startsWith(prefix = prefix) }
    }

    private fun volumeRootOf(path: String): String? {
        val parts = path.split('/').filter { it.isNotEmpty() }
        return when {
            parts.size >= 3 && parts[0] == "storage" && parts[1] == "emulated" ->
                "/storage/emulated/${parts[2]}"

            parts.size >= 2 && parts[0] == "storage" -> "/storage/${parts[1]}"

            else -> null
        }
    }
}
