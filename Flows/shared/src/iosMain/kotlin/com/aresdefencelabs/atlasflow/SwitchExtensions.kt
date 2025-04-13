package com.aresdefencelabs.atlasflow

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.stringWithUTF8String
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject

@OptIn(ExperimentalForeignApi::class)
fun String.toNSString() = NSString.stringWithUTF8String(null)

@OptIn(ExperimentalForeignApi::class)
fun UISwitch.bindChecked(
    scope: CoroutineScope,
    state: MutableStateFlow<Boolean>
): DisposableHandle {
    val job = scope.launch {
        state.collect { value ->
            dispatch_async(dispatch_get_main_queue()) {
                if (this@bindChecked.on != value)
                    this@bindChecked.setOn(value, animated = true)
            }
        }
    }

    // Create an Objective-C compatible observer
    class SwitchObserver(val uiSwitch: UISwitch, val flow: MutableStateFlow<Boolean>) : NSObject() {
        @ObjCAction
        fun switchChanged() {
            val newValue = uiSwitch.on
            if (flow.value != newValue) {
                flow.value = newValue
            }
        }
    }

    val observer = SwitchObserver(this, state)

    // Store reference for cleanup
    objc_setAssociatedObject(
        `object` = this,
        key = StableRef.create(NSObject()).asCPointer(),
        value = observer,
        policy = OBJC_ASSOCIATION_RETAIN_NONATOMIC
    )

    this.addTarget(
        target = observer,
        action = NSSelectorFromString("switchChanged"),
        forControlEvents = UIControlEventValueChanged
    )

    return object : DisposableHandle {
        override fun dispose() {
            job.cancel()
            this@bindChecked.removeTarget(
                target = observer,
                action = NSSelectorFromString("switchChanged"),
                forControlEvents = UIControlEventValueChanged
            )
        }
    }
}