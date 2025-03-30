package com.architect.atlas.liveData

import androidx.lifecycle.Observer
import androidx.lifecycle.LiveData as ArchLiveData

actual open class LiveData<T>(liveData: ArchLiveData<T>) {
    protected val archLiveData: ArchLiveData<T> = liveData
    private val observers = mutableMapOf<(T) -> Unit, Observer<T>>()

    @Suppress("UNCHECKED_CAST")
    actual open val value: T
        get() = archLiveData.value as T

    actual open fun addObserver(observer: (T) -> Unit) {
        val archObserver = Observer<T> { value ->
            observer(value)
        }
        observers[observer] = archObserver

        archLiveData.observeForever(archObserver)
    }

    actual open fun removeObserver(observer: (T) -> Unit) {
        val archObserver = observers.remove(observer) ?: return
        archLiveData.removeObserver(archObserver)
    }

    open fun ld(): ArchLiveData<T> = archLiveData
}