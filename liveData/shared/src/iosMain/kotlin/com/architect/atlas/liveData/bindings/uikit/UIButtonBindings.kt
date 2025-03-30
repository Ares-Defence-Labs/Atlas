package com.architect.atlas.liveData.bindings.uikit

import com.architect.atlas.liveData.Closeable
import com.architect.atlas.liveData.LiveData
import platform.UIKit.UIButton
import platform.UIKit.UIControlStateHighlighted
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UIImage

fun <T : String?> UIButton.bindTitle(liveData: LiveData<T>): Closeable {
    val observer: (T) -> Unit = { value ->
        this.setTitle(value, forState = UIControlStateNormal)
        this.setTitle(value, forState = UIControlStateHighlighted)
        this.setTitle(value, forState = UIControlStateSelected)
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}

fun UIButton.bindImage(
    liveData: LiveData<Boolean>,
    trueImage: UIImage,
    falseImage: UIImage
): Closeable {
    val observer: (Boolean) -> Unit = { value ->
        val imageToSet = if (value) trueImage else falseImage
        setImage(imageToSet, forState = UIControlStateNormal)
    }

    liveData.addObserver(observer)

    return object : Closeable {
        override fun close() {
            liveData.removeObserver(observer)
        }
    }
}