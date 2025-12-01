package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ApplicationScope
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlas.viewBinding.architecture.listeners.ActivityFragmentLifecycleListener
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

abstract class AtlasActivity<Binding : ViewBinding, VM : ViewModel> : FragmentActivity() {
    lateinit var binding: Binding
    val viewModel: VM by lazy {
        AtlasDI.resolveViewModel(viewModelType)!!
    }

    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    private lateinit var fragmentLifecycleHandler: FragmentLifecycleCallbacks
    private val initialized = AtomicBoolean(false)
    private fun ensureInitialized() {
        if (initialized.compareAndSet(false, true)) {
            ApplicationScope.launch {
                viewModel.onInitialize()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureInitialized()

        binding = viewBindingInflate()
        setContentView(binding.root)

        fragmentLifecycleHandler = ActivityFragmentLifecycleListener(this)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleHandler, true)
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleHandler)
        if (!isChangingConfigurations) {
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