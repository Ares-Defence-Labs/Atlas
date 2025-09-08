package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    actual val viewModelScopeWithoutCancel: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            onInitialize()
        }

        viewModelScopeWithoutCancel.launch {
            onInitializeWithoutCancel()
        }
    }

    actual open fun onCleared() {
        viewModelScope.cancel()
    }

    actual open fun onAppearing() {

    }

    actual open fun onDisappearing() {

    }

    actual open suspend fun onInitialize() {

    }

    actual open fun onDestroy() {

    }

    actual open fun onBackground() {
    }

    actual open fun onForeground() {
    }

    actual open suspend fun onInitializeWithoutCancel() {
    }
}