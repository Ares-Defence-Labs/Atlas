package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.viewbinding.ViewBinding
import com.architect.atlas.viewBinding.architecture.listeners.ActivityFragmentLifecycleListener

abstract class AtlasActivityWithoutViewModelNonGeneric : AppCompatActivity() {
    protected lateinit var binding: ViewBinding
        private set

    protected abstract val bindingClass: Class<out ViewBinding>

    private lateinit var fragmentLifecycleHandler: FragmentLifecycleCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflateBinding(bindingClass, layoutInflater)
        setContentView(binding.root)

        fragmentLifecycleHandler = ActivityFragmentLifecycleListener(this)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleHandler, true)
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleHandler)
        super.onDestroy()
    }

    private fun inflateBinding(
        clazz: Class<out ViewBinding>,
        inflater: LayoutInflater
    ): ViewBinding {
        val method = clazz.getMethod("inflate", LayoutInflater::class.java)
        return method.invoke(null, inflater) as ViewBinding
    }
}