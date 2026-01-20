package com.architect.atlas.atlasflow.bindings

import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun CheckBox.bindChecked(
    lifecycleOwner: LifecycleOwner,
    state: MutableAtlasFlowState<Boolean>
): DisposableHandle {
    setOnCheckedChangeListener { _, isChecked ->
        if (state.getCurrentValue() != isChecked) {
            state.postValueOnMainThread(isChecked)
        }
    }

    val job = state.asStateFlow().bind(lifecycleOwner.lifecycleScope) { isChecked = it }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}

