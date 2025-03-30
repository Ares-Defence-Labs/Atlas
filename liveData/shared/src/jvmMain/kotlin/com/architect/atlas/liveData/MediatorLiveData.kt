package com.architect.atlas.liveData

actual open class MediatorLiveData<T> actual constructor(initialValue: T) :
    MutableLiveData<T>(initialValue) {

    actual fun <IT> addSource(
        liveData: LiveData<IT>,
        onChange: (newValue: IT) -> Unit
    ) {
        liveData.addObserver { onChange(it) }
    }
}