package com.sipun.playora.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hidden_video",
    indices = [
        Index(
            value = ["privacy_path"],
            unique = true,
        ),
    ],
)
data class HiddenVideoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "privacy_path")
    val privacyPath: String,
    @ColumnInfo(name = "original_path")
    val originalPath: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "duration")
    val duration: Long = 0,
    @ColumnInfo(name = "size")
    val size: Long = 0,
    @ColumnInfo(name = "width")
    val width: Int = 0,
    @ColumnInfo(name = "height")
    val height: Int = 0,
    @ColumnInfo(name = "hidden_at")
    val hiddenAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "media_state",
    indices = [
        Index(
            value = ["uri"],
            unique = true,
        ),
    ],
)
data class MediumStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "uri")
    val uriString: String,
    @ColumnInfo(name = "playback_position")
    val playbackPosition: Long = 0,
    @ColumnInfo(name = "audio_track_index")
    val audioTrackIndex: Int? = null,
    @ColumnInfo(name = "subtitle_track_index")
    val subtitleTrackIndex: Int? = null,
    @ColumnInfo(name = "last_played_time")
    val lastPlayedTime: Long? = null,
    @ColumnInfo(name = "duration")
    val duration: Long? = null,
    @ColumnInfo(name = "external_subs")
    val externalSubs: String = "",
    @ColumnInfo(
        name = "external_audio",
        defaultValue = "''",
    )
    val externalAudio: String = "",
    @ColumnInfo(name = "video_scale")
    val videoScale: Float = 1f,
    @ColumnInfo(name = "subtitle_delay")
    val subtitleDelayMilliseconds: Long = 0,
    @ColumnInfo(name = "subtitle_speed")
    val subtitleSpeed: Float = 1f,
)
