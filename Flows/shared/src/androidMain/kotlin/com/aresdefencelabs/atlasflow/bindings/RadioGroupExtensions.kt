package com.aresdefencelabs.atlasflow.bindings

import android.widget.RadioGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun RadioGroup.bindCheckedId(
    lifecycleOwner: LifecycleOwner,
    state: MutableStateFlow<Int>
): DisposableHandle {
    val listener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        if (state.value != checkedId) state.value = checkedId
    }

    setOnCheckedChangeListener(listener)

    val job = state.bind(lifecycleOwner.lifecycleScope) {
        if (checkedRadioButtonId != it) check(it)
    }

    return object : DisposableHandle {
        override fun dispose() {
            setOnCheckedChangeListener(null)
            job.dispose()
        }
    }
}