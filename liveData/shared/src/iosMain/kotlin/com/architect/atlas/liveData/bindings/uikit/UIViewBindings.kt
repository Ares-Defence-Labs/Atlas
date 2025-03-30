package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import platform.UIKit.UIColor
import platform.UIKit.UIView

fun UIView.bindBackgroundColor(
    liveData: LiveData<Boolean>,
    trueColor: UIColor,
    falseColor: UIColor
): Closeable {
    val observer: (Boolean) -> Unit = { value ->
        val color = when (value) {
            true -> trueColor
            false -> falseColor
        }

        backgroundColor = color
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}

fun UIView.bindHidden(
    liveData: LiveData<Boolean>
): Closeable {
    val observer: (Boolean) -> Unit = { value ->
        setHidden(value)
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}

fun UIView.bindFocus(
    liveData: LiveData<Boolean>
): Closeable {
    val observer: (Boolean) -> Unit = { value ->
        when (value) {
            true -> becomeFirstResponder()
            else -> {
                if (nextResponder?.canBecomeFirstResponder == true) {
                    nextResponder?.becomeFirstResponder()
                } else {
                    resignFirstResponder()
                }
            }
        }
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}

