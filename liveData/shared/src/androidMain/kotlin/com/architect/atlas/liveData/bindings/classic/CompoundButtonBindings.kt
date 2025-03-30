package com.architect.atlas.liveData.bindings.classic

import android.widget.CompoundButton
import androidx.lifecycle.LifecycleOwner
import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import com.architect.atlas.liveData.MutableLiveData
import com.architect.atlas.liveData.utils.bindNotNull

fun CompoundButton.bindChecked(
    lifecycleOwner: LifecycleOwner,
    liveData: LiveData<Boolean>
): Closeable {
    return liveData.bindNotNull(lifecycleOwner) { this.isChecked = it }
}

fun CompoundButton.bindCheckedTwoWay(
    lifecycleOwner: LifecycleOwner,
    liveData: MutableLiveData<Boolean>
): Closeable {
    val readCloseable = liveData.bindNotNull(lifecycleOwner) { value ->
        if (this.isChecked == value) return@bindNotNull

        this.isChecked = value
    }

    val checkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (liveData.value == isChecked) return@OnCheckedChangeListener

        liveData.value = isChecked
    }
    setOnCheckedChangeListener(checkListener)

    val writeCloseable = Closeable {
        setOnCheckedChangeListener(null)
    }

    return readCloseable + writeCloseable
}



