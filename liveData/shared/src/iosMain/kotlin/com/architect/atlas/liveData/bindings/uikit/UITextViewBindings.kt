package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MutableLiveData
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UITextView
import platform.UIKit.UITextViewTextDidChangeNotification
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalNativeApi::class)
fun <T : String?> UITextView.bindTextTwoWay(
    liveData: MutableLiveData<T>
): Closeable {
    val liveDataObserver: (T) -> Unit = { value ->
        if (this.text != value) {
            this.text = value.orEmpty()
        }
    }

    liveData.addObserver(liveDataObserver)

    val textViewRef = WeakReference(this)
    val notificationCenter = NSNotificationCenter.defaultCenter

    val notificationName = UITextViewTextDidChangeNotification
    notificationCenter.addObserverForName(
        name = notificationName,
        `object` = this,
        queue = null
    ) { notification ->
        textViewRef.get()?.let { textView ->
            @Suppress("UNCHECKED_CAST")
            liveData.value = textView.text as T
        }
    }.also { token ->

        return object : Closeable {
            override fun close() {
                liveData.removeObserver(liveDataObserver)
                notificationCenter.removeObserver(token)
            }
        }
    }
}

fun <T : String?> UITextView.bindTextOneWay(
    liveData: LiveData<T>
): Closeable {
    val observer: (T) -> Unit = { value ->
        this.text = value.orEmpty()
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}