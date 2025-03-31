package com.architect.atlastestclient.android

import android.os.Bundle
import android.widget.ImageView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.architect.atlas.resources.colors.AtlasColors
import com.architect.atlas.resources.images.AtlasImages
import com.architect.atlas.resources.strings.AtlasStrings

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_xml)

        val image = findViewById<ImageView>(R.id.sample_image);
        image.setImageDrawable(AtlasImages.android_svg(this))
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
