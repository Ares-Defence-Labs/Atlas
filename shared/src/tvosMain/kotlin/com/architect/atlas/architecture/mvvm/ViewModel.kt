package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    actual open fun onCleared() {
        viewModelScope.cancel()
    }
}