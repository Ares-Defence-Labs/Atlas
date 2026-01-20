package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ApplicationScope
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

abstract class AtlasFragment<Binding : ViewBinding, out VM : ViewModel> : Fragment() {
    lateinit var binding: Binding

    protected abstract val viewModelType: KClass<out VM>
    val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        val resolved = AtlasDI.resolveViewModel(viewModelType)
            ?: error("AtlasDI returned null for ${viewModelType.qualifiedName}")

        @Suppress("UNCHECKED_CAST")
        resolved as VM
    }

    private val initialized = AtomicBoolean(false)
    private fun ensureInitialized() {
        if (initialized.compareAndSet(false, true)) {
            ApplicationScope.launch {
                viewModel.onInitialize()
            }
        }
    }

    protected abstract fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): Binding

    override fun onResume() {
        super.onResume()
        viewModel.onAppearing()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onDisappearing()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureInitialized()
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
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
    }
}