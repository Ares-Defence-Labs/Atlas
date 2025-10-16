package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.CoroutineScope

@Suppress("EmptyDefaultConstructor")
expect open class ViewModel() {
    val viewModelScope: CoroutineScope
    val viewModelScopeWithoutCancel: CoroutineScope

    open fun onCleared()

    open fun onAppearing()
    open fun onDisappearing()
    open suspend fun onInitialize()
    open suspend fun onInitializeWithoutCancel()
    open fun onDestroy()

    // used mostly for mobile platforms
    open fun onBackground()
    open fun onForeground()
}

