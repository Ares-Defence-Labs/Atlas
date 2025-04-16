package com.architect.atlas.atlasflow.bindings

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UITextField
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
fun UITextField.bindTwoWayText(
    scope: CoroutineScope,
    state: MutableStateFlow<String>
): DisposableHandle {
    val job = scope.launch {
        state.collect { value ->
            dispatch_async(dispatch_get_main_queue()) {
                if (this@bindTwoWayText.text != value)
                    this@bindTwoWayText.text = value
            }
        }
    }

    val observer = object : NSObject() {
        @ObjCAction
        fun textFieldDidChange() {
            val newValue = this@bindTwoWayText.text ?: ""
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
        action = NSSelectorFromString("textFieldObserver"),
        forControlEvents = UIControlEventEditingChanged
    )

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
        }
    }
}