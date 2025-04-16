package com.architect.atlas.atlasflow.bindings

import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun CheckBox.bindChecked(
    lifecycleOwner: LifecycleOwner,
    state: MutableStateFlow<Boolean>
): DisposableHandle {
    setOnCheckedChangeListener { _, isChecked ->
        if (state.value != isChecked) {
            state.value = isChecked
        }
    }

    val job = state.bind(lifecycleOwner.lifecycleScope) { isChecked = it }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}

