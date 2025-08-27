package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope as VM

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel : androidx.lifecycle.ViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = VM

    actual val viewModelScopeWithoutCancel: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        viewModelScope.launch { withContext(Dispatchers.Default) { onInitialize() } }
        viewModelScopeWithoutCancel.launch { withContext(Dispatchers.Default) { onInitializeWithoutCancel() } }
    }

    public actual override fun onCleared() {
        super.onCleared()
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