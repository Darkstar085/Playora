package com.sipun.playora.core.theme

import androidx.compose.ui.graphics.Color
import com.sipun.playora.core.datastore.serializer.ApplicationPreferencesSerializer
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.DynamicPalette
import com.sipun.playora.core.model.ThemeConfig
import com.sipun.playora.core.ui.theme.wallpaperPalette
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ThemePreferencesTest {

    @Test
    fun testDynamicPaletteEnumEntries() {
        assertEquals(5, DynamicPalette.entries.size)
        assertEquals(DynamicPalette.TONAL_SPOT, DynamicPalette.valueOf("TONAL_SPOT"))
        assertEquals(DynamicPalette.VIBRANT, DynamicPalette.valueOf("VIBRANT"))
        assertEquals(DynamicPalette.EXPRESSIVE, DynamicPalette.valueOf("EXPRESSIVE"))
        assertEquals(DynamicPalette.NEUTRAL, DynamicPalette.valueOf("NEUTRAL"))
        assertEquals(DynamicPalette.MONOCHROME, DynamicPalette.valueOf("MONOCHROME"))
    }

    @Test
    fun testDefaultApplicationPreferencesThemeSettings() {
        val prefs = ApplicationPreferences()
        assertEquals(ThemeConfig.SYSTEM, prefs.themeConfig)
        assertFalse(prefs.useHighContrastDarkTheme)
        assertFalse(prefs.useDynamicColors)
        assertTrue(prefs.useLiquidGlass)
        assertEquals(DynamicPalette.TONAL_SPOT, prefs.dynamicPalette)
        assertEquals(0xFF2196F3.toInt(), prefs.customColor)
    }

    @Test
    fun testCustomThemeAndAccentSerializationRoundTrip() = runBlocking {
        val original = ApplicationPreferences(
            themeConfig = ThemeConfig.ON,
            useHighContrastDarkTheme = true,
            useDynamicColors = false,
            useLiquidGlass = false,
            dynamicPalette = DynamicPalette.VIBRANT,
            customColor = 0xFF42A5F5.toInt(),
        )

        val outputStream = ByteArrayOutputStream()
        ApplicationPreferencesSerializer.writeTo(original, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val deserialized = ApplicationPreferencesSerializer.readFrom(inputStream)

        assertEquals(original.themeConfig, deserialized.themeConfig)
        assertEquals(original.useHighContrastDarkTheme, deserialized.useHighContrastDarkTheme)
        assertEquals(original.useDynamicColors, deserialized.useDynamicColors)
        assertEquals(original.useLiquidGlass, deserialized.useLiquidGlass)
        assertEquals(original.dynamicPalette, deserialized.dynamicPalette)
        assertEquals(original.customColor, deserialized.customColor)
        assertEquals(original, deserialized)
    }

    @Test
    fun testLegacyPreferencesWithoutLiquidGlassKeyDefaultsToTrue() = runBlocking {
        val legacyJson = """{"themeConfig":"ON","useDynamicColors":false}"""
        val deserialized = ApplicationPreferencesSerializer.readFrom(
            ByteArrayInputStream(legacyJson.toByteArray()),
        )

        assertEquals(ThemeConfig.ON, deserialized.themeConfig)
        assertTrue(deserialized.useLiquidGlass)
    }

    @Test
    fun testWallpaperPaletteGeneration() {
        val seed = Color(0xFFE92B2B)

        DynamicPalette.entries.forEach { style ->
            val darkPalette = wallpaperPalette(seed = seed, style = style, darkTheme = true)
            assertNotNull(darkPalette.primary)
            assertNotNull(darkPalette.secondary)
            assertNotNull(darkPalette.tertiary)
            assertNotNull(darkPalette.container)

            val lightPalette = wallpaperPalette(seed = seed, style = style, darkTheme = false)
            assertNotNull(lightPalette.primary)
            assertNotNull(lightPalette.secondary)
            assertNotNull(lightPalette.tertiary)
            assertNotNull(lightPalette.container)
        }
    }

    @Test
    fun testSolidAccentColorHsvGeneration() {
        val hue = 210f
        val color = Color.hsv(hue, 0.78f, 0.92f)
        assertNotNull(color)
        assertTrue(color.red in 0f..1f)
        assertTrue(color.green in 0f..1f)
        assertTrue(color.blue in 0f..1f)
    }
}
