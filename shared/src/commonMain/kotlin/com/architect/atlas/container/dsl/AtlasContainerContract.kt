package com.architect.atlas.container.dsl

import kotlin.reflect.KClass

interface AtlasContainerContract {
    fun <T : Any> resolveViewModel(clazz: KClass<T>): T?
    fun <T : Any> resolve(clazz: KClass<T>): T
    fun <T : Any> register(
        clazz: KClass<T>,
        instance: T? = null,
        factory: (() -> T)? = null,
        scopeId: String? = null,
        viewModel: Boolean = false
    )
}