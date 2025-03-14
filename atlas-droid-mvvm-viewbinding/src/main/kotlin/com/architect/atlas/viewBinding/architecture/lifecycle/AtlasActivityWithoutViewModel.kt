package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.viewbinding.ViewBinding
import com.architect.atlas.viewBinding.architecture.listeners.ActivityFragmentLifecycleListener

abstract class AtlasActivityWithoutViewModel<Binding : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: Binding
    protected abstract fun viewBindingInflate(): Binding

    private lateinit var fragmentLifecycleHandler : FragmentLifecycleCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = viewBindingInflate()
        setContentView(binding.root)

        fragmentLifecycleHandler = ActivityFragmentLifecycleListener(this)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleHandler, true)
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleHandler)
        super.onDestroy()
    }
}