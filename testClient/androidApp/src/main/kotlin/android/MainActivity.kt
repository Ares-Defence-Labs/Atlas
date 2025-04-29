package com.architect.atlastestclient.android
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import com.architect.atlas.architecture.navigation.AtlasNavigationService
import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.annotations.AtlasScreen
import com.architect.atlas.container.AtlasContainer
//import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.DroidStandardSecond
import kotlinx.coroutines.cancel

//import com.architect.atlas.resources.fonts.AtlasFonts

//import com.architect.atlas.resources.fonts.AtlasFonts

//import com.architect.atlas.resources.images.AtlasImages

class MainActivity : FragmentActivity() {

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        AtlasDI.resolveService<Dep>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.test_xml)
        //val t = AtlasStrings

        //val fonts = AtlasFonts.roboto_semicondensed_blackitalic(this)


        AtlasDI.injectContainer(AtlasContainer);
        AtlasDI.resolveService<Dep>()

        //AtlasDI.registerFactory()<>()<>()
        //At/lasDI.registerInterfaceToInstance(AtlasNavigationService::class, AtlasNavigation)
        //AtlasNavigation.navigateToPageTest(DroidStandard::class, "")
        setContent {
            MyApplicationTheme {
                //GreetingView()
            }
        }
        val t : ViewModel
    }
}
//

@AtlasScreen(DroidStandard::class, initial = true)
@Composable
fun GreetingView(vm: DroidStandard) {
    Button({
        AtlasDI.resolveService<Dep>().test()
    }){
        Text(text = "Screen 1 Button")
    }
}

@AtlasScreen(DroidStandardSecond::class)
@Composable
fun GreetingSecondView(vm: DroidStandardSecond) {
    Button({
      //  AtlasNavigation.popPage(true, 12)
    }){
        Text(text = "Second Screen. CLICK ME!!!")
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Text("")
    }
}

