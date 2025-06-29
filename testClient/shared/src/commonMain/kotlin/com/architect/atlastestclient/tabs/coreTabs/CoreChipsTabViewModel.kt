package com.architect.atlastestclient.tabs.coreTabs

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.annotations.ViewModels
import com.architect.kmpessentials.alerts.KmpAlert
import com.architect.kmpessentials.logging.KmpLogging

@ViewModels
class CoreChipsTabViewModel : ViewModel(){
    override fun onAppearing() {
        super.onAppearing()

        KmpLogging.writeInfo("Sample", "Chips")
        KmpAlert.showAlert("", "Chips Tab Appearing")
    }
}

