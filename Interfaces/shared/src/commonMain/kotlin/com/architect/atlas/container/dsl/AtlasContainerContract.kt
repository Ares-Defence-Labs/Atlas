package com.architect.atlas.container.dsl

import kotlin.reflect.KClass

interface AtlasContainerContract {
    fun resetViewModelByName(clazz: String)
    fun <T : Any> resetViewModel(clazz: KClass<T>)
    fun <T : Any> resolveViewModel(clazz: KClass<T>): T?
    fun <T : Any> resolve(clazz: KClass<T>): T
    fun <T : Any> resolveByName(clazz: String): T
    fun <T : Any> register(
        clazz: KClass<T>,
        instance: T? = null,
        factory: (() -> T)? = null,
        scopeId: String? = null,
        viewModel: Boolean = false
    )
}