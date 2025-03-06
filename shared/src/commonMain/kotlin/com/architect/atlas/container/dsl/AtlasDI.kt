package com.architect.atlas.container.dsl

import kotlin.reflect.KClass

object AtlasDI {
    // 🔹 Holds a reference to AtlasContainer
    var container: AtlasContainerContract? = null

    /**
     * Injects the AtlasContainer instance (should be called at app startup).
     */
    fun injectContainer(containerInstance: AtlasContainerContract) {
        container = containerInstance
    }

    /**
     * Fetches a registered service from the AtlasContainer.
     */
    inline fun <reified T : Any> resolveService(): T {
        return container?.resolve(T::class)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    /**
     * Fetches a registered view model from the AtlasContainer (ViewModel)
     */
    fun <T : Any> resolveViewModel(clazz: KClass<T>): T? {
        return container?.resolveViewModel(clazz)
    }

    /**
     * Fetches a registered service from the AtlasContainer (with class instance type)
     */
    fun <T : Any> resolveServiceNullable(clazz: KClass<T>): T? {
        return container?.resolve(clazz)
    }

    /**
     * Fetches a registered service lazily from the AtlasContainer.
     */
    inline fun <reified T : Any> resolveLazyService(): Lazy<T> {
        return lazy {
            container?.resolve(T::class)
                ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
        }
    }

    /**
     * Registers an interface to a service dynamically into the AtlasContainer so that resolve<Interface> can be called.
     */
    inline fun <reified T : Any> registerInterfaceToInstance(
        clazz: KClass<T>,
        instance: T,
    ) {
        container?.register(clazz, instance, null, null, false)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    /**
     * Registers a service dynamically into the AtlasContainer.
     */
    inline fun <reified T : Any> registerService(
        instance: T? = null,
        noinline factory: (() -> T)? = null,
        scopeId: String? = null,
        viewModel: Boolean = false
    ) {
        container?.register(T::class, instance, factory, scopeId, viewModel)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    inline fun <reified T : Any> registerInstance(
        instance: T? = null,
    ) {
        container?.register(T::class, instance, null, null, false)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    inline fun <reified T : Any> registerSingleton(
        instance: T? = null,
    ) {
        container?.register(T::class, instance, null, null, false)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    inline fun <reified T : Any> registerViewModel(
        instance: T? = null,
    ) {
        container?.register(T::class, instance, null, null, true)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    inline fun <reified T : Any> registerFactory(
        noinline factory: (() -> T)? = null,
    ) {
        container?.register(T::class, null, factory, null, false)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }

    inline fun <reified T : Any> registerScoped(
        scopeId: String? = null,
    ) {
        container?.register(T::class, null, null, scopeId, false)
            ?: throw IllegalStateException("❌ AtlasContainer is not initialized. Call injectContainer() first.")
    }
}

