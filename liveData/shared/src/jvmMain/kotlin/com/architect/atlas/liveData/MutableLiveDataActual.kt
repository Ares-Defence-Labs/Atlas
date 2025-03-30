package com.architect.atlas.liveData

import java.util.concurrent.Executor
import java.util.concurrent.Executors

private val mainExecutor: Executor = Executors.newSingleThreadExecutor()

actual open class MutableLiveData<T> actual constructor(
    initialValue: T
) : LiveData<T>(initialValue) {

    actual override var value: T
        get() = super.value
        set(newValue) {
            changeValue(newValue)
        }
}

actual fun <T> MutableLiveData<T>.postValue(value: T) {
    mainExecutor.execute {
        this.value = value
    }
}