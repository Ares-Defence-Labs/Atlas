package com.architect.atlas.liveData

actual open class LiveData<T>(initialValue: T) {
    private var storedValue: T = initialValue
    private val observers = mutableListOf<(T) -> Unit>()

    actual open val value: T
        get() = storedValue

    actual open fun addObserver(observer: (T) -> Unit) {
        observer(value)
        observers.add(observer)
    }

    actual open fun removeObserver(observer: (T) -> Unit) {
        observers.remove(observer)
    }

    protected fun changeValue(value: T) {
        storedValue = value

        observers.forEach { it(storedValue) }
    }
}