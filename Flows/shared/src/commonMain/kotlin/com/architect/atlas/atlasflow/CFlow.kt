package com.architect.atlas.atlasflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class CFlow<T>(open val flow: StateFlow<T>) {
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

    fun subscribe(
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
        onCollect: (T) -> Unit
    ): DisposableHandle {
        val job: Job = coroutineScope.launch(dispatcher) {
            flow.onEach { onCollect(it) }.collect()
        }
        return DisposableHandle {
            job.cancel()
        }
    }

    fun subscribe(onCollect: (T) -> Unit): DisposableHandle {
        @Suppress("OPT_IN_USAGE")
        return subscribe(
            coroutineScope = GlobalScope,
            dispatcher = Dispatchers.Main,
            onCollect = onCollect
        )
    }
}

fun <T> StateFlow<T>.asCFlow(): CFlow<T> = CFlow(this)
fun <T> StateFlow<T>.asCStateFlow(): StateCFlow<T> = StateCFlow(this)
fun <T> MutableStateFlow<T>.asMutableCFlow(): MutableCFlow<T> = MutableCFlow(this)
fun <T : Any> MutableStateFlow<T>.asSwiftFlow(): AnyKmpObjectFlow {
    return AnyKmpObjectFlow(
        getter = { value },
        setter = { newValue -> value = newValue as T },
        collector = { block ->
            this.onEach { block(it) }.launchIn(CoroutineScope(Dispatchers.Main))
        }
    )
}

