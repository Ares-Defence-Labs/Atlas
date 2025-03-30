package com.architect.atlas.liveData

expect open class LiveData<T> {
    open val value: T
    open fun addObserver(observer: (T) -> Unit)
    open fun removeObserver(observer: (T) -> Unit)
}

