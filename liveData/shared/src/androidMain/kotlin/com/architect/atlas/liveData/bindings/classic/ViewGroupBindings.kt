package com.architect.atlas.liveData.bindings.classic

import android.view.ViewGroup
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.utils.bindNotNull

fun ViewGroup.bindVisibleOrGone(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<Boolean>
): Closeable {
    return liveData.bindNotNull(lifecycleOwner) { value ->
        this.visibility = if (value) View.VISIBLE else View.GONE
    }
}

fun ViewGroup.bindVisibleOrInvisible(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<Boolean>
): Closeable {
    return liveData.bindNotNull(lifecycleOwner) { value ->
        this.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }
}

fun ViewGroup.bindEnabled(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<Boolean>
): Closeable {
    return liveData.bindNotNull(lifecycleOwner) { this.isEnabled = it }
}