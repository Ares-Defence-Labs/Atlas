package android.compose.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.architect.atlas.architecture.navigation.annotations.AtlasTab
import com.architect.atlas.navigation.TabParentViewModelTabsNavigation
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreSettingsTabViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreDashboardTabViewModel

@AtlasTab(CoreSettingsTabViewModel::class, position = 1, holder = TabParentViewModel::class)
@Composable
fun GreetingTabSecond(vm: CoreSettingsTabViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button({
            TabParentViewModelTabsNavigation.navigateToTabIndex(CoreDashboardTabViewModel::class)
        }) {
            Text(text = "Settings Component")
        }
    }

}
