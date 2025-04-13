package com.aresdefencelabs.atlasflow.bindings

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.StateFlow

fun TextView.bindText(
    lifecycleOwner: LifecycleOwner,
    flow: StateFlow<String>
): DisposableHandle {
    return flow.bind(lifecycleOwner.lifecycleScope) { this.text = it }
}


