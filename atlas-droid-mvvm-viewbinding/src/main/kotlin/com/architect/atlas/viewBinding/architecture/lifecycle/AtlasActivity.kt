package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.viewBinding.architecture.listeners.ActivityFragmentLifecycleListener
import kotlin.reflect.KClass

abstract class AtlasActivity<Binding : ViewBinding, VM : ViewModel> : FragmentActivity() {
    protected lateinit var binding: Binding
    val viewModel: VM by lazy {
        ViewModelProvider(this, AtlasViewModelFactory(viewModelType))
            .get(viewModelType.java)
    }

    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    private lateinit var fragmentLifecycleHandler : FragmentLifecycleCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onInitialize()

        binding = viewBindingInflate()
        setContentView(binding.root)

        fragmentLifecycleHandler = ActivityFragmentLifecycleListener(this)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleHandler, true)
    }

    override fun onDestroy() {
        viewModel.onCleared()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleHandler)
        super.onDestroy()
    }

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
}