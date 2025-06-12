package com.architect.atlas.atlasflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <T> StateFlow<T>.bind(
    coroutineScope: CoroutineScope,
    block: (T) -> Unit
): DisposableHandle {
    val job = coroutineScope.launch {
        this@bind.collect { value ->
            withContext(Dispatchers.Main.immediate) {
                block(value)
            }
        }
    }
    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}

fun <T> MutableStateFlow<T>.bindTwoWay(
    coroutineScope: CoroutineScope,
    onUiChange: (T) -> Unit
): DisposableHandle {
    val job = coroutineScope.launch {
        this@bindTwoWay.collect { value ->
            withContext(Dispatchers.Main.immediate) {
                onUiChange(value)
            }
        }
    }

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }

        fun updateFromUI(newValue: T) {
            if (this@bindTwoWay.value != newValue) {
                this@bindTwoWay.value = newValue
            }
        }
    }
}


