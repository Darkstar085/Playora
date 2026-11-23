package com.sipun.playora.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipun.playora.R
import com.sipun.playora.core.model.ApplicationPreferences
import com.sipun.playora.core.model.Folder
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.ui.components.AppSegmentedListItem
import com.sipun.playora.core.ui.components.DestructiveRed
import com.sipun.playora.core.ui.theme.PlayoraTheme
import java.util.Locale

@Composable
fun FolderItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    when (preferences.mediaLayoutMode) {
        MediaLayoutMode.LIST -> FolderListItem(
            folder = folder,
            isRecentlyPlayedFolder = isRecentlyPlayedFolder,
            preferences = preferences,
            modifier = modifier,
            isFirstItem = isFirstItem,
            isLastItem = isLastItem,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )

        MediaLayoutMode.GRID -> FolderGridItem(
            folder = folder,
            isRecentlyPlayedFolder = isRecentlyPlayedFolder,
            preferences = preferences,
            modifier = modifier,
            isFirstItem = isFirstItem,
            isLastItem = isLastItem,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@Composable
fun getFolderArt(folderName: String, folderPath: String): ImageVector? {
    val name = folderName.lowercase(Locale.ROOT)
    val path = folderPath.lowercase(Locale.ROOT)

    return when {
        name.contains("camera") || name == "dcim" || path.contains("/dcim") ->
            Icons.Rounded.PhotoCamera

        name.contains("movie") || name.contains("cinema") || name.contains("film") ->
            Icons.Rounded.Movie

        name.contains("whatsapp") || name.contains("wa ") -> {
            if (name.contains("doc")) {
                Icons.Rounded.Description
            } else {
                ImageVector.vectorResource(R.drawable.ic_folder_whatsapp)
            }
        }

        name.contains("download") || name.contains("1dmp") || name.contains("adm") || name.contains(
            "torrent"
        ) ->
            Icons.Rounded.FileDownload

        name.contains("insta") || name.contains("aero") || name.contains("reel") || name.contains("stories") ->
            ImageVector.vectorResource(R.drawable.ic_folder_instagram)

        name.contains("screenshot") || name.contains("screen") || name.contains("record") ->
            Icons.Rounded.Screenshot

        name.contains("telegram") || name.contains("tg ") ->
            Icons.AutoMirrored.Rounded.Send

        name.contains("picture") || name.contains("image") || name.contains("photo") || name.contains(
            "gallery"
        ) ->
            Icons.Rounded.Image

        name.contains("music") || name.contains("audio") || name.contains("song") ->
            Icons.Rounded.MusicNote

        name.contains("bluetooth") ->
            Icons.Rounded.Bluetooth

        name.contains("document") || name.contains("doc") ->
            Icons.Rounded.Description

        name.contains("youtube") || name.contains("vanced") || name.contains("newpipe") || name.contains(
            "short"
        ) ->
            Icons.Rounded.SmartDisplay

        name.contains("private") || name.contains("vault") || name.contains("hidden") || name.contains(
            "lock"
        ) ->
            Icons.Rounded.Lock

        name.contains("anime") || name.contains("animation") ->
            Icons.Rounded.Animation

        name.contains("snapchat") || name.contains("snap") ->
            Icons.Rounded.Face

        name.contains("facebook") || name.contains("messenger") ->
            Icons.Rounded.ChatBubble

        name.contains("reddit") ->
            Icons.Rounded.ChatBubble

        name.contains("tiktok") ->
            Icons.Rounded.SmartDisplay

        name.contains("series") || name.contains("tv") || name.contains("show") ->
            Icons.Rounded.Movie

        name == "video" || name == "videos" || name.contains("video") ->
            Icons.Rounded.SmartDisplay

        else -> null
    }
}

private fun formatFolderSize(size: Long): String {
    val kb = 1024L
    val mb = kb * 1024L
    val gb = mb * 1024L

    return when {
        size < kb -> "$size B"
        size < mb -> "${size / kb} KB"
        size < gb -> "${size / mb} MB"
        else -> {
            val gbVal = size.toDouble() / gb.toDouble()
            if (gbVal >= 10.0 || (gbVal * 10).toLong() % 10 == 0L) {
                "${gbVal.toLong()} GB"
            } else {
                "%.1f GB".format(Locale.US, gbVal)
            }
        }
    }
}

@Composable
fun FolderThumbnail(
    folder: Folder,
    modifier: Modifier = Modifier,
) {
    val artIcon = getFolderArt(folderName = folder.name, folderPath = folder.path)

    Box(
        modifier = modifier,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.folder_thumbnail),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxSize(),
        )

        if (artIcon != null) {
            Icon(
                imageVector = artIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(23.dp)
                    .align(Alignment.Center)
                    .offset(y = 2.dp),
            )
        }

        if (folder.newVideosCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-3).dp)
                    .defaultMinSize(minWidth = 17.dp)
                    .height(17.dp)
                    .clip(CircleShape)
                    .background(DestructiveRed)
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = folder.newVideosCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolderListItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val hasNewVideos = folder.newVideosCount > 0
    val isHighlighted = (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) || hasNewVideos
    val showPath = preferences.showPathField && folder.path.isNotBlank()

    AppSegmentedListItem(
        modifier = modifier,
        selected = selected,
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = if (showPath) 5.dp else 3.dp,
        ),
        colors = ListItemDefaults.segmentedColors(
            containerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
        ),
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        onClick = onClick,
        onLongClick = onLongClick,
        leadingContent = {
            FolderThumbnail(
                folder = folder,
                modifier = Modifier
                    .width(58.dp)
                    .height(43.dp),
            )
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = folder.name,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                    ),
                    color = if (isHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    overflow = TextOverflow.Ellipsis,
                )
                if (showPath) {
                    Text(
                        text = folder.path,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        supportingContent = {
            Row(
                modifier = Modifier.padding(top = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val videosLabel = if (folder.videosCount == 1) {
                    stringResource(R.string.video).lowercase()
                } else {
                    stringResource(R.string.videos).lowercase()
                }
                Text(
                    text = "${folder.videosCount} $videosLabel",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
                if (folder.foldersCount > 0) {
                    val foldersLabel = if (folder.foldersCount == 1) {
                        stringResource(R.string.folder).lowercase()
                    } else {
                        stringResource(R.string.folders).lowercase()
                    }
                    Text(
                        text = "${folder.foldersCount} $foldersLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
                if (folder.totalSize > 0) {
                    Text(
                        text = formatFolderSize(folder.totalSize),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FolderGridItem(
    folder: Folder,
    isRecentlyPlayedFolder: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val hasNewVideos = folder.newVideosCount > 0
    val isHighlighted = (isRecentlyPlayedFolder && preferences.markLastPlayedMedia) || hasNewVideos

    AppSegmentedListItem(
        modifier = modifier.width(IntrinsicSize.Min),
        selected = selected,
        contentPadding = PaddingValues(8.dp),
        colors = ListItemDefaults.segmentedColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
        ),
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        onClick = onClick,
        onLongClick = onLongClick,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FolderThumbnail(
                    folder = folder,
                    modifier = Modifier
                        .width(70.dp)
                        .height(51.dp),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = folder.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        overflow = TextOverflow.Ellipsis,
                        color = if (isHighlighted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textAlign = TextAlign.Center,
                    )
                    val mediaCount = if (folder.videosCount > 0) {
                        val videosLabel = if (folder.videosCount == 1) {
                            stringResource(R.string.video).lowercase()
                        } else {
                            stringResource(R.string.videos).lowercase()
                        }
                        "${folder.videosCount} $videosLabel"
                    } else {
                        null
                    }
                    val folderCount = if (folder.foldersCount > 0) {
                        val foldersLabel = if (folder.foldersCount == 1) {
                            stringResource(R.string.folder).lowercase()
                        } else {
                            stringResource(R.string.folders).lowercase()
                        }
                        "${folder.foldersCount} $foldersLabel"
                    } else {
                        null
                    }

                    Text(
                        text = buildString {
                            mediaCount?.let {
                                append(it)
                                folderCount?.let {
                                    append(", ")
                                    append("\u00A0")
                                }
                            }
                            folderCount?.let { append(it) }
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
fun FolderItemRecentlyPlayedPreview() {
    PlayoraTheme {
        FolderListItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = true,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderItemPreview() {
    PlayoraTheme {
        FolderListItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = false,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderGridViewPreview() {
    PlayoraTheme {
        FolderGridItem(
            folder = Folder.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = true,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderItemWithNewVideosPreview() {
    PlayoraTheme {
        FolderListItem(
            folder = Folder.sample.copy(newVideosCount = 3),
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = false,
        )
    }
}

@PreviewLightDark
@Composable
fun FolderGridItemWithNewVideosOverflowPreview() {
    PlayoraTheme {
        FolderGridItem(
            folder = Folder.sample.copy(newVideosCount = 42),
            preferences = ApplicationPreferences(),
            isRecentlyPlayedFolder = false,
        )
    }
}
