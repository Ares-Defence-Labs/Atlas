package com.architect.atlastestclient.android.navigation

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.architect.atlas.viewBinding.architecture.lifecycle.AtlasFragment
import com.architect.atlastestclient.android.MainComponents
import com.architect.atlastestclient.android.R
import com.architect.atlastestclient.android.databinding.FragmentTestFirstBinding
import com.architect.atlastestclient.android.databinding.TestXmlBinding
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.viewModels.FragmentTestFirstViewModel
import kotlin.reflect.KClass

class FragmentTest1 : AtlasFragment<FragmentTestFirstBinding, FragmentTestFirstViewModel>() {
    override val viewModelType: KClass<FragmentTestFirstViewModel>
        get() = FragmentTestFirstViewModel::class

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTestFirstBinding {
        return FragmentTestFirstBinding.inflate(inflater, container, false)
    }

    var isNavigated = false

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if(!isNavigated) {
            isNavigated = true
            MainComponents.navController.navigate(R.id.fragmentTest2)
        }
    }
}

