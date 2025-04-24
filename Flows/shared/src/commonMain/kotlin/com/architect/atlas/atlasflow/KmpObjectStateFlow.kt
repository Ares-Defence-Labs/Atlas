package com.architect.atlas.atlasflow

import kotlinx.coroutines.*

class AnyKmpObjectFlow(
    private val getter: () -> Any,
    private val setter: ((Any) -> Unit)?,
    private val collector: (suspend (Any) -> Unit) -> Job
) {
    fun getValue(): Any = getter()

    fun setValue(value: Any) {
        setter?.invoke(value)
    }

    fun observe(block: (Any) -> Unit): DisposableHandle {
        val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        val job = collector {
            block(it)
        }
        return object : DisposableHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }
}