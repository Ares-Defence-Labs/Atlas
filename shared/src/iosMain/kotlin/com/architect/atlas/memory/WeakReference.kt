package com.architect.atlas.memory

import kotlinx.cinterop.ObjCObject
import kotlin.experimental.ExperimentalNativeApi
import kotlin.reflect.KClass
import kotlin.native.ref.WeakReference as NativeWeakReference

actual class WeakReference<T : Any> actual constructor(value: T?) {
    @OptIn(ExperimentalNativeApi::class)
    private var weakRef: NativeWeakReference<T>? = value?.let { NativeWeakReference(it) }

    @OptIn(ExperimentalNativeApi::class)
    actual fun get(): T? = weakRef?.get()
}
