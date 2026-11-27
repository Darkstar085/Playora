package com.sipun.playora.feature.videopicker

import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.Video
import com.sipun.playora.feature.videopicker.state.SelectionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionManagerTest {

    private fun sampleFolder(name: String, path: String) = Folder(
        name = name,
        path = path,
        dateModified = 1000L,
    )

    private fun sampleVideo(id: Long, name: String) = Video(
        id = id,
        path = "/storage/emulated/0/Movies/$name.mp4",
        uriString = "content://media/external/video/media/$id",
        nameWithExtension = "$name.mp4",
        duration = 60_000L,
        width = 1920,
        height = 1080,
        size = 10_000_000L,
        dateModified = 1000L,
    )

    @Test
    fun toggleVideoUpdatesSelectionModeAndSingleVideoFlag() {
        val manager = SelectionManager()
        val video1 = sampleVideo(1L, "ClipOne")
        val video2 = sampleVideo(2L, "ClipTwo")

        assertFalse(manager.isInSelectionMode)
        assertFalse(manager.isSingleVideoSelected)

        manager.toggleVideoSelection(video1)
        assertTrue(manager.isInSelectionMode)
        assertTrue(manager.isSingleVideoSelected)
        assertTrue(manager.isVideoSelected(video1))
        assertEquals(1, manager.selectionItems.size)

        manager.toggleVideoSelection(video2)
        assertTrue(manager.isInSelectionMode)
        assertFalse(manager.isSingleVideoSelected)
        assertEquals(2, manager.selectionItems.size)

        manager.toggleVideoSelection(video1)
        assertTrue(manager.isInSelectionMode)
        assertTrue(manager.isSingleVideoSelected)

        manager.toggleVideoSelection(video2)
        assertFalse(manager.isInSelectionMode)
        assertFalse(manager.isSingleVideoSelected)
        assertTrue(manager.selectionItems.isEmpty())
    }

    @Test
    fun folderSelectionAndClearExitSelectionModeBehavior() {
        val manager = SelectionManager()
        val folder1 = sampleFolder("Movies", "/storage/emulated/0/Movies")
        val folder2 = sampleFolder("Shows", "/storage/emulated/0/Shows")
        val video = sampleVideo(10L, "Trailer")

        manager.selectFolder(folder1)
        manager.toggleFolderSelection(folder2)
        assertTrue(manager.isInSelectionMode)
        assertTrue(manager.isFolderSelected(folder1))
        assertTrue(manager.isFolderSelected(folder2))
        assertFalse(manager.isSingleVideoSelected)

        manager.toggleFolderSelection(folder2)
        assertFalse(manager.isFolderSelected(folder2))

        manager.selectVideo(video)
        assertEquals(2, manager.selectionItems.size)

        manager.clearSelection()
        assertTrue(manager.selectionItems.isEmpty())
        assertTrue(manager.isInSelectionMode)

        manager.exitSelectionMode()
        assertFalse(manager.isInSelectionMode)
        assertTrue(manager.selectionItems.isEmpty())
    }
}
