package com.architect.wearosapp

import android.content.res.Configuration
import android.os.Bundle
import com.architect.atlas.container.AtlasContainer
//import com.architect.atlas.container.AtlasContainer
//import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.navigation.AtlasNavGraph

import com.architect.atlas.navigation.AtlasNavigation
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.navigation.annotations.AtlasScreen
import com.architect.atlas.architecture.navigation.annotations.AtlasTab
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.DroidStandardSecond
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreDashboardTabViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreSettingsTabViewModel
import com.architect.kmpessentials.KmpAndroid
import com.architect.wearosapp.theme.AtlasTestClientTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
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
//

@AtlasScreen(DroidStandard::class, initial = true)
@Composable
fun GreetingView(vm: DroidStandard) {
    Box(
        modifier = Modifier
            .background(androidx.compose.ui.graphics.Color.Red)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,

        ) {
        Button({
            GlobalScope.launch {
                AtlasNavigation.navigateToPage(DroidStandardSecond::class)
            }
        }) {
            Text(text = "Screen 1 Button")
        }
    }
}

@AtlasScreen(DroidStandardSecond::class)
@Composable
fun GreetingSecondView(vm: DroidStandardSecond) {
    Box(
        modifier = Modifier
            .background(androidx.compose.ui.graphics.Color.Green)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button({
            vm.openThirdScreenPush()
        }) {
            Text(text = "Second Screen. CLICK ME!!!")
        }
    }

}



@AtlasTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
@Composable
fun GreetingTabOne(vm: CoreDashboardTabViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button({
            AtlasNavigation.popToRoot()
        }) {
            Text(text = "Dashboard Component")
        }
    }

}


@AtlasTab(CoreSettingsTabViewModel::class, position = 1, holder = TabParentViewModel::class)
@Composable
fun GreetingTabSecond(vm: CoreSettingsTabViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button({
        }) {
            Text(text = "Settings Component")
        }
    }

}
