package com.architect.atlas.atlasflow.bindings

import android.widget.CompoundButton
import android.widget.Switch
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun Switch.bindChecked(
    lifecycleOwner: LifecycleOwner,
    state: MutableStateFlow<Boolean>
): DisposableHandle {
    val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (state.value != isChecked) state.value = isChecked
    }

    setOnCheckedChangeListener(listener)

    val job = state.bind(lifecycleOwner.lifecycleScope) {
        if (isChecked != it) isChecked = it
    }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}

