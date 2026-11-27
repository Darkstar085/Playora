package com.sipun.playora.core.common

import android.Manifest
import android.os.Build
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

val storagePermission = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_VIDEO
    else -> Manifest.permission.READ_EXTERNAL_STORAGE
}

object Utils {

    fun formatDurationMillis(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes) -
                TimeUnit.HOURS.toSeconds(hours)
        return if (hours > 0) {
            String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds,
            )
        } else {
            String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds,
            )
        }
    }

    fun formatDurationMillisSign(millis: Long): String = if (millis >= 0) {
        "+${formatDurationMillis(millis = millis)}"
    } else {
        "-${formatDurationMillis(millis = abs(n = millis))}"
    }

    fun formatFileSize(size: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            size < kb -> "$size B"
            size < mb -> String.format(Locale.US, "%.2f KB", size / kb.toDouble())
            size < gb -> String.format(Locale.US, "%.2f MB", size / mb.toDouble())
            else -> String.format(Locale.US, "%.2f GB", size / gb.toDouble())
        }
    }

    fun formatBitrate(bitrate: Long): String? {
        if (bitrate <= 0) {
            return null
        }

        val kiloBitrate = bitrate.toDouble() / 1000.0
        val megaBitrate = kiloBitrate / 1000.0
        val gigaBitrate = megaBitrate / 1000.0

        return when {
            gigaBitrate >= 1.0 -> String.format(
                Locale.US,
                "%.1f Gbps",
                gigaBitrate,
            )

            megaBitrate >= 1.0 -> String.format(
                Locale.US,
                "%.1f Mbps",
                megaBitrate,
            )

            kiloBitrate >= 1.0 -> String.format(
                Locale.US,
                "%.1f kbps",
                kiloBitrate,
            )

            else -> String.format(
                Locale.US,
                "%d bps",
                bitrate,
            )
        }
    }

    fun formatLanguage(language: String?): String? = language?.let { lang ->
        Locale.forLanguageTag(lang).displayLanguage.takeIf { it.isNotEmpty() }
    }
}
