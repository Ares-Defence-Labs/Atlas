package com.architect.atlas.atlasflow.bindings

import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun SeekBar.bindProgress(
    lifecycleOwner: LifecycleOwner,
    state: MutableStateFlow<Int>
): DisposableHandle {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser && state.value != progress) {
                state.value = progress
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    val job = state.bind(lifecycleOwner.lifecycleScope) {
        if (progress != it) progress = it
    }

    return object : DisposableHandle {
        override fun dispose() {
            setOnSeekBarChangeListener(null)
            job.dispose()
        }
    }
}

