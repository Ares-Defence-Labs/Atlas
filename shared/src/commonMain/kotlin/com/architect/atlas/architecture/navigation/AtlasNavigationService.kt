package com.architect.atlas.architecture.navigation

import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

interface AtlasNavigationService {
    fun <T : ViewModel> navigateToPage(viewModelClass: KClass<T>, params: Any? = null)
    fun <T : ViewModel> navigateToPageModal(viewModelClass: KClass<T>, params: Any? = null)

    fun <T : ViewModel> setNavigationStack(stack: List<T>, params: Any? = null)
    fun <T : ViewModel> getNavigationStack(): List<T>

    fun popToRoot(animate: Boolean = true, params: Any? = null)
    fun popPage(animate: Boolean = true, params: Any? = null)
    fun popPagesWithCount(countOfPages: Int, animate: Boolean = true, params: Any? = null)

    fun popToPage(route: String, params: Any? = null)
    fun dismissModal(animate: Boolean = true, params: Any? = null)
}