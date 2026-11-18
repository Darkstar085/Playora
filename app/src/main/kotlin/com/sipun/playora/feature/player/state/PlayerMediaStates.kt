package com.sipun.playora.feature.player.state

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.Chapter
import androidx.media3.ui.compose.state.PlayerStateObserver
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import androidx.media3.ui.compose.state.observeState
import com.sipun.playora.feature.player.extensions.switchTrack

@OptIn(UnstableApi::class)
@Composable
fun rememberChaptersState(
    player: Player?,
    progressState: ProgressStateWithTickInterval,
): ChaptersState {
    val chaptersState = remember(
        player,
        progressState,
    ) {
        ChaptersState(
            player = player,
            progressState = progressState,
        )
    }
    LaunchedEffect(
        key1 = player,
        key2 = progressState,
    ) { chaptersState.observe() }
    return chaptersState
}

@UnstableApi
class ChaptersState(
    private val player: Player?,
    private val progressState: ProgressStateWithTickInterval,
) {
    var chapters: List<Chapter> by mutableStateOf(value = emptyList())
        private set

    val currentChapterIndex: Int by derivedStateOf {
        chapters.currentChapterIndex(progressState.currentPositionMs)
    }

    private val playerStateObserver: PlayerStateObserver? = player?.observeState(
        Player.EVENT_TRACKS_CHANGED,
        Player.EVENT_TIMELINE_CHANGED,
        Player.EVENT_MEDIA_ITEM_TRANSITION,
        Player.EVENT_POSITION_DISCONTINUITY,
        Player.EVENT_PLAYBACK_STATE_CHANGED,
    ) { updateChapters() }

    suspend fun observe() {
        updateChapters()
        playerStateObserver?.observe()
    }

    private fun updateChapters() {
        chapters = player?.let {
            if (player.currentTimeline.isEmpty) {
                emptyList()
            } else {
                val periodOffsetMs = player.currentTimeline.getPeriod(
                    player.currentPeriodIndex,
                    Timeline.Period(),
                ).positionInWindowMs
                player.currentTracks.chapters(
                    player.duration,
                    periodOffsetMs = periodOffsetMs,
                )
            }
        } ?: emptyList()
    }
}

@OptIn(UnstableApi::class)
private fun Tracks.chapters(durationMs: Long, periodOffsetMs: Long = 0): List<Chapter> {
    if (durationMs <= 0) return emptyList()
    return groups.flatMap { group ->
        (0 until group.length).flatMap { trackIndex ->
            group.getTrackFormat(
                trackIndex,
            ).metadata?.getEntriesOfType(Chapter::class.java).orEmpty()
        }
    }.mapNotNull { chapter ->
        if (chapter.isHidden || chapter.startTimeMs < 0) return@mapNotNull null
        val start = chapter.startTimeMs + periodOffsetMs
        if (start < 0 && periodOffsetMs >= 0) return@mapNotNull null
        val end = if (chapter.endTimeMs == C.TIME_UNSET) {
            C.TIME_UNSET
        } else {
            chapter.endTimeMs + periodOffsetMs
        }
        if (start >= durationMs || (end != C.TIME_UNSET && end <= 0)) return@mapNotNull null
        if (periodOffsetMs == 0L) {
            chapter
        } else {
            Chapter.Builder()
                .setStartTimeMs(start.coerceAtLeast(minimumValue = 0))
                .setEndTimeMs(end)
                .setTitle(chapter.title)
                .build()
        }
    }.sortedBy { it.startTimeMs }.distinctBy { it.startTimeMs }
}

@OptIn(UnstableApi::class)
internal fun List<Chapter>.currentChapterIndex(positionMs: Long): Int =
    indexOfLast { it.startTimeMs <= positionMs }

@UnstableApi
@Composable
fun rememberCuesState(player: Player?): CuesState {
    val cuesState = remember(player) { CuesState(player = player) }
    LaunchedEffect(key1 = player) { cuesState.observe() }
    return cuesState
}

@UnstableApi
class CuesState(private val player: Player?) {
    var currentCues: CueGroup? by mutableStateOf(value = null)
        private set

    private var selectedTextFormat: Format? by mutableStateOf(null)

    val canReposition: Boolean
        get() = canRepositionSubtitles(
            selectedTextFormat,
            currentCues?.cues.orEmpty(),
        )

    private fun update() {
        currentCues = player?.currentCues
        selectedTextFormat = player?.currentTracks?.groups?.firstNotNullOfOrNull { group ->
            if (group.type != C.TRACK_TYPE_TEXT) return@firstNotNullOfOrNull null
            (0 until group.length).firstOrNull { group.isTrackSelected(it) }
                ?.let { group.getTrackFormat(it) }
        }
    }

    private val playerStateObserver: PlayerStateObserver? = player?.observeState(
        Player.EVENT_CUES,
        Player.EVENT_TRACKS_CHANGED,
        Player.EVENT_MEDIA_ITEM_TRANSITION,
    ) {
        update()
    }

    suspend fun observe() {
        update()
        playerStateObserver?.observe()
    }
}

@UnstableApi
internal fun canRepositionSubtitles(format: Format?, cues: List<Cue>): Boolean {

    val mimeType = if (format?.sampleMimeType ==
        MimeTypes.APPLICATION_MEDIA3_CUES
    ) {
        format.codecs
    } else {
        format?.sampleMimeType
    }
    return mimeType != MimeTypes.TEXT_SSA && cues.none { it.bitmap != null }
}

@OptIn(UnstableApi::class)
@Composable
fun rememberMetadataState(player: Player?): MetadataState {
    val metadataState = remember(player) { MetadataState(player = player) }
    LaunchedEffect(key1 = player) { metadataState.observe() }
    return metadataState
}

@UnstableApi
@Stable
class MetadataState(private val player: Player?) {
    var mediaId: String? by mutableStateOf(value = null)
        private set

    var title: String? by mutableStateOf(value = null)
        private set

    private val playerStateObserver: PlayerStateObserver? = player?.observeState(
        Player.EVENT_MEDIA_METADATA_CHANGED,
        Player.EVENT_MEDIA_ITEM_TRANSITION,
    ) {
        mediaId = player.currentMediaItem?.mediaId
        title = player.mediaMetadata.title?.toString()
    }

    suspend fun observe() {
        mediaId = player?.currentMediaItem?.mediaId
        title = player?.mediaMetadata?.title?.toString()
        playerStateObserver?.observe()
    }
}

@UnstableApi
@Composable
fun rememberTracksState(player: Player?, trackType: @C.TrackType Int): TracksState {
    val tracksState = remember(player) {
        TracksState(
            player = player,
            trackType = trackType,
        )
    }
    LaunchedEffect(key1 = player) { tracksState.observe() }
    return tracksState
}

@UnstableApi
class TracksState(private val player: Player?, private val trackType: @C.TrackType Int) {
    var tracks: List<Tracks.Group> by mutableStateOf(value = emptyList())
        private set

    private val playerStateObserver: PlayerStateObserver? =
        player?.observeState(Player.EVENT_TRACKS_CHANGED) { updateTracks() }

    fun switchTrack(index: Int) {
        player?.switchTrack(
            trackType = trackType,
            trackIndex = index,
        )
    }

    suspend fun observe() {
        updateTracks()
        playerStateObserver?.observe()
    }

    private fun updateTracks() {
        tracks = player?.currentTracks?.groups
            ?.filter { it.type == trackType && it.isSupported } ?: emptyList()
    }
}
