package com.sipun.playora.core.common.extensions

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.pow
import kotlin.math.roundToInt

suspend inline fun <T, R> List<T>.mapAsync(crossinline transform: suspend (T) -> R): List<R> =
    coroutineScope { map { async { transform(it) } }.awaitAll() }

suspend fun File.getSubtitles(): List<File> = withContext(Dispatchers.IO) {
    val mediaName = this@getSubtitles.nameWithoutExtension
    val parentDir = this@getSubtitles.parentFile
    val subtitleExtensions = listOf("srt", "ssa", "ass", "vtt", "ttml")

    subtitleExtensions.mapNotNull { extension ->
        val file = File(parentDir, "$mediaName.$extension")
        file.takeIf { it.exists() && it.isFile }
    }
}

suspend fun File.getLocalSubtitles(
    context: Context,
    excludeSubsList: List<Uri> = emptyList(),
): List<Uri> = withContext(Dispatchers.Default) {
    val excludeSubsPathSet = excludeSubsList.mapNotNull { context.getPath(uri = it) }.toSet()
    getSubtitles().mapNotNull { file ->
        if (file.path !in excludeSubsPathSet) {
            file.toUri()
        } else {
            null
        }
    }
}

fun String.getThumbnail(): File? {
    val filePathWithoutExtension = this.substringBeforeLast(delimiter = ".")
    val imageExtensions = listOf("png", "jpg", "jpeg")
    for (imageExtension in imageExtensions) {
        val file = File("$filePathWithoutExtension.$imageExtension")
        if (file.exists()) return file
    }
    return null
}

fun File.isSubtitle(): Boolean {
    val subtitleExtensions = listOf("srt", "ssa", "ass", "vtt", "ttml")
    return extension.lowercase() in subtitleExtensions
}

fun File.deleteFiles() {
    try {
        listFiles()?.onEach { it.delete() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

val File.prettyName: String
    get() = name.takeIf { parent?.replace('\\', '/') != "/storage/emulated" } ?: "Internal Storage"

fun Float.round(decimalPlaces: Int): Float = (this * 10.0.pow(decimalPlaces.toDouble()))
    .roundToInt() / 10.0.pow(decimalPlaces.toDouble()).toFloat()

fun Float.toString(decimalPlaces: Int): String = "%.${decimalPlaces}f".format(this)

val Uri.isExternalStorageDocument: Boolean
    get() = "com.android.externalstorage.documents" == authority

val Uri.isDownloadsDocument: Boolean
    get() = "com.android.providers.downloads.documents" == authority

val Uri.isMediaDocument: Boolean
    get() = "com.android.providers.media.documents" == authority

val Uri.isGooglePhotosUri: Boolean
    get() = "com.google.android.apps.photos.content" == authority

val Uri.isLocalPhotoPickerUri: Boolean
    get() = toString().contains(other = "com.android.providers.media.photopicker")

val Uri.isCloudPhotoPickerUri: Boolean
    get() = toString().contains(other = "com.google.android.apps.photos.cloudpicker")
