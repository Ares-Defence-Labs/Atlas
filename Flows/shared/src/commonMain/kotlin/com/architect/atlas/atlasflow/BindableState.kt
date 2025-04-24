package com.architect.atlas.atlasflow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MutableAtlasFlowState<T : Any>(initialValue: T) {
    private val _state = MutableStateFlow(initialValue)
    private val state: StateFlow<T> get() = _state

    private var value: T
        get() = _state.value
        set(value) {
            _state.value = value
        }

    fun getCurrentValue(): T {
        return _state.value
    }

    fun setValueOnContext(value: T) {
        _state.value = value
    }

    fun postValueOnMainThread(value: T) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                _state.value = value
            }
        }
    }

    fun asStateFlow(): StateFlow<T> = state
    fun asMutableStateFlow(): MutableStateFlow<T> = _state
    fun asSwiftFlow(): AnyKmpObjectFlow {
        return asMutableStateFlow().asSwiftFlow()
    }
}

