package com.architect.atlas.architecture.navigation

import com.architect.atlas.architecture.mvvm.ViewModel

interface AtlasNavigationService {
    fun <T : ViewModel> navigateToPage(params: Any)
    fun <T : ViewModel> navigateToPageModal(params: Any)

    fun popToRoot(animate: Boolean = true, params: Any? = null)
    fun popPage(animate: Boolean = true, params: Any? = null)
    fun popPagesWithCount(countOfPages: Int, animate: Boolean = true, params: Any? = null)

    fun popToPage(route: String, params: Any? = null)
    fun dismissModal(animate: Boolean = true, params: Any? = null)
}