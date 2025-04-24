package com.architect.atlas.atlasflow.swiftUI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DoubleFlow(private val stateFlow: MutableStateFlow<Double>) {
    fun getValue(): Double = stateFlow.value
    fun setValue(value: Double) {
        stateFlow.value = value
    }

    fun observe(block: (Double) -> Unit): DisposableHandle {
        val scope = CoroutineScope(SupervisorJob())
        val job = scope.launch {
            stateFlow.collect {
                block(it)
            }
        }

        return object : DisposableHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }

    fun observeMain(block: (Double) -> Unit): DisposableHandle {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        val job = scope.launch {
            stateFlow.collect {
                block(it)
            }
        }

        return object : DisposableHandle {
            override fun dispose() {
                job.cancel()
            }
        }
    }
}