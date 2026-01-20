package com.architect.atlastestclient.android

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.Settings.Global
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.annotations.AtlasScreen
import com.architect.atlas.architecture.navigation.annotations.AtlasTab
import com.architect.atlas.container.AtlasContainer
//import com.architect.atlas.container.AtlasContainer
//import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlas.navigation.AtlasNavGraph

import com.architect.atlas.navigation.AtlasNavigation
import com.architect.atlas.navigation.AtlasTabItem
import com.architect.atlas.navigation.TabParentViewModelNavGraph
import com.architect.atlas.navigation.TabParentViewModelTabsNavigation
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.DroidStandardSecond
import com.architect.atlastestclient.software.DroidStandardThird
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreDashboardTabViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreSettingsTabViewModel
import com.architect.kmpessentials.KmpAndroid
import com.architect.kmpessentials.alerts.KmpAlert
import com.google.android.material.tabs.TabItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

//import com.architect.atlas.resources.fonts.AtlasFonts

//import com.architect.atlas.resources.fonts.AtlasFonts

//import com.architect.atlas.resources.images.AtlasImages

class MainActivity : FragmentActivity() {

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        AtlasDI.resolveService<Dep>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.test_xml)
        //val t = AtlasStrings

        //val fonts = AtlasFonts.roboto_semicondensed_blackitalic(this)


        AtlasDI.injectContainer(AtlasContainer);
//        AtlasDI.resolveService<Dep>()

        //AtlasDI.registerFactory()<>()<>()
        AtlasDI.registerInterfaceToInstance(AtlasNavigationService::class, AtlasNavigation)
        //AtlasNavigation.navigateToPageTest(DroidStandard::class, "")

        KmpAndroid.initializeApp(this)
        setContent {
            AtlasNavGraph()
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Text("")
    }
}

