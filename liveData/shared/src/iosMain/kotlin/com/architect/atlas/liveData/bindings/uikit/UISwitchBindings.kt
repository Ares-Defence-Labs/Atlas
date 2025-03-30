package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MutableLiveData
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch

fun UISwitch.bindSwitchOn(
    liveData: LiveData<Boolean>,
    animated: Boolean = true
): Closeable {
    val observer: (Boolean) -> Unit = { value ->
        this.setOn(value, animated)
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}

fun UISwitch.bindSwitchOnTwoWay(
    liveData: MutableLiveData<Boolean>,
    animated: Boolean = true
): Closeable {
    val readCloseable = bindSwitchOn(liveData, animated)
    val writeCloseable = setEventHandler(UIControlEventValueChanged) {
        if (liveData.value == on) return@setEventHandler

        liveData.value = on
    }

    return readCloseable + writeCloseable
}