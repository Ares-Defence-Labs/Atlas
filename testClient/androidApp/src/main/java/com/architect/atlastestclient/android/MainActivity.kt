package com.architect.atlastestclient.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.architect.atlas.container.AtlasContainer
import com.architect.atlas.container.android.viewModels
import com.architect.atlas.container.dsl.AtlasDI
import com.architect.atlastestclient.Greeting
import com.architect.atlastestclient.MainViewModel
import com.architect.atlastestclient.ReviewApps
import com.architect.atlastestclient.ReviewProcess
import com.architect.atlastestclient.ReviewProcessTester
import com.architect.atlastestclient.Sample


class MainActivity : ComponentActivity() {
    val vm : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AtlasDI.injectContainer(AtlasContainer)
        AtlasDI.registerSingleton(ReviewProcess())
        //AtlasDI.registerSingleton(ReviewProcessTester());

        var ts = AtlasDI.resolveService<ReviewProcessTester>()

        Log.i("TEST", "${ts.test()}")
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
