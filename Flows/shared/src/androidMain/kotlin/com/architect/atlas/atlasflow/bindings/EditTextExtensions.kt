package com.architect.atlas.atlasflow.bindings

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.architect.atlas.atlasflow.MutableAtlasFlowState
import com.architect.atlas.atlasflow.bind
import kotlinx.coroutines.DisposableHandle

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.bindSectionError(
    lifecycleOwner: LifecycleOwner,
    errorFlow: MutableAtlasFlowState<String?>,
    treatBlankAsNull: Boolean = true
): DisposableHandle {
    return errorFlow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { message ->
        val effective = if (treatBlankAsNull && message.isNullOrBlank()) null else message

        isErrorEnabled = effective != null
        error = effective
    }
}


fun TextInputLayout.bindSectionErrorOptional(
    lifecycleOwner: LifecycleOwner,
    errorFlow: MutableAtlasFlowState<String>,
    treatBlankAsNull: Boolean = true
): DisposableHandle {
    return errorFlow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { message ->
        val effective = if (treatBlankAsNull && message.isNotBlank()) null else message

        isErrorEnabled = effective != null
        error = effective
    }
}


fun EditText.bindTwoWayText(
    lifecycleOwner: LifecycleOwner,
    state: MutableAtlasFlowState<String>
): DisposableHandle {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (state.getCurrentValue() != s.toString()) {
                state.postValueOnMainThread(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    addTextChangedListener(watcher)

    val job = state.asStateFlow().bind(lifecycleOwner.lifecycleScope) { value ->
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


fun EditText.bindText(
    lifecycleOwner: LifecycleOwner,
    flow: MutableAtlasFlowState<String>
): DisposableHandle {
    return flow.asStateFlow().bind(lifecycleOwner.lifecycleScope) { setText(it) }
}
