package com.architect.atlas.atlasflow.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.interfaces.Closeable
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun <T : Any> MutableAtlasFlowState<T>.bind(
    lifecycleOwner: LifecycleOwner,
    observer: (T?) -> Unit
): Closeable {
    val job: Job = lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            asStateFlow().collect { value ->
                observer(value)
            }
        }
    }

    return Closeable {
        job.cancel()
    }
}

fun <T : Any> MutableAtlasFlowState<T>.bindNotNull(
    lifecycleOwner: LifecycleOwner,
    observer: (T) -> Unit
): Closeable {
    return bind(lifecycleOwner) { value ->
        if (value == null) return@bind
        observer(value)
    }
}