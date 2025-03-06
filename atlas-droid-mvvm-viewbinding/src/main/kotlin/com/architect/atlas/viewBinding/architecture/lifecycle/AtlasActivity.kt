package com.architect.atlas.viewBinding.architecture.lifecycle

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

abstract class AtlasActivity<Binding : ViewBinding, VM : ViewModel> : FragmentActivity(),
    FragmentManager.OnBackStackChangedListener {
    private var previousFragments: List<Fragment> = listOf()
    protected lateinit var binding: Binding
    val viewModel: VM by lazy {
        ViewModelProvider(this, AtlasViewModelFactory(viewModelType))
            .get(viewModelType.java)
    }

    private var navigationId: Int? = null
    protected fun setNavigationControllerId(navigationId: Int) {
        this.navigationId = navigationId
    }

    protected abstract val viewModelType: KClass<VM>
    protected abstract fun viewBindingInflate(): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onInitializing()

        binding = viewBindingInflate()
        setContentView(binding.root)

        if(navigationId != null){
            setupNavControllerListener()
        }
        else {
            supportFragmentManager.addOnBackStackChangedListener(this)
        }
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

    private fun setupNavControllerListener() {
        val navController = getNavControllerOrNull()
        navController?.addOnDestinationChangedListener { _, _, _ ->
            // ✅ Manually trigger FragmentManager back stack change when NavController changes
            onBackStackChanged()
        }
    }

    private fun getNavControllerOrNull(): NavController? {
        return try {
            val nav = navigationId
            if (nav != null) {
                findNavController(nav)
            }

            null
        } catch (ex: Exception) {
            null // ✅ If NavController isn't found, return null to prevent crash
        }
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