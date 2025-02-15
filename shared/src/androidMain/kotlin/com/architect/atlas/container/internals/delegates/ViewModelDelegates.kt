//package com.architect.atlas.container.internals.delegates
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelStoreOwner
//import com.architect.atlas.container.AtlasDIContainer
//import kotlin.reflect.KClass
//import kotlin.properties.ReadOnlyProperty
//import kotlin.reflect.KProperty
////
////class ViewModelDelegate<T : ViewModel>(
////    private val viewModelClass: KClass<T>,
////    private val ownerProvider: () -> ViewModelStoreOwner
////) : ReadOnlyProperty<Any, T> {
////
////    override fun getValue(thisRef: Any, property: KProperty<*>): T {
////        val owner = ownerProvider()
////        //return AtlasDIContainer.resolveViewModel(owner, viewModelClass)
////    }
////}
////
////inline fun <reified T : ViewModel> viewModels(noinline ownerProvider: () -> ViewModelStoreOwner): ViewModelDelegate<T> {
////    return ViewModelDelegate(T::class, ownerProvider)
////}