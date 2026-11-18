package com.sipun.playora.core.domain

import com.sipun.playora.core.common.di.DiQualifiers
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.Video
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

data class SearchResults(
    val folders: List<Folder> = emptyList(),
    val videos: List<Video> = emptyList(),
) {
    val isEmpty: Boolean
        get() = folders.isEmpty() && videos.isEmpty()

    val totalCount: Int
        get() = folders.size + videos.size
}

@Factory
class SearchMediaUseCase(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val getSortedFoldersUseCase: GetSortedFoldersUseCase,
    @Named(DiQualifiers.DEFAULT_DISPATCHER) private val defaultDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(query: String): Flow<SearchResults> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return flowOf(value = SearchResults())
        }

        return combine(
            flow = getSortedVideosUseCase(),
            flow2 = getSortedFoldersUseCase(),
        ) { videos, folders ->
            val searchMatcher = SearchMatcher(query = normalizedQuery)

            val scoredFolders = folders.mapNotNull { folder ->
                val score = searchMatcher.calculateScore(
                    folder.name,
                    folder.path,
                )
                if (score > 0) folder to score else null
            }.sortedByDescending { it.second }.map { it.first }

            val scoredVideos = videos.mapNotNull { video ->
                val score = searchMatcher.calculateScore(
                    video.nameWithExtension,
                    video.path,
                )
                if (score > 0) video to score else null
            }.sortedByDescending { it.second }.map { it.first }

            SearchResults(
                folders = scoredFolders,
                videos = scoredVideos,
            )
        }.flowOn(context = defaultDispatcher)
    }
}

private class SearchMatcher(query: String) {
    private val queryLower = query.lowercase()
    private val queryWords = query.lowercase().split(regex = Regex(pattern = "\\s+")).filter {
        it.isNotBlank()
    }

    private val wordsInOrderPattern: Regex? = if (queryWords.size > 1) {
        Regex(
            queryWords.joinToString(separator = ".*") { Regex.escape(literal = it) },
            RegexOption.IGNORE_CASE,
        )
    } else {
        null
    }

    fun calculateScore(vararg texts: String): Int {
        var bestScore = 0

        for (text in texts) {
            val textLower = text.lowercase()
            val score = calculateTextScore(textLower = textLower)
            if (score > bestScore) {
                bestScore = score
            }
        }

        return bestScore
    }

    private fun calculateTextScore(textLower: String): Int {

        if (textLower.contains(other = queryLower)) {

            val wordBoundaryBonus = if (isWordBoundaryMatch(
                    text = textLower,
                    query = queryLower,
                )
            ) {
                50
            } else {
                0
            }

            val startBonus = if (textLower.startsWith(prefix = queryLower)) 30 else 0
            return 1000 + wordBoundaryBonus + startBonus
        }

        if (queryWords.size <= 1) {
            return 0
        }

        wordsInOrderPattern?.let { pattern ->
            if (pattern.containsMatchIn(input = textLower)) {
                return 500
            }
        }

        val allWordsPresent = queryWords.all { word -> textLower.contains(other = word) }
        if (allWordsPresent) {

            val boundaryMatches = queryWords.count { word ->
                isWordBoundaryMatch(
                    text = textLower,
                    query = word,
                )
            }
            return 200 + (boundaryMatches * 20)
        }

        return 0
    }

    private fun isWordBoundaryMatch(text: String, query: String): Boolean {
        val index = text.indexOf(string = query)
        if (index == -1) return false

        val isAtStart = index == 0
        val isPrecededByBoundary = index > 0 && text[index - 1].isWordBoundary()

        return isAtStart || isPrecededByBoundary
    }

    private fun Char.isWordBoundary(): Boolean = this in listOf(
        ' ',
        '_',
        '-',
        '.',
        '/',
        '\\',
        '[',
        ']',
        '(',
        ')',
    )
}
