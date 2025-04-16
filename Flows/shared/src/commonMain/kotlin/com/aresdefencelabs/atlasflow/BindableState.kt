package com.architect.atlas.atlasflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MutableAtlasFlowState<T : Any>(initialValue: T) {
    private val _state = MutableStateFlow(initialValue)
    private val state: StateFlow<T> get() = _state

    var value: T
        get() = _state.value
        set(value) {
            _state.value = value
        }

    fun update(transform: (T) -> T) {
        _state.value = transform(_state.value)
    }

    fun asStateFlow(): StateFlow<T> = state
    fun asMutableStateFlow(): MutableStateFlow<T> = _state
    fun asSwiftFlow(): AnyKmpObjectFlow {
        return asMutableStateFlow().asSwiftFlow()
    }
}

