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
import com.architect.atlastestclient.software.DroidStandardSecond

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

