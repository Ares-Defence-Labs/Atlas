package com.architect.atlas.atlasflow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MutableAtlasFlowState<T>(initialValue: T) {
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
    fun asMutableStateFlow(): StateFlow<T> = _state
}

