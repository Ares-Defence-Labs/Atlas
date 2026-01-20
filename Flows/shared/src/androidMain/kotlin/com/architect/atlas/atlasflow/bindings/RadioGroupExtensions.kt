package com.architect.atlas.atlasflow.bindings

import android.widget.RadioGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle

fun RadioGroup.bindCheckedId(
    lifecycleOwner: LifecycleOwner,
    state: MutableAtlasFlowState<Int>
): DisposableHandle {
    val listener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        if (state.getCurrentValue() != checkedId) state.postValueOnMainThread(checkedId)
    }

    setOnCheckedChangeListener(listener)

    val job = state.asStateFlow().bind(lifecycleOwner.lifecycleScope) {
        if (checkedRadioButtonId != it) check(it)
    }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}