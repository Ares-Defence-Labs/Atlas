package com.architect.atlas.viewBinding.architecture.lifecycle

import android.app.Activity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import kotlin.reflect.KClass

abstract class AtlasActivity<Binding : ViewBinding, VM : ViewModel> : Activity() {
    protected val viewModel: VM by lazy {
        AtlasDI.resolveServiceNullable(viewModelType) ?: throw Exception("Could not find view model")
    }
    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBindingInflate().root)
    }
}