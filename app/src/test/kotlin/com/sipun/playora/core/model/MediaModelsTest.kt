package com.sipun.playora.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class MediaModelsTest {

    @Test
    fun testFindClosestFolder() {
        val folder1 =
            Folder(name = "Movies", path = "/storage/emulated/0/Movies", dateModified = 1000)
        val folder2 =
            Folder(name = "Action", path = "/storage/emulated/0/Movies/Action", dateModified = 2000)
        val folders = listOf(folder1, folder2)

        val closest1 = folders.findClosestFolder("/storage/emulated/0/Movies/Action/film.mp4")
        assertEquals(folder2, closest1)

        val closest2 = folders.findClosestFolder("/storage/emulated/0/Movies/film.mp4")
        assertEquals(folder1, closest2)

        val closest3 = folders.findClosestFolder("/storage/emulated/0/DCIM/video.mp4")
        assertNull(closest3)
    }

    @Test
    fun testVideoDisplayName() {
        val video = Video.sample.copy(nameWithExtension = "Inception.2010.1080p.mkv")
        assertEquals("Inception.2010.1080p", video.displayName)
    }

    @Test
    fun testVideoPlayedPercentage() {
        val normalVideo = Video.sample.copy(duration = 10_000, playbackPosition = 5_000)
        assertEquals(0.5f, normalVideo.playedPercentage ?: 0f, 0.001f)

        val zeroDurationVideo = Video.sample.copy(duration = 0, playbackPosition = 100)
        assertNull(zeroDurationVideo.playedPercentage)

        val unplayedVideo = Video.sample.copy(duration = 10_000, playbackPosition = null)
        assertNull(unplayedVideo.playedPercentage)

        val negativePositionVideo = Video.sample.copy(duration = 10_000, playbackPosition = -1)
        assertEquals(1f, negativePositionVideo.playedPercentage ?: 0f, 0.001f)
    }

    @Test
    fun testRecentPlayed() {
        val now = System.currentTimeMillis()
        val video1 = Video.sample.copy(id = 1, lastPlayedAt = Date(now - 100_000))
        val video2 = Video.sample.copy(id = 2, lastPlayedAt = Date(now - 10_000))
        val video3 = Video.sample.copy(id = 3, lastPlayedAt = null)

        val list = listOf(video1, video3, video2)
        val recent = list.recentPlayed()
        assertEquals(video2.id, recent?.id)

        val unplayedList = listOf(video3)
        assertNull(unplayedList.recentPlayed())
    }

    @Test
    fun testVideoIsNew() {
        // Unplayed video: lastPlayedAt == null and playbackPosition == null -> isNew is true regardless of dateAdded
        val unplayedVideoNullPos = Video.sample.copy(
            dateAdded = 1000L,
            lastPlayedAt = null,
            playbackPosition = null,
        )
        assertTrue(unplayedVideoNullPos.isNew())

        // Unplayed video: lastPlayedAt == null and playbackPosition <= 0L -> isNew is true
        val unplayedVideoZeroPos = Video.sample.copy(
            dateAdded = 1000L,
            lastPlayedAt = null,
            playbackPosition = 0L,
        )
        assertTrue(unplayedVideoZeroPos.isNew())

        val unplayedVideoNegativePos = Video.sample.copy(
            dateAdded = 1000L,
            lastPlayedAt = null,
            playbackPosition = -5L,
        )
        assertTrue(unplayedVideoNegativePos.isNew())

        // Played video: playbackPosition > 0L even if lastPlayedAt == null -> isNew is false
        val playedPositionVideo = Video.sample.copy(
            lastPlayedAt = null,
            playbackPosition = 150L,
        )
        assertFalse(playedPositionVideo.isNew())

        // Played video: lastPlayedAt is set -> isNew is false regardless of playbackPosition
        val playedDateVideoNullPos = Video.sample.copy(
            lastPlayedAt = Date(),
            playbackPosition = null,
        )
        assertFalse(playedDateVideoNullPos.isNew())

        val playedDateVideoZeroPos = Video.sample.copy(
            lastPlayedAt = Date(),
            playbackPosition = 0L,
        )
        assertFalse(playedDateVideoZeroPos.isNew())

        val playedDateVideoPositivePos = Video.sample.copy(
            lastPlayedAt = Date(),
            playbackPosition = 500L,
        )
        assertFalse(playedDateVideoPositivePos.isNew())
    }

    @Test
    fun testMediaItemTypes() {
        val videoItem: MediaItem = MediaItem.VideoItem(Video.sample)
        val folderItem: MediaItem = MediaItem.FolderItem(Folder.sample)

        assertTrue(videoItem is MediaItem.VideoItem)
        assertTrue(folderItem is MediaItem.FolderItem)
        assertEquals(Video.sample, (videoItem as MediaItem.VideoItem).video)
        assertEquals(Folder.sample, (folderItem as MediaItem.FolderItem).folder)
    }

    @Test
    fun testStreamInfoModels() {
        val audio = AudioStreamInfo(
            index = 1,
            title = "Surround 5.1",
            codecName = "aac",
            language = "eng",
            disposition = 1,
            bitRate = 320_000,
            sampleFormat = "fltp",
            sampleRate = 48_000,
            channels = 6,
            channelLayout = "5.1",
        )
        val video = VideoStreamInfo(
            index = 0,
            title = "Main",
            codecName = "hevc",
            language = null,
            disposition = 1,
            bitRate = 4_500_000,
            frameRate = 23.976,
            frameWidth = 1920,
            frameHeight = 1080,
        )
        val subtitle = SubtitleStreamInfo(
            index = 2,
            title = "English SDH",
            codecName = "subrip",
            language = "eng",
            disposition = 0,
        )
        val mediaInfo = MediaInfo(
            video = Video.sample,
            videoStream = video,
            audioStreams = listOf(audio),
            subtitleStreams = listOf(subtitle),
        )

        assertEquals("hevc", mediaInfo.videoStream?.codecName)
        assertEquals(1, mediaInfo.audioStreams.size)
        assertEquals("English SDH", mediaInfo.subtitleStreams.first().title)
    }
}
