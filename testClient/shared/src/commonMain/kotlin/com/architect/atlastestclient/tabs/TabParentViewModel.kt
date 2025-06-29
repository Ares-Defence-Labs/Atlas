package com.architect.atlastestclient.tabs

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.container.annotations.ViewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.DroidStandard
import com.architect.kmpessentials.alerts.KmpAlert
import com.architect.kmpessentials.logging.KmpLogging
import com.architect.kmpessentials.permissions.KmpPermissionsManager
import com.architect.kmpessentials.permissions.Permission
import com.architect.kmpessentials.permissions.PermissionStatus

@ViewModels
class TabParentViewModel : ViewModel() {
    override suspend fun onInitialize() {
        super.onInitialize()

        KmpAlert.showAlert("TAB NAVIGATION", "")
    }

    fun popToRoot() {
        KmpPermissionsManager.getCurrentPermissionState(Permission.Location) {
            if (it == PermissionStatus.Granted) {
                // permission is granted
            } else {
                // all other states
            }
        }
        KmpLogging.writeInfo("Popping toRoot", "RUNNING THIRD SCREEN")
        AtlasDI.resolveService<AtlasNavigationService>()
            .navigateToPagePushAndReplace(DroidStandard::class)
    }
}