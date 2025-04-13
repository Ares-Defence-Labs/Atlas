package com.aresdefencelabs.atlasflow

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.UIKit.UISlider
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControlEventValueChanged
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
fun UISlider.bindSlider(
    scope: CoroutineScope,
    state: MutableStateFlow<Float>
): DisposableHandle {
    val job = scope.launch {
        state.collect { value ->
            dispatch_async(dispatch_get_main_queue()) {
                if (this@bindSlider.value != value)
                    this@bindSlider.setValue(value, animated = true)
            }
        }
    }

    val observer = object : NSObject() {
        @ObjCAction
        fun sliderChanged() {
            val newValue = this@bindSlider.value
            if (state.value != newValue) {
                state.value = newValue
            }
        }
    }

    objc_setAssociatedObject(
        `object` = this,
        key = StableRef.create(NSObject()).asCPointer(),
        value = observer,
        policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
    )

    this.addTarget(
        target = observer,
        action = NSSelectorFromString("sliderChanged"),
        forControlEvents = UIControlEventValueChanged
    )


    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}