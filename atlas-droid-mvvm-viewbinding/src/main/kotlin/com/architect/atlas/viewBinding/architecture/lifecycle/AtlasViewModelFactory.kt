package com.architect.atlas.viewBinding.architecture.lifecycle

import androidx.lifecycle.ViewModelProvider
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import kotlin.reflect.KClass

internal class AtlasViewModelFactory<VM : ViewModel>(
    private val viewModelClass: KClass<VM>
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        val resolvedViewModel = AtlasDI.resolveViewModel(viewModelClass)

        @Suppress("UNCHECKED_CAST")
        return resolvedViewModel as? T
            ?: modelClass.getDeclaredConstructor().newInstance()
    }
}