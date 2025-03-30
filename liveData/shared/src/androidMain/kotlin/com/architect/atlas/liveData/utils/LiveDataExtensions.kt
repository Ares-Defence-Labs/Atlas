package com.architect.atlas.liveData.utils

import com.architect.atlas.liveData.LiveData
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.architect.atlas.liveData.Closeable

fun <T> LiveData<T>.bind(lifecycleOwner: LifecycleOwner, observer: (T?) -> Unit): Closeable {
    observer(value)

    val androidObserver = Observer<T> { value ->
        observer(value)
    }
    val androidLiveData = this.ld()

    androidLiveData.observe(lifecycleOwner, androidObserver)

    return Closeable {
        androidLiveData.removeObserver(androidObserver)
    }
}

fun <T> LiveData<T>.bindNotNull(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit): Closeable {
    return bind(lifecycleOwner) { value ->
        if (value == null) return@bind

        observer(value)
    }
}