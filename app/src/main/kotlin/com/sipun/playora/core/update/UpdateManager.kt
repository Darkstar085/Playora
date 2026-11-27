package com.sipun.playora.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Serializable
internal data class GithubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String = "",
    val body: String = "",
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GithubAsset> = emptyList(),
)

@Serializable
internal data class GithubAsset(
    @SerialName("url") val apiUrl: String = "",
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("content_type") val contentType: String = "",
    val size: Long = 0,
    val digest: String? = null,
)

@Serializable
internal data class UpdateManifest(
    val version: String,
    val tag: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("download_url") val downloadUrl: String,
    val size: Long = 0,
    val sha256: String,
    @SerialName("published_at") val publishedAt: String? = null,
)

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
    private const val REPOSITORY = "Darkstar085/playora"
    private const val MANIFEST_URL =
        "https://github.com/$REPOSITORY/releases/latest/download/app-release.json"
    private const val RELEASE_API =
        "https://api.github.com/repos/$REPOSITORY/releases/latest"
    private val TRUSTED_DOWNLOAD_HOSTS = setOf("github.com", "api.github.com")
    private val json = Json { ignoreUnknownKeys = true }

    internal sealed interface ManifestLookupResult {
        data class Success(val update: AppUpdate?) : ManifestLookupResult
        data object Unavailable : ManifestLookupResult
    }

    suspend fun findLatestUpdate(context: Context): AppUpdate? = withContext(Dispatchers.IO) {
        val current = currentVersion(context)
        when (val manifestResult = loadManifest(current)) {
            is ManifestLookupResult.Success -> return@withContext manifestResult.update
            ManifestLookupResult.Unavailable -> Unit
        }
        loadLatestRelease(current)
    }

    private fun loadManifest(currentVersion: String): ManifestLookupResult {
        val connection = openMetadataConnection(MANIFEST_URL)
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return ManifestLookupResult.Unavailable
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            parseManifestLookup(raw, currentVersion)
        } catch (_: Exception) {
            ManifestLookupResult.Unavailable
        } finally {
            connection.disconnect()
        }
    }

    private fun loadLatestRelease(currentVersion: String): AppUpdate? {
        val connection = openMetadataConnection(RELEASE_API)
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub returned HTTP ${connection.responseCode}")
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            return parseRelease(raw, currentVersion)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseManifestLookup(
        rawJson: String,
        currentVersion: String,
    ): ManifestLookupResult {
        val manifest = runCatching { json.decodeFromString<UpdateManifest>(rawJson) }.getOrNull()
            ?: return ManifestLookupResult.Unavailable
        val normalizedDigest = normalizeSha256(manifest.sha256)
            ?: return ManifestLookupResult.Unavailable
        val version = manifest.version.removePrefix("v").trim()
        if (version.isBlank() || manifest.downloadUrl.isBlank()) {
            return ManifestLookupResult.Unavailable
        }
        if (!isNewerVersion(currentVersion, version)) {
            return ManifestLookupResult.Success(null)
        }

        val fileName = manifest.downloadUrl
            .substringBefore('?')
            .substringAfterLast('/')
            .ifBlank { "Playora_v$version.apk" }

        return ManifestLookupResult.Success(
            AppUpdate(
                version = version,
                name = manifest.name.ifBlank { "Playora $version" },
                notes = formatReleaseNotes(manifest.description),
                downloadUrl = manifest.downloadUrl,
                fileName = fileName,
                size = manifest.size,
                digest = normalizedDigest,
                releaseDate = formatReleaseDate(manifest.publishedAt.orEmpty()),
                isPrerelease = false,
            ),
        )
    }

    internal fun parseRelease(rawJson: String, currentVersion: String): AppUpdate? {
        val release = json.decodeFromString<GithubRelease>(rawJson)
        if (release.draft || release.prerelease) return null

        val version = release.tagName.removePrefix("v").trim()
        if (!isNewerVersion(currentVersion, version)) return null

        val publishedAt = release.publishedAt?.takeIf { it.isNotBlank() }
            ?: release.createdAt.orEmpty()
        val formattedDate = formatReleaseDate(publishedAt)

        val asset = release.assets.firstOrNull { asset ->
            asset.name.endsWith(".apk", ignoreCase = true) &&
                asset.contentType.equals("application/vnd.android.package-archive", ignoreCase = true) &&
                normalizeSha256(asset.digest.orEmpty()) != null
        } ?: throw IllegalStateException("Latest release has no APK with a SHA-256 digest")

        val normalizedDigest = normalizeSha256(asset.digest.orEmpty())
            ?: throw IllegalStateException("Latest release has no APK with a SHA-256 digest")

        return AppUpdate(
            version = version,
            name = release.name.ifBlank { "Playora $version" },
            notes = formatReleaseNotes(release.body),
            downloadUrl = asset.downloadUrl,
            fileName = asset.name,
            size = asset.size,
            digest = normalizedDigest,
            releaseDate = formattedDate,
            isPrerelease = false,
        )
    }

    suspend fun download(
        context: Context,
        update: AppUpdate,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        require(update.fileName.matches(Regex("[A-Za-z0-9._-]+\\.apk", RegexOption.IGNORE_CASE))) {
            "Invalid APK filename"
        }
        validateDownloadUrl(update.downloadUrl)

        val directory = File(context.filesDir, "updates").apply { mkdirs() }
        val partial = File(directory, update.fileName + ".part")
        val apk = File(directory, update.fileName)

        try {
            downloadWithFallback(update.downloadUrl, partial, update.size, onProgress)
        } catch (error: Exception) {
            partial.delete()
            throw error
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

    private suspend fun downloadWithFallback(
        url: String,
        destination: File,
        expectedSize: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ) {
        var lastError: Exception? = null
        repeat(3) { attempt ->
            try {
                destination.delete()
                downloadFile(url, destination, expectedSize, onProgress)
                return
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                lastError = error
                if (attempt < 2) delay((attempt + 1).seconds)
            }
        }

        val fallbackUrl = resolveDownloadUrl(url)
        if (fallbackUrl != url) {
            validateDownloadUrl(fallbackUrl)
            destination.delete()
            downloadFile(fallbackUrl, destination, expectedSize, onProgress)
            return
        }

        throw lastError ?: IllegalStateException("Download failed")
    }

    private fun downloadFile(
        url: String,
        destination: File,
        expectedSize: Long,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/octet-stream")
            setRequestProperty("User-Agent", "Playora")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Download failed: HTTP ${connection.responseCode}")
            }
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: expectedSize
            var downloaded = 0L
            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
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
        } finally {
            connection.disconnect()
        }
    }

    private fun resolveDownloadUrl(browserDownloadUrl: String): String {
        val direct = URL(browserDownloadUrl)
        val path = direct.path.trim('/').split('/')
        if (path.size < 5 || path[2] != "releases" || path[3] != "download") {
            return browserDownloadUrl
        }

        val owner = path[0]
        val repository = path[1]
        val tag = URLDecoder.decode(path[4], StandardCharsets.UTF_8.name())
        val fileName = URLDecoder.decode(path.last(), StandardCharsets.UTF_8.name())
        val endpoint =
            "https://api.github.com/repos/$owner/$repository/releases/tags/${encodePath(tag)}"
        val connection = openMetadataConnection(endpoint)
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return browserDownloadUrl
            }
            val release = connection.inputStream.bufferedReader().use {
                json.decodeFromString<GithubRelease>(it.readText())
            }
            release.assets.firstOrNull { it.name == fileName }?.apiUrl?.takeIf { it.isNotBlank() }
                ?: browserDownloadUrl
        } catch (_: Exception) {
            browserDownloadUrl
        } finally {
            connection.disconnect()
        }
    }

    private fun validateDownloadUrl(url: String) {
        val downloadUri = url.toUri()
        require(downloadUri.scheme == "https" && downloadUri.host in TRUSTED_DOWNLOAD_HOSTS) {
            "Update download URL is not a trusted GitHub release URL"
        }
    }

    private fun openMetadataConnection(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Playora")
        }

    private fun encodePath(value: String): String =
        value.split('/').joinToString("/") {
            URLEncoder.encode(it, StandardCharsets.UTF_8.name()).replace("+", "%20")
        }

    internal fun formatReleaseNotes(raw: String): String {
        val bulletLines = raw.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("-") || it.startsWith("*") || it.startsWith("•") }
            .filterNot { it.startsWith("**") }
            .map { line ->
                line.removePrefix("-")
                    .removePrefix("*")
                    .removePrefix("•")
                    .trim()
                    .replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1")
            }
            .filter { it.isNotEmpty() }
            .toList()
        if (bulletLines.isNotEmpty()) return bulletLines.joinToString("\n")
        return raw.lineSequence()
            .map { it.trim() }
            .filter {
                it.isNotEmpty() &&
                    !it.startsWith("#") &&
                    !it.startsWith("**Full Changelog:**")
            }
            .map { it.replace(Regex("\\[([^]]+)]\\([^)]*\\)"), "$1") }
            .joinToString("\n")
    }

    private fun formatReleaseDate(isoDate: String): String {
        if (isoDate.isBlank()) return "Recently"
        return runCatching {
            Instant.parse(isoDate)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
        }.getOrElse {
            runCatching {
                val parts = isoDate.take(10).split("-")
                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
            }.getOrDefault("Recently")
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

    private fun normalizeSha256(value: String): String? {
        val hash = value.trim().lowercase().removePrefix("sha256:").trim()
        if (hash.length != 64 || hash.any { it !in "0123456789abcdef" }) return null
        return "sha256:$hash"
    }

    private fun verifyDigest(file: File, expected: String): Boolean {
        val hash = normalizeSha256(expected)?.removePrefix("sha256:") ?: return false
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

    private fun verifyPackage(context: Context, apk: File): Boolean {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        val archived =
            context.packageManager.getPackageArchiveInfo(apk.absolutePath, flags) ?: return false
        if (archived.packageName != context.packageName) return false

        val installedSignatures = installed.signingInfo?.apkContentsSigners ?: return false
        val archivedSignatures = archived.signingInfo?.apkContentsSigners ?: return false
        return installedSignatures.isNotEmpty() &&
            installedSignatures.size == archivedSignatures.size &&
            installedSignatures.zip(archivedSignatures).all { (left, right) ->
                left.toByteArray().contentEquals(right.toByteArray())
            }
    }
}
