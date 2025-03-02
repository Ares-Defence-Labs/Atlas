package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import kotlin.reflect.KClass

abstract class AtlasFragment<Binding : ViewBinding, VM : ViewModel> : Fragment() {
    protected lateinit var binding: Binding
    protected val viewModel: VM by lazy {
        AtlasDI.resolveServiceNullable(viewModelType) ?: ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[viewModelType]
    }
    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = viewBindingInflate(inflater, container)
        return binding.root
    }
}