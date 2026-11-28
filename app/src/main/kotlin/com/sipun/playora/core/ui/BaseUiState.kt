package com.sipun.playora.core.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

sealed class DataState<out T> {
    data object Loading : DataState<Nothing>()
    data class Success<T>(val value: T) : DataState<T>()
    data class Error(val value: Throwable) : DataState<Nothing>()

    val result: T? get() = (this as? Success)?.value
    val error: Throwable? get() = (this as? Error)?.value
}

abstract class MviViewModel<State, Action> : ViewModel() {
    abstract val state: StateFlow<State>
    abstract fun onAction(action: Action)
}
