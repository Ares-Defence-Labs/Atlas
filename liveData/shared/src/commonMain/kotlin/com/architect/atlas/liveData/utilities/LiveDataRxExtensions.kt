package com.architect.atlas.liveData.utilities


import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
fun <T> LiveData<T>.debounce(
    coroutineScope: CoroutineScope,
    timeInMillis: Long
): LiveData<T> {
    val resultLiveData = MutableLiveData(this.value)

    coroutineScope.launch {
        this@debounce.asFlow()
            .debounce(timeInMillis)
            .collect {
                resultLiveData.value = it
            }
    }

    return resultLiveData
}

@OptIn(FlowPreview::class)
fun <T> LiveData<T>.throttleFirst(
    coroutineScope: CoroutineScope,
    timeInMillis: Long
): LiveData<T> {
    val resultLiveData = MutableLiveData(this.value)
    coroutineScope.launch {
        this@throttleFirst.asFlow()
            .throttleFirst(timeInMillis)
            .collect {
                resultLiveData.value = it
            }
    }

    return resultLiveData
}


@OptIn(FlowPreview::class)
fun <T> LiveData<T>.throttleLast(
    coroutineScope: CoroutineScope,
    timeInMillis: Long
): LiveData<T> {
    val resultLiveData = MutableLiveData(this.value)

    coroutineScope.launch {
        this@throttleLast.asFlow()
            .sample(timeInMillis)
            .collect {
                resultLiveData.value = it
            }
    }

    return resultLiveData
}


@OptIn(FlowPreview::class)
fun <T> LiveData<T>.broadcast(
    coroutineScope: CoroutineScope,
): LiveData<T> {
    val resultLiveData = MutableLiveData(this.value)

    coroutineScope.launch {
        this@broadcast.asFlow()
            .retry()
            .collect {
                resultLiveData.value = it
            }
    }

    return resultLiveData
}

@OptIn(FlowPreview::class)
fun <T> LiveData<T>.retryAttempts(
    coroutineScope: CoroutineScope,
    attempts: Long
): LiveData<T> {
    val resultLiveData = MutableLiveData(this.value)

    coroutineScope.launch {
        this@retryAttempts.asFlow()
            .retry(attempts)
            .collect {
                resultLiveData.value = it
            }
    }

    return resultLiveData
}


