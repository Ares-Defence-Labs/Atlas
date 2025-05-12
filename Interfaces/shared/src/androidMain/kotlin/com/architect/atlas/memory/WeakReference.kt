package com.architect.atlas.memory

import java.lang.ref.WeakReference as JvmWeakReference

actual class WeakReference<T : Any> actual constructor(value: T?) {
    private val weakRef = JvmWeakReference(value)

    actual fun get(): T? = weakRef.get()
}