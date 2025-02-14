package com.architect.atlas.container

import kotlin.reflect.KClass

actual object AtlasDIContainer {
    private val singletons = mutableMapOf<KClass<*>, Any>()
    private val factories = mutableMapOf<KClass<*>, () -> Any>()
    private val scopedInstances = mutableMapOf<KClass<*>, Any>()

    actual fun <T : Any> resolve(clazz: KClass<T>): T {
        return (singletons[clazz] ?: factories[clazz]?.invoke() ?: scopedInstances[clazz]) as? T
            ?: throw IllegalArgumentException("No provider found for ${clazz.simpleName}")
    }
}