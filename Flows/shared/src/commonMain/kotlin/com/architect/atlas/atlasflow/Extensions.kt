package com.architect.atlas.atlasflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
fun <T> CFlow<T>.debounce(
    coroutineScope: CoroutineScope,
    timeInMillis: Long
): CFlow<T> {
    val stateFlow = MutableStateFlow(this.flow.value)
    coroutineScope.launch {
        this@debounce.flow
            .debounce(timeInMillis)
            .collect {
                stateFlow.value = it
            }
    }
    return CFlow(stateFlow)
}

@OptIn(FlowPreview::class)
fun <T> CFlow<T>.throttleLast(
    coroutineScope: CoroutineScope,
    timeInMillis: Long
): CFlow<T> {
    val stateFlow = MutableStateFlow(this.flow.value)

    coroutineScope.launch {
        this@throttleLast.flow
            .sample(timeInMillis)
            .collect {
                stateFlow.value = it
            }
    }

    return CFlow(stateFlow)
}

@OptIn(FlowPreview::class)
fun <T> CFlow<T>.broadcast(
    coroutineScope: CoroutineScope
): CFlow<T> {
    val stateFlow = MutableStateFlow(this.flow.value)

    coroutineScope.launch {
        this@broadcast.flow
            .retry()
            .collect {
                stateFlow.value = it
            }
    }

    return CFlow(stateFlow)
}

@OptIn(FlowPreview::class)
fun <T> CFlow<T>.retryAttempts(
    coroutineScope: CoroutineScope,
    attempts: Long
): CFlow<T> {
    val stateFlow = MutableStateFlow(this.flow.value)

    coroutineScope.launch {
        this@retryAttempts.flow
            .retry(attempts)
            .collect {
                stateFlow.value = it
            }
    }

    return CFlow(stateFlow)
}