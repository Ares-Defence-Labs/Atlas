package com.architect.atlas.liveData

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
    this.value = value
}