package com.architect.atlas.liveData

expect open class MutableLiveData<T>(initialValue: T) : LiveData<T> {
    override var value: T
}

expect fun <T> MutableLiveData<T>.postValue(value: T)

fun <T> MutableLiveData<T>.setValue(value: T, async: Boolean) {
    if (async) {
        this.postValue(value)
    } else {
        this.value = value
    }
}