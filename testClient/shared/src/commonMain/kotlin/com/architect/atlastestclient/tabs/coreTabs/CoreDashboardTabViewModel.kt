package com.architect.atlastestclient.tabs.coreTabs

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.container.annotations.ViewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.DroidStandardSecond
import com.architect.kmpessentials.alerts.KmpAlert
import com.architect.kmpessentials.logging.KmpLogging

@ViewModels
class CoreDashboardTabViewModel : ViewModel() {
    override fun onAppearing() {
        super.onAppearing()

        KmpLogging.writeInfo("SampleTest", "Dashboard")
        KmpAlert.showAlert("Sample Dashboard", "Dashboard Tab Appearing")
    }

    fun openDroidScreen(){
        AtlasDI.resolveService<AtlasNavigationService>()
            .popToRoot()


    }
}

