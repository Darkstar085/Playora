package com.sipun.playora.core.domain

import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.Video
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultsTest {

    @Test
    fun testEmptySearchResults() {
        val results = SearchResults()
        assertTrue(results.isEmpty)
        assertEquals(0, results.totalCount)
        assertTrue(results.folders.isEmpty())
        assertTrue(results.videos.isEmpty())
    }

    @Test
    fun testNonEmptySearchResults() {
        val results = SearchResults(
            folders = listOf(Folder.sample),
            videos = listOf(Video.sample),
        )
        assertFalse(results.isEmpty)
        assertEquals(2, results.totalCount)
    }

    @Test
    fun testOnlyVideosOrOnlyFolders() {
        val videosOnly = SearchResults(videos = listOf(Video.sample))
        assertFalse(videosOnly.isEmpty)
        assertEquals(1, videosOnly.totalCount)

        val foldersOnly = SearchResults(folders = listOf(Folder.sample))
        assertFalse(foldersOnly.isEmpty)
        assertEquals(1, foldersOnly.totalCount)
    }
}
