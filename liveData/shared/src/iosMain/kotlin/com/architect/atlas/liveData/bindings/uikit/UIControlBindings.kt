package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControl
import platform.UIKit.UIControlEvents
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
fun UIControl.setEventHandler(
    controlEvents: UIControlEvents,
    handler: UIControl.() -> Unit
): Closeable {
    val target = UIControlTarget(this, handler)
    this.addTarget(target, NSSelectorFromString("invoke"), controlEvents)

    return object : Closeable {
        override fun close() {
            this@setEventHandler.removeTarget(target, NSSelectorFromString("invoke"), controlEvents)
        }
    }
}

private class UIControlTarget(
    val control: UIControl,
    val block: UIControl.() -> Unit
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun invoke() {
        control.block()
    }
}