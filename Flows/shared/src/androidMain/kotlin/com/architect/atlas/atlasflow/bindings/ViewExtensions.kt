package com.architect.atlas.atlasflow.bindings

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle

fun View.bindVisibility(
    lifecycleOwner: LifecycleOwner,
    flow: MutableAtlasFlowState<Boolean>
): DisposableHandle {
    return flow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { visible ->
        visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }
}

fun View.bindGone(
    lifecycleOwner: LifecycleOwner,
    flow: MutableAtlasFlowState<Boolean>
): DisposableHandle {
    return flow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { visible ->
        visibility = if (visible) View.VISIBLE else View.GONE
    }
}

fun <T> View.bindVisibility(
    lifecycleOwner: LifecycleOwner,
    flow: MutableAtlasFlowState<T>,
    isVisible: (T) -> Boolean
): DisposableHandle {
    return flow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { value ->
        visibility = if (isVisible(value)) View.VISIBLE else View.GONE
    }
}