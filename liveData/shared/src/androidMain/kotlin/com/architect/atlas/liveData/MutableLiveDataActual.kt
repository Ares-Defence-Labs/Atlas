package com.architect.atlas.liveData

import androidx.lifecycle.MutableLiveData

actual open class MutableLiveData<T> : LiveData<T> {
    actual constructor(initialValue: T) : super(MutableLiveData<T>().apply { value = initialValue })
    constructor(mutableLiveData: MutableLiveData<T>) : super(mutableLiveData)

    @Suppress("UNCHECKED_CAST")
    actual override var value: T
        get() = archLiveData.value as T
        set(newValue) {
            (archLiveData as MutableLiveData<T>).value = newValue
        }

    override fun ld(): MutableLiveData<T> = archLiveData as MutableLiveData<T>
}

actual fun <T> com.architect.atlas.liveData.MutableLiveData<T>.postValue(value: T) {
    ld().postValue(value)
}


