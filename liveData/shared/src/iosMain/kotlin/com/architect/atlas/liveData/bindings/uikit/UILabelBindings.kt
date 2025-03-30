package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import platform.UIKit.UILabel

fun UILabel.bindText(
    liveData: LiveData<String>
): Closeable {
    val observer: (String) -> Unit = { value ->
        this.text = value
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}