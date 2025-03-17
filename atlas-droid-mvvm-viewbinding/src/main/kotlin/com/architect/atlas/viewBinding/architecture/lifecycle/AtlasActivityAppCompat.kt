package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.viewBinding.architecture.listeners.ActivityFragmentLifecycleListener
import kotlin.reflect.KClass

abstract class AtlasActivityAppCompat<Binding : ViewBinding, VM : ViewModel> : AppCompatActivity() {
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
        viewModel.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleHandler)
        if(!isChangingConfigurations){
            viewModel.onCleared()
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppearing()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onDisappearing()
    }
}


