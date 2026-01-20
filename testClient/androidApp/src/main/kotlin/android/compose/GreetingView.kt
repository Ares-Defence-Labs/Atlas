package android.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.architect.atlas.architecture.navigation.annotations.AtlasScreen
import com.architect.atlas.navigation.AtlasNavigation
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.DroidStandardSecond
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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