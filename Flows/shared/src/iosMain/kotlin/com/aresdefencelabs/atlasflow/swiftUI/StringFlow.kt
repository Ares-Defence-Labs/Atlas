package com.aresdefencelabs.atlasflow.swiftUI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StringFlow(private val stateFlow: MutableStateFlow<String>) {
    fun getValue(): String = stateFlow.value
    fun setValue(value: String) {
        stateFlow.value = value
    }

    fun observe(block: (String) -> Unit): DisposableHandle {
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

    fun observeMain(block: (String) -> Unit): DisposableHandle {
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


