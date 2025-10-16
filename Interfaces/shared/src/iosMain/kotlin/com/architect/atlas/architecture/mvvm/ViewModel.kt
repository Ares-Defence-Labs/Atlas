package com.architect.atlas.architecture.mvvm

import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.posix.abort
import platform.posix.usleep

private val CrashlyticsFatalHandler = CoroutineExceptionHandler { _, t ->
    runCatching {
        CrashlyticsKotlin.sendFatalException(t)
    }
    usleep(300_000u)
    abort()
}

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() {
    actual val viewModelScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO + CrashlyticsFatalHandler)
    actual val viewModelScopeWithoutCancel: CoroutineScope =
        CoroutineScope(Dispatchers.IO + CrashlyticsFatalHandler)

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