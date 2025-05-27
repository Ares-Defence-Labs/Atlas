package com.architect.atlastestclient.tabs

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.container.annotations.ViewModels
import com.architect.kmpessentials.alerts.KmpAlert

@ViewModels
class TabParentViewModel : ViewModel(){
    override suspend fun onInitialize() {
        super.onInitialize()

        KmpAlert.showAlert("TAB NAVIGATION","")
    }
}