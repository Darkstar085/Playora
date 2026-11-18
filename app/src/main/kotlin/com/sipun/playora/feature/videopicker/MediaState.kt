package com.sipun.playora.feature.videopicker.screens

import com.sipun.playora.core.model.Folder

sealed interface MediaState {
    data object Loading : MediaState
    data class Success(val data: Folder?) : MediaState
}
