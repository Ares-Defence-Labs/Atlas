package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.CoroutineScope

@Suppress("EmptyDefaultConstructor")
expect open class ViewModel() {
    val viewModelScope: CoroutineScope

    open fun onCleared()

    open fun onAppearing()
    open fun onAppeared()
    open fun onDisappearing()
    open fun onDisappeared()
    open fun onInitialize()

    // used mostly for mobile platforms
    open fun onBackground()
    open fun onForeground()
}