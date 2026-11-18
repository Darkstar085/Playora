package com.sipun.playora.feature.videopicker.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sipun.playora.R
import com.sipun.playora.core.model.MediaLayoutMode
import com.sipun.playora.core.model.MediaViewMode
import com.sipun.playora.core.model.Sort

@Composable
fun MediaLayoutMode.name(): String = when (this) {
    MediaLayoutMode.LIST -> stringResource(id = R.string.list)
    MediaLayoutMode.GRID -> stringResource(id = R.string.grid)
}

@Composable
fun MediaViewMode.name(): String = when (this) {
    MediaViewMode.VIDEOS -> stringResource(id = R.string.videos)
    MediaViewMode.FOLDERS -> stringResource(id = R.string.folders)
    MediaViewMode.FOLDER_TREE -> stringResource(id = R.string.tree)
}

@Composable
fun Sort.Order.name(sortBy: Sort.By): String {
    val stringRes = when (sortBy) {
        Sort.By.TITLE,
        Sort.By.PATH -> when (this) {
            Sort.Order.ASCENDING -> R.string.a_z
            Sort.Order.DESCENDING -> R.string.z_a
        }

        Sort.By.LENGTH -> when (this) {
            Sort.Order.ASCENDING -> R.string.shortest
            Sort.Order.DESCENDING -> R.string.longest
        }

        Sort.By.SIZE -> when (this) {
            Sort.Order.ASCENDING -> R.string.smallest
            Sort.Order.DESCENDING -> R.string.largest
        }

        Sort.By.DATE -> when (this) {
            Sort.Order.ASCENDING -> R.string.oldest
            Sort.Order.DESCENDING -> R.string.newest
        }
    }
    return stringResource(id = stringRes)
}
