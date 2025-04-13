package com.aresdefencelabs.atlasflow.bindings

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.StateFlow

fun View.bindVisibility(
    lifecycleOwner: LifecycleOwner,
    flow: StateFlow<Boolean>
): DisposableHandle {
    return flow.bind(lifecycleOwner.lifecycleScope) { visible ->
        visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }
}

fun View.bindGone(
    lifecycleOwner: LifecycleOwner,
    flow: StateFlow<Boolean>
): DisposableHandle {
    return flow.bind(lifecycleOwner.lifecycleScope) { visible ->
        visibility = if (visible) View.VISIBLE else View.GONE
    }
}

fun <T> View.bindVisibility(
    lifecycleOwner: LifecycleOwner,
    flow: StateFlow<T>,
    isVisible: (T) -> Boolean
): DisposableHandle {
    return flow.bind(lifecycleOwner.lifecycleScope) { value ->
        visibility = if (isVisible(value)) View.VISIBLE else View.GONE
    }
}