package com.sipun.playora.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SortTest {

    private val sampleVideo1 = Video.sample.copy(
        id = 1,
        path = "/storage/emulated/0/Movies/ep1.mp4",
        nameWithExtension = "Episode 1.mp4",
        duration = 10_000,
        size = 1000,
        dateModified = 100,
    )

    private val sampleVideo2 = Video.sample.copy(
        id = 2,
        path = "/storage/emulated/0/Movies/ep2.mp4",
        nameWithExtension = "Episode 2.mp4",
        duration = 50_000,
        size = 5000,
        dateModified = 200,
    )

    private val sampleVideo10 = Video.sample.copy(
        id = 10,
        path = "/storage/emulated/0/Movies/ep10.mp4",
        nameWithExtension = "Episode 10.mp4",
        duration = 30_000,
        size = 3000,
        dateModified = 300,
    )

    @Test
    fun testNaturalAlphanumericSortingByTitle() {
        val sortAsc = Sort(by = Sort.By.TITLE, order = Sort.Order.ASCENDING)
        val videos = listOf(sampleVideo10, sampleVideo2, sampleVideo1)
        val sorted = videos.sortedWith(sortAsc.videoComparator())

        // Natural sort: Episode 1, Episode 2, Episode 10 (not Episode 1, Episode 10, Episode 2)
        assertEquals(listOf(sampleVideo1, sampleVideo2, sampleVideo10), sorted)

        val sortDesc = Sort(by = Sort.By.TITLE, order = Sort.Order.DESCENDING)
        val sortedDesc = videos.sortedWith(sortDesc.videoComparator())
        assertEquals(listOf(sampleVideo10, sampleVideo2, sampleVideo1), sortedDesc)
    }

    @Test
    fun testSortingByDuration() {
        val sortAsc = Sort(by = Sort.By.LENGTH, order = Sort.Order.ASCENDING)
        val videos = listOf(sampleVideo2, sampleVideo10, sampleVideo1)
        val sorted = videos.sortedWith(sortAsc.videoComparator())

        // Durations: 10_000 (v1), 30_000 (v10), 50_000 (v2)
        assertEquals(listOf(sampleVideo1, sampleVideo10, sampleVideo2), sorted)

        val sortDesc = Sort(by = Sort.By.LENGTH, order = Sort.Order.DESCENDING)
        val sortedDesc = videos.sortedWith(sortDesc.videoComparator())
        assertEquals(listOf(sampleVideo2, sampleVideo10, sampleVideo1), sortedDesc)
    }

    @Test
    fun testSortingBySize() {
        val sortAsc = Sort(by = Sort.By.SIZE, order = Sort.Order.ASCENDING)
        val videos = listOf(sampleVideo2, sampleVideo10, sampleVideo1)
        val sorted = videos.sortedWith(sortAsc.videoComparator())

        // Sizes: 1000 (v1), 3000 (v10), 5000 (v2)
        assertEquals(listOf(sampleVideo1, sampleVideo10, sampleVideo2), sorted)

        val sortDesc = Sort(by = Sort.By.SIZE, order = Sort.Order.DESCENDING)
        val sortedDesc = videos.sortedWith(sortDesc.videoComparator())
        assertEquals(listOf(sampleVideo2, sampleVideo10, sampleVideo1), sortedDesc)
    }

    @Test
    fun testSortingByDate() {
        val sortAsc = Sort(by = Sort.By.DATE, order = Sort.Order.ASCENDING)
        val videos = listOf(sampleVideo10, sampleVideo1, sampleVideo2)
        val sorted = videos.sortedWith(sortAsc.videoComparator())

        // Dates: 100 (v1), 200 (v2), 300 (v10)
        assertEquals(listOf(sampleVideo1, sampleVideo2, sampleVideo10), sorted)

        val sortDesc = Sort(by = Sort.By.DATE, order = Sort.Order.DESCENDING)
        val sortedDesc = videos.sortedWith(sortDesc.videoComparator())
        assertEquals(listOf(sampleVideo10, sampleVideo2, sampleVideo1), sortedDesc)
    }

    @Test
    fun testSortingByPath() {
        val sortAsc = Sort(by = Sort.By.PATH, order = Sort.Order.ASCENDING)
        val videos = listOf(sampleVideo10, sampleVideo2, sampleVideo1)
        val sorted = videos.sortedWith(sortAsc.videoComparator())

        assertEquals(listOf(sampleVideo1, sampleVideo2, sampleVideo10), sorted)
    }

    @Test
    fun testFolderComparator() {
        val f1 = Folder(name = "Anime 1", path = "/Movies/Anime 1", dateModified = 100)
        val f2 = Folder(name = "Anime 2", path = "/Movies/Anime 2", dateModified = 200)
        val f10 = Folder(name = "Anime 10", path = "/Movies/Anime 10", dateModified = 300)

        val sortAsc = Sort(by = Sort.By.TITLE, order = Sort.Order.ASCENDING)
        val folders = listOf(f10, f2, f1)
        val sorted = folders.sortedWith(sortAsc.folderComparator())

        assertEquals(listOf(f1, f2, f10), sorted)

        val sortDesc = Sort(by = Sort.By.TITLE, order = Sort.Order.DESCENDING)
        val sortedDesc = folders.sortedWith(sortDesc.folderComparator())
        assertEquals(listOf(f10, f2, f1), sortedDesc)
    }
}
