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
    val viewModel: VM by lazy {
        ViewModelProvider(this, AtlasViewModelFactory(viewModelType)).get(viewModelType.java)
    }

    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): Binding

    override fun onResume() {
        super.onResume()
        viewModel.onAppeared()
    }

    override fun onStart() {
        super.onStart()

        viewModel.onAppearing()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onDisappeared()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onDisappearing()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onInitialize()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = viewBindingInflate(inflater, container)
        return binding.root
    }

    fun resetComponent() {
        viewModel.onCleared()
        AtlasDI.resetViewModel(viewModelType)
        viewModelStore.clear()
    }
}