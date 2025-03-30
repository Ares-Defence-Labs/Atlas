package com.architect.atlas.liveData

import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_current_queue
import platform.darwin.dispatch_get_main_queue

actual open class MutableLiveData<T> actual constructor(initialValue: T) : LiveData<T>(initialValue) {
    actual override var value: T
        get() = super.value
        set(newValue) {
            changeValue(newValue)
        }
}

actual fun <T> MutableLiveData<T>.postValue(value: T) {
    val mainQueue = dispatch_get_main_queue()
    if (dispatch_get_current_queue() == mainQueue) {
        this.value = value
    } else {
        dispatch_async(mainQueue) { this.value = value }
    }
}