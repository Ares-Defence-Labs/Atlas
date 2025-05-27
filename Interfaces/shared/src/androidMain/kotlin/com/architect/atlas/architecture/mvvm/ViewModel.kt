package com.architect.atlas.architecture.mvvm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() : ViewModel() {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                onInitialize()
            }
        }
    }

    public actual override fun onCleared() {
        super.onCleared()

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
}