package com.architect.atlas.liveData.utilities

import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose

fun <T> LiveData<T>.asFlow(): Flow<T> = channelFlow {
    val observer: (T) -> Unit = { trySend(it) }

    addObserver(observer)

    awaitClose { removeObserver(observer) }
}

fun <T> Flow<T>.asLiveData(coroutineScope: CoroutineScope, initialValue: T): LiveData<T> {
    val resultLiveData = MutableLiveData(initialValue)
    coroutineScope.launch {
        runCatching {
            collect {
                resultLiveData.value = it
            }
        }
    }
    return resultLiveData
}

fun <T> StateFlow<T>.asLiveData(coroutineScope: CoroutineScope): LiveData<T> {
    return asLiveData(coroutineScope, value)
}

fun <T : Throwable> LiveData<T>.throwableMessage(
    mapper: (Throwable) -> String = { it.message.orEmpty() }
): LiveData<String> = map(mapper)