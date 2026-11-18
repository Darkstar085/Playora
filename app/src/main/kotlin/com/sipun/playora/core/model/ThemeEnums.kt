package com.sipun.playora.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class DynamicPalette {
    TONAL_SPOT,
    VIBRANT,
    EXPRESSIVE,
    NEUTRAL,
    MONOCHROME,
}
