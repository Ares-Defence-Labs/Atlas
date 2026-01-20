package android.compose.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.architect.atlas.architecture.navigation.annotations.AtlasScreen
import com.architect.atlas.navigation.AtlasTabItem
import com.architect.atlas.navigation.TabParentViewModelNavGraph
import com.architect.atlas.navigation.TabParentViewModelTabsNavigation
import com.architect.atlastestclient.tabs.TabParentViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreDashboardTabViewModel
import com.architect.atlastestclient.tabs.coreTabs.CoreSettingsTabViewModel
import com.architect.kmpessentials.alerts.KmpAlert

@AtlasScreen(TabParentViewModel::class, isTabHolder = true)
@Composable
fun GreetingThirdTabHolder(vm: TabParentViewModel) {
    val items = listOf(
        AtlasTabItem("Dashboard", CoreDashboardTabViewModel::class, Icons.Default.Home),
        AtlasTabItem("Settings", CoreSettingsTabViewModel::class, Icons.Default.Settings)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentTab = TabParentViewModelTabsNavigation.getCurrentTabViewModel()
                items.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab.viewModel,
                        onClick = {

                        },
                        icon = { Icon(tab.icon!!, contentDescription = null) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            TabParentViewModelNavGraph {
                KmpAlert.showAlert("Position $it", "Changed")
            }
        }
    }
}
