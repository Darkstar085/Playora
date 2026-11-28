package com.sipun.playora.settings.utils

import java.util.Locale

object LocalesHelper {

    fun getAvailableLocales(): List<Pair<String, String>> = try {
        Locale.getAvailableLocales().mapNotNull { locale ->
            val key = runCatching { locale.isO3Language }.getOrNull()?.trim().orEmpty()
            val language = locale.displayLanguage.trim()
            if (key.isBlank() || language.isBlank()) {
                null
            } else {
                Pair(
                    first = language,
                    second = key,
                )
            }
        }.distinctBy { it.second }.sortedBy { it.first }
    } catch (e: Exception) {
        e.printStackTrace()
        listOf()
    }

    fun getLocaleDisplayLanguage(key: String): String {
        if (key.isBlank()) return ""
        return try {
            Locale.getAvailableLocales().firstOrNull { it.isO3Language == key }?.displayLanguage.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
