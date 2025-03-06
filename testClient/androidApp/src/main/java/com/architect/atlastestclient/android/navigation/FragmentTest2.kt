package com.architect.atlastestclient.android.navigation

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.architect.atlas.viewBinding.architecture.lifecycle.AtlasFragment
import com.architect.atlastestclient.android.databinding.FragmentTestSecondBinding
import com.architect.atlastestclient.viewModels.FragmentTestSecondViewModel
import kotlin.reflect.KClass

class FragmentTest2 : AtlasFragment<FragmentTestSecondBinding, FragmentTestSecondViewModel>() {
    override val viewModelType: KClass<FragmentTestSecondViewModel>
        get() = FragmentTestSecondViewModel::class

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTestSecondBinding {
        return FragmentTestSecondBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("TEST_FRAGMENT", viewModel.q)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        viewModel.processResult()
        Log.i("TEST_FRAGMENT_ON_CONFIG_CHANGED", viewModel.q)
    }
}