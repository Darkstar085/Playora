package com.sipun.playora.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class AppUpdate(
    val version: String,
    val name: String,
    val notes: String,
    val downloadUrl: String,
    val fileName: String,
    val size: Long,
    val digest: String,
    val releaseDate: String = "",
    val isPrerelease: Boolean = false,
)

object UpdateManager {
    private const val RELEASE_API =
        "https://api.github.com/repos/Darkstar085/playora/releases/latest"

    suspend fun findLatestUpdate(context: Context): AppUpdate? = withContext(Dispatchers.IO) {
        val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Playora")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub returned HTTP ${connection.responseCode}")
            }
            val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (release.optBoolean("draft") || release.optBoolean("prerelease")) return@withContext null

            val version = release.getString("tag_name").removePrefix("v")
            if (!isNewerVersion(currentVersion(context), version)) return@withContext null

            val isPrerelease = release.optBoolean("prerelease")
            val publishedAt =
                release.optString("published_at").ifBlank { release.optString("created_at") }
            val formattedDate = formatReleaseDate(publishedAt)

            val assets = release.getJSONArray("assets")
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                val name = asset.optString("name")
                val digest = asset.optString("digest")
                if (
                    name.endsWith(".apk", ignoreCase = true) &&
                    digest.startsWith("sha256:") &&
                    asset.optString("content_type") == "application/vnd.android.package-archive"
                ) {
                    return@withContext AppUpdate(
                        version = version,
                        name = release.optString("name").ifBlank { "Playora $version" },
                        notes = release.optString("body"),
                        downloadUrl = asset.getString("browser_download_url"),
                        fileName = name,
                        size = asset.optLong("size"),
                        digest = digest,
                        releaseDate = formattedDate,
                        isPrerelease = isPrerelease,
                    )
                }
            }
            throw IllegalStateException("Latest release has no APK with a SHA-256 digest")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun download(
        context: Context,
        update: AppUpdate,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        require(update.fileName.matches(Regex("[A-Za-z0-9._-]+\\.apk", RegexOption.IGNORE_CASE))) {
            "Invalid APK filename"
        }
        val downloadUri = update.downloadUrl.toUri()
        require(downloadUri.scheme == "https" && downloadUri.host == "github.com") {
            "Update download URL is not a trusted GitHub release URL"
        }
        val directory = File(context.filesDir, "updates").apply { mkdirs() }
        val partial = File(directory, update.fileName + ".part")
        val apk = File(directory, update.fileName)
        partial.delete()

        val connection = (URL(update.downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", "Playora")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Download failed: HTTP ${connection.responseCode}")
            }
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: update.size
            var downloaded = 0L
            connection.inputStream.use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        onProgress(downloaded, total)
                    }
                }
            }
        } catch (error: Exception) {
            partial.delete()
            throw error
        } finally {
            connection.disconnect()
        }

        if (!verifyDigest(partial, update.digest) || !verifyPackage(context, partial)) {
            partial.delete()
            throw SecurityException("Downloaded APK failed integrity or signature verification")
        }
        apk.delete()
        if (!partial.renameTo(apk)) {
            partial.delete()
            throw IllegalStateException("Could not save downloaded APK")
        }
        apk
    }

    private fun formatReleaseDate(isoDate: String): String {
        if (isoDate.isBlank()) return "Recently"
        return try {
            val parser =
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
            val date = parser.parse(isoDate) ?: return "Recently"
            val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
            formatter.format(date)
        } catch (_: Exception) {
            try {
                val datePart = isoDate.take(10)
                val parts = datePart.split("-")
                val year = parts.getOrNull(0) ?: ""
                val monthNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
                val day = parts.getOrNull(2)?.toIntOrNull() ?: 1
                val months = listOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec",
                )
                val month = months.getOrElse(monthNum - 1) { "Jan" }
                "$month $day, $year"
            } catch (_: Exception) {
                "Recently"
            }
        }
    }

    fun install(context: Context, apk: File): Boolean {
        if (!apk.isFile) return false
        if (!context.packageManager.canRequestPackageInstalls()) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:${context.packageName}".toUri(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return false
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun currentVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()

    internal fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.removePrefix("v").split(".")
        val latestParts = latest.removePrefix("v").split(".")
        for (index in 0 until maxOf(currentParts.size, latestParts.size)) {
            val currentPart =
                currentParts.getOrNull(index)?.takeWhile(Char::isDigit)?.toIntOrNull() ?: 0
            val latestPart =
                latestParts.getOrNull(index)?.takeWhile(Char::isDigit)?.toIntOrNull() ?: 0
            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    private fun verifyDigest(file: File, expected: String): Boolean {
        val hash = expected.removePrefix("sha256:").lowercase()
        if (hash.length != 64 || hash.any { it !in "0123456789abcdef" }) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) } == hash
    }

    @Suppress("DEPRECATION")
    private fun verifyPackage(context: Context, apk: File): Boolean {
        val flags = PackageManager.GET_SIGNATURES
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        val archived =
            context.packageManager.getPackageArchiveInfo(apk.absolutePath, flags) ?: return false
        val installedSignatures = installed.signatures ?: return false
        val archivedSignatures = archived.signatures ?: return false
        return archived.packageName == context.packageName &&
                installedSignatures.size == archivedSignatures.size &&
                installedSignatures.zip(archivedSignatures).all { (left, right) ->
                    left.toByteArray().contentEquals(right.toByteArray())
                }
    }
}
