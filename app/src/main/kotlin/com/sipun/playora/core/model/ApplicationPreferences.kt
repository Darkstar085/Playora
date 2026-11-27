package com.sipun.playora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationPreferences(
    val sortBy: Sort.By = Sort.By.TITLE,
    val sortOrder: Sort.Order = Sort.Order.ASCENDING,
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    val useHighContrastDarkTheme: Boolean = false,
    val useDynamicColors: Boolean = false,
    val dynamicPalette: DynamicPalette = DynamicPalette.TONAL_SPOT,
    val customColor: Int = 0xFF2196F3.toInt(),
    val useLiquidGlass: Boolean = true,
    val markLastPlayedMedia: Boolean = true,
    val isHistoryPaused: Boolean = false,
    val excludeFolders: List<String> = emptyList(),
    val mediaViewMode: MediaViewMode = MediaViewMode.FOLDERS,
    val mediaLayoutMode: MediaLayoutMode = MediaLayoutMode.LIST,

    val showDurationField: Boolean = true,
    val showFolderDurationField: Boolean = false,
    val showExtensionField: Boolean = false,
    val showPathField: Boolean = false,
    val showResolutionField: Boolean = true,
    val showSizeField: Boolean = true,
    val showThumbnailField: Boolean = true,
    val showPlayedProgress: Boolean = true,

    val newVideoThresholdDays: Int = 7,

    val thumbnailGenerationStrategy: ThumbnailGenerationStrategy =
        ThumbnailGenerationStrategy.FRAME_AT_PERCENTAGE,
    val thumbnailFramePosition: Float = DEFAULT_THUMBNAIL_FRAME_POSITION,
    val hasCompletedOnboarding: Boolean = false,
    val hasSeenVaultHint: Boolean = false,
) {

    companion object {
        const val DEFAULT_THUMBNAIL_FRAME_POSITION = 0.33f
    }
}
