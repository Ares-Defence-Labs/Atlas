package com.architect.atlas.atlasflow.bindings

import android.widget.CompoundButton
import android.widget.Switch
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle

fun Switch.bindChecked(
    lifecycleOwner: LifecycleOwner,
    state: MutableAtlasFlowState<Boolean>
): DisposableHandle {
    val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (state.getCurrentValue() != isChecked) state.postValueOnMainThread(isChecked)
    }

    setOnCheckedChangeListener(listener)

    val job = state.asStateFlow().bind(lifecycleOwner.lifecycleScope) {
        if (isChecked != it) isChecked = it
    }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}

