package com.architect.atlas.atlasflow.bindings

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle

fun TextView.bindText(
    lifecycleOwner: LifecycleOwner,
    flow: MutableAtlasFlowState<String>
): DisposableHandle {
    return flow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { this.text = it }
}


