package com.architect.atlastestclient.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.annotations.Provides
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlas.container.dsl.AtlasDI.container
import com.architect.atlastestclient.ComponentTest
import com.architect.atlastestclient.DNSComps
import com.architect.atlastestclient.Greeting
import com.architect.atlastestclient.Hello
import com.architect.atlastestclient.MainViewModel
import com.architect.atlastestclient.ReviewApps
import com.architect.atlastestclient.ReviewProcess
import com.architect.atlastestclient.ReviewProcessTester
import com.architect.atlastestclient.Sample
import com.architect.atlastestclient.TestProcess
import com.architect.atlastestclient.dns.DNSTest
import com.architect.atlastestclient.services.HelloThereTest
import com.architect.atlastestclient.software.DroidStandard
import com.architect.atlastestclient.software.MobileTest
import com.architect.atlastestclient.software.TestHelloThere
import com.architect.atlastestclient.software.TestSingle


class MainActivity : ComponentActivity() {
    val vm: DroidStandard by viewModels()

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
        Log.i("TEST", "${vm.helloThere()}")
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingView(Greeting().greet())
                }
            }
        }
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
