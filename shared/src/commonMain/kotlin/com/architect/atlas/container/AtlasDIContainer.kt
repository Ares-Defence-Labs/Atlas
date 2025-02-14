package com.architect.atlas.container

import kotlin.reflect.KClass

expect object AtlasDIContainer {
    fun <T : Any> resolve(clazz: KClass<T>): T
}