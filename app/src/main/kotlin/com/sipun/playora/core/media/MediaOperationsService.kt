package com.sipun.playora.core.media.services

import android.net.Uri
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MediaOperationsService {
    fun initialize(activity: ComponentActivity)
    suspend fun deleteMedia(uris: List<Uri>, permanently: Boolean = false): Boolean
    suspend fun restoreMedia(uris: List<Uri>): Boolean
    suspend fun renameMedia(uri: Uri, to: String): Boolean
    suspend fun shareMedia(uris: List<Uri>)
    suspend fun moveMedia(targets: Map<Uri, File>): Map<Uri, File?>

    fun transferMedia(uris: List<Uri>, folderUri: Uri, mode: TransferMode): Flow<TransferEvent>

    companion object {
        fun supportsTrash(): Boolean = true
    }
}

enum class TransferMode { COPY, MOVE }

sealed interface TransferEvent {
    data class Progress(val progress: TransferProgress) : TransferEvent
    data class Completed(val result: TransferResult) : TransferEvent
}

data class TransferProgress(
    val totalFiles: Int,
    val currentIndex: Int = 0,
    val currentName: String? = null,
    val currentBytesCopied: Long = 0,
    val currentBytesTotal: Long = 0,
    val overallBytesCopied: Long = 0,
    val overallBytesTotal: Long = 0,
) {
    val currentFraction: Float?
        get() = if (currentBytesTotal > 0) {
            (currentBytesCopied.toFloat() / currentBytesTotal).coerceIn(
                minimumValue = 0f,
                maximumValue = 1f,
            )
        } else {
            null
        }

    val overallFraction: Float
        get() = if (overallBytesTotal > 0) {
            (overallBytesCopied.toFloat() / overallBytesTotal).coerceIn(
                minimumValue = 0f,
                maximumValue = 1f,
            )
        } else if (totalFiles > 0) {
            (currentIndex.toFloat() / totalFiles).coerceIn(
                minimumValue = 0f,
                maximumValue = 1f,
            )
        } else {
            0f
        }
}

data class TransferResult(
    val succeeded: Int,
    val failed: Int,
    val sameFolderSkipped: Int = 0,
    val originalsNotDeleted: Boolean = false,
)
