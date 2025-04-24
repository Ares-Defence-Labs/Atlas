package com.architect.atlas.atlasflow.swiftUI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class IntFlow(private val stateFlow: MutableStateFlow<Int>) {
    fun getValue(): Int = stateFlow.value
    fun setValue(value: Int) {
        stateFlow.value = value
    }

    fun observe(block: (Int) -> Unit): DisposableHandle {
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

    fun observeMain(block: (Int) -> Unit): DisposableHandle {
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

