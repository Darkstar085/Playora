package com.sipun.playora.core.model

import kotlinx.serialization.Serializable

enum class ControlButtonsPosition {
    LEFT,
    RIGHT,
}

enum class DoubleTapGesture {
    PLAY_PAUSE,
    FAST_FORWARD_AND_REWIND,
    BOTH,
    NONE,
}

enum class FastSeek {
    AUTO,
    ENABLE,
    DISABLE,
}

enum class Font {
    DEFAULT,
    MONOSPACE,
    SANS_SERIF,
    SERIF,
}

@Serializable
enum class LoopMode {
    OFF,
    ONE,
    ALL,
}

enum class MediaLayoutMode {
    LIST,
    GRID,
}

enum class MediaViewMode {
    FOLDER_TREE,
    FOLDERS,
    VIDEOS,
}

enum class Resume {
    YES,
    NO,
}

enum class ScreenOrientation {
    VIDEO_ORIENTATION,
}

enum class ThemeConfig {
    SYSTEM,
    OFF,
    ON,
}

enum class ThumbnailGenerationStrategy {
    FIRST_FRAME,
    FRAME_AT_PERCENTAGE,
    HYBRID,
}

enum class VideoContentScale {
    BEST_FIT,
    STRETCH,
    CROP,
    HUNDRED_PERCENT,
}
