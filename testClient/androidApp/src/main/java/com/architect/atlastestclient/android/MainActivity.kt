package com.architect.atlastestclient.android

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.architect.atlas.resources.colors.AtlasColors
import com.architect.atlas.resources.images.AtlasImages
import com.architect.atlas.resources.strings.AtlasStrings

//import com.architect.atlas.resources.images.AtlasImages

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_xml)

        //AtlasString


        //com.architect.atlas.resources.strings
      //  val imagec = AtlasImages.android_svg(this)

//        val image = findViewById<ImageView>(R.id.sample_image);
//        image.setImageDrawable(imagec)
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
