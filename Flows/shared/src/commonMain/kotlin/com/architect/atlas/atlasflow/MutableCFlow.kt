package com.architect.atlas.atlasflow

import kotlinx.coroutines.flow.MutableStateFlow

class MutableCFlow<T>(override val flow: MutableStateFlow<T>) : CFlow<T>(flow) {
    fun setValue(value: T) {
        flow.value = value
    }
}


