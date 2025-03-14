package com.architect.atlas.viewBinding.architecture.listeners

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.architect.atlas.viewBinding.architecture.lifecycle.AtlasFragment

class ActivityFragmentLifecycleListener(private val activity : Activity) : FragmentLifecycleCallbacks() {
    override fun onFragmentDestroyed(fm: FragmentManager, fragment: Fragment) {
        if (fragment is AtlasFragment<*, *>) {
            val isConfigChange = activity.isChangingConfigurations
            if (!isConfigChange) {
                fragment.resetComponent()
            }
        }
    }
}