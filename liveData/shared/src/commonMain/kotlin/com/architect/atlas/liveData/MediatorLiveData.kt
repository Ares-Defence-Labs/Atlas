package com.architect.atlas.liveData

expect open class MediatorLiveData<T>(initialValue: T) : MutableLiveData<T> {
    fun <IT> addSource(
        liveData: LiveData<IT>,
        onChange: (newValue: IT) -> Unit
    )
}