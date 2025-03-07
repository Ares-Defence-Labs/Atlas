package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding

abstract class AtlasActivityAppCompatWithoutViewModel<Binding : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: Binding
    protected abstract fun viewBindingInflate(): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = viewBindingInflate()
        setContentView(binding.root)

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, fragment: Fragment) {
                if (fragment is AtlasFragment<*, *>) {
                    val isConfigChange = isChangingConfigurations
                    if (!isConfigChange) {
                        fragment.resetComponent()
                    }
                }
            }
        }, true)
    }
}


