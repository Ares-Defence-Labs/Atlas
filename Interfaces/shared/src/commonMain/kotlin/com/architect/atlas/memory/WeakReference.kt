package com.architect.atlas.memory

expect class WeakReference<T : Any>(value: T?) {
    fun get(): T?
}