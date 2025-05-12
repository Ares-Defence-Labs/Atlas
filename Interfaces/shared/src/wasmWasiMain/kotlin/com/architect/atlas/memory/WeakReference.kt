package com.architect.atlas.memory

actual class WeakReference<T : Any> actual constructor(value: T?) {
    private var weakRef: T? = value

    actual fun get(): T? = weakRef
}