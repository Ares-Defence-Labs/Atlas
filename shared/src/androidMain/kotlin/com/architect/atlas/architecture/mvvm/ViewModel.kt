package com.architect.atlas.architecture.mvvm

import androidx.lifecycle.ViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() : ViewModel() {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    public actual override fun onCleared() {
        super.onCleared()

        viewModelScope.cancel()
    }
}