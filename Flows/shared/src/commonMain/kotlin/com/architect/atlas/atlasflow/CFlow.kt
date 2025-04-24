package com.architect.atlas.atlasflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class CFlow<T>(private val flow: StateFlow<T>) {
    fun observe(block: (T) -> Unit): DisposableHandle {
        val scope = CoroutineScope(SupervisorJob())
        val job = scope.launch {
            flow.collect {
                block(it)
            }
        }

        return object : DisposableHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }

    fun observeMain(block: (T) -> Unit): DisposableHandle {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        val job = scope.launch {
            flow.collect {
                withContext(Dispatchers.Main.immediate) {
                    block(it)
                }
            }
        }

        return object : DisposableHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }
}

fun <T> StateFlow<T>.asCFlow(): CFlow<T> = CFlow(this)
fun <T> MutableStateFlow<T>.asMutableCFlow(): MutableCFlow<T> = MutableCFlow(this)
fun <T : Any> MutableStateFlow<T>.asSwiftFlow(): AnyKmpObjectFlow {
    return AnyKmpObjectFlow(
        getter = { value },
        setter = { newValue -> value = newValue as T },
        collector = { block -> this.onEach { block(it) }.launchIn(CoroutineScope(Dispatchers.Main)) }
    )
}