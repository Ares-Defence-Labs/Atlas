package com.architect.atlas.architecture.mvvm

import androidx.lifecycle.ViewModel
import com.architect.kmpessentials.lifecycle.KmpLifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@Suppress("EmptyDefaultConstructor")
actual open class ViewModel actual constructor() : ViewModel() {
    actual val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

//    init {
//        KmpLifecycle.setAppLifecycleBackground {
//            onBackground()
//        }
//        KmpLifecycle.setAppLifecycleForeground {
//            onForeground()
//        }
//    }

    public actual override fun onCleared() {
        super.onCleared()

        viewModelScope.cancel()
    }

    actual open fun onAppearing() {

    }

    actual open fun onAppeared() {

    }

    actual open fun onDisappearing() {

    }

    actual open fun onDisappeared() {

    }

    actual open fun onInitialize() {

    }

    actual open fun onBackground() {

    }

    actual open fun onForeground() {

    }
}