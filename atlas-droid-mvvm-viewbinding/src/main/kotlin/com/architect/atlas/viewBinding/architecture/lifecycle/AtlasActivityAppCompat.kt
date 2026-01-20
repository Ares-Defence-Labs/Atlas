package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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

abstract class AtlasActivityAppCompat<Binding : ViewBinding, out VM : ViewModel> : AppCompatActivity() {
    protected lateinit var binding: Binding

    protected abstract val viewModelType: KClass<out VM>
    val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        val resolved = AtlasDI.resolveViewModel(viewModelType)
            ?: error("AtlasDI returned null for ${viewModelType.qualifiedName}")

        @Suppress("UNCHECKED_CAST")
        resolved as VM
    }
    protected abstract fun viewBindingInflate(): Binding
    private val initialized = AtomicBoolean(false)
    private fun ensureInitialized() {
        if (initialized.compareAndSet(false, true)) {
            ApplicationScope.launch {
                viewModel.onInitialize()
            }
        }
    }

    private lateinit var fragmentLifecycleHandler : FragmentLifecycleCallbacks
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


