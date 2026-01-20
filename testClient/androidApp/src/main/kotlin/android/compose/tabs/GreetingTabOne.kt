package android.compose.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.architect.atlas.architecture.navigation.annotations.AtlasTab
import com.architect.atlas.navigation.AtlasNavigation
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreDashboardTabViewModel
import com.architect.atlas.navigation.TabParentViewModelTabsNavigation
import com.architect.atlastestclient.tabs.coreTabs.CoreSettingsTabViewModel

@AtlasTab(CoreDashboardTabViewModel::class, position = 0, holder = TabParentViewModel::class)
@Composable
fun GreetingTabOne(vm: CoreDashboardTabViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button({
            TabParentViewModelTabsNavigation.navigateToTabIndex(CoreSettingsTabViewModel::class)
        }) {
            Text(text = "Dashboard Component")
        }
    }

}
