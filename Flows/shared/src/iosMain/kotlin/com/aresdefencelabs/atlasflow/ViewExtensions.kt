package com.aresdefencelabs.atlasflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

fun UIView.bindVisible(
    scope: CoroutineScope,
    flow: StateFlow<Boolean>
): DisposableHandle {
    val job = scope.launch {
        flow.collect { isVisible ->
            dispatch_async(dispatch_get_main_queue()) {
                this@bindVisible.hidden = !isVisible
            }
        }
    }

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}

fun UIView.bindGone(
    scope: CoroutineScope,
    flow: StateFlow<Boolean>
): DisposableHandle {
    val job = scope.launch {
        flow.collect { visible ->
            dispatch_async(dispatch_get_main_queue()) {
                this@bindGone.hidden = !visible
                this@bindGone.alpha = if (visible) 1.0 else 0.0
            }
        }
    }

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}
