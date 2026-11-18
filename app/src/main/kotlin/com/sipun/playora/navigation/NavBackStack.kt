package com.sipun.playora.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

internal fun <T : NavKey> NavBackStack<T>.ensureRoot(root: T) {
    if (isEmpty()) add(element = root)
}

internal fun <T : NavKey> NavBackStack<T>.removeLastIfNotRoot(): T? =
    if (size > 1) removeAt(index = lastIndex) else null
