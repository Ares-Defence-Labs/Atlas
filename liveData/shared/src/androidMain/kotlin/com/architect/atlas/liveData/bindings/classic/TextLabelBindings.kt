package com.architect.atlas.liveData.bindings.classic

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.utils.bindNotNull

fun TextView.bindText(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<String>
): Closeable {
    return liveData.bindNotNull(lifecycleOwner) { this.text = it }
}