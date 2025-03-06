package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

abstract class AtlasActivityAppCompat<Binding : ViewBinding, VM : ViewModel> : AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener {
    private var previousFragments: List<Fragment> = listOf()
    protected lateinit var binding: Binding
    val viewModel: VM by lazy {
        ViewModelProvider(this, AtlasViewModelFactory(viewModelType))
            .get(viewModelType.java)
    }
    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onInitializing()
        supportFragmentManager.addOnBackStackChangedListener(this)

        binding = viewBindingInflate()
        setContentView(binding.root)
    }

    override fun onBackStackChanged() {
        val currentFragments = supportFragmentManager.fragments
        val removedFragments =
            previousFragments.filter { it !in currentFragments && it is AtlasFragment<*, *> }

        removedFragments.forEach { fragment ->
            (fragment as? AtlasFragment<*, *>)?.resetComponent()
        }

        // Update previousFragments to track changes
        previousFragments = currentFragments.toList()
    }

    override fun onDestroy() {
        viewModel.onCleared()
        supportFragmentManager.removeOnBackStackChangedListener(this)
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

