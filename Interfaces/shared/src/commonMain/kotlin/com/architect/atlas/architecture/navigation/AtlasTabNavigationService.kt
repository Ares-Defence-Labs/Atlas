package com.architect.atlas.architecture.navigation

import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

interface AtlasTabNavigationService{
    fun <T : ViewModel> navigateToTabIndex(viewModelClass: KClass<T>, params: Any? = null)
}