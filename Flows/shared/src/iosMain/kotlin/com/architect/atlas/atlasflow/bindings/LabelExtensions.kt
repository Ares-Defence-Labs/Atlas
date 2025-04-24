package com.architect.atlas.atlasflow.bindings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.UIKit.UILabel
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

fun UILabel.bindText(
    scope: CoroutineScope,
    flow: StateFlow<String>
): DisposableHandle {
    val job = scope.launch {
        flow.collect { value ->
            dispatch_async(dispatch_get_main_queue()) {
                this@bindText.text = value
            }
        }
    }

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}