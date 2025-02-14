package com.architect.atlas.container

import kotlin.reflect.KClass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

actual object AtlasDIContainer {
    private val singletons = mutableMapOf<KClass<*>, Any>()
    private val factories = mutableMapOf<KClass<*>, () -> Any>()
    private val scopedInstances = mutableMapOf<KClass<*>, Any>()
    private val viewModelFactories = mutableMapOf<KClass<out ViewModel>, () -> ViewModel>()

    actual fun <T : Any> resolve(clazz: KClass<T>): T {
        return (singletons[clazz] ?: factories[clazz]?.invoke() ?: scopedInstances[clazz]) as? T
            ?: throw IllegalArgumentException("No provider found for ${clazz.simpleName}")
    }

    fun <T : ViewModel> registerViewModel(clazz: KClass<T>, factory: () -> T) {
        viewModelFactories[clazz] = factory
    }

    fun <T : ViewModel> resolveViewModel(owner: ViewModelStoreOwner, clazz: KClass<T>): T {
        val factory = viewModelFactories[clazz]
            ?: throw IllegalArgumentException("No ViewModel factory found for ${clazz.simpleName}")

        return ViewModelProvider(owner, ViewModelFactory(factory))[clazz.java]
    }
}

class ViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as? T
            ?: throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}