package com.architect.atlas.atlasflow.bindings

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.flow.MutableStateFlow

fun EditText.bindTwoWayText(
    lifecycleOwner: LifecycleOwner,
    state: MutableStateFlow<String>
): DisposableHandle {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (state.value != s.toString()) {
                state.value = s.toString()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    addTextChangedListener(watcher)

    val job = state.bind(lifecycleOwner.lifecycleScope) { value ->
        if (text.toString() != value) {
            setText(value)
            setSelection(value.length)
        }
    }

    return object : DisposableHandle {
        override fun dispose() {
            removeTextChangedListener(watcher)
            job.dispose()
        }
    }
}