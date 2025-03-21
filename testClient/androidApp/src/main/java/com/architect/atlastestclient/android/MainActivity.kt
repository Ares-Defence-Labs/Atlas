package com.architect.atlastestclient.android

import android.os.Bundle
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlas.container.dsl.AtlasDI.container
import com.architect.atlas.viewBinding.architecture.lifecycle.AtlasActivity
import com.architect.atlastestclient.ReviewApps
import com.architect.atlastestclient.ReviewProcess
import com.architect.atlastestclient.TestProcess
import com.architect.atlastestclient.android.databinding.TestXmlBinding
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.TestHelloThere
import com.architect.atlastestclient.software.TestSingle
import kotlin.reflect.KClass

class MainComponents {
    companion object {
        lateinit var navController: NavController
    }
}

class MainActivity : AtlasActivity<TestXmlBinding, DroidStandard>() {
    override val viewModelType: KClass<DroidStandard>
        get() = DroidStandard::class

    override fun viewBindingInflate(): TestXmlBinding {
        return TestXmlBinding.inflate(layoutInflater)
    }

    override fun onStart() {
        super.onStart()

        MainComponents.navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AtlasDI.injectContainer(AtlasContainer)
        AtlasDI.registerSingleton(ReviewProcess())
        //AtlasDI.registerSingleton(TestProcess::class,  ReviewApps());

        container?.register(TestProcess::class, ReviewApps(), null, null, false)

        var ts = AtlasDI.resolveService<TestSingle>()
        var tsc = AtlasDI.resolveService<TestHelloThere>()

        Log.i("TEST", "${ts.helloThere()}")
        Log.i("TEST", "${tsc.helloThere()}")
        Log.i("TEST", "${viewModel.helloThere()}")
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
