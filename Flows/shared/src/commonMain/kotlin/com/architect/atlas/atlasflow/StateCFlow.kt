package com.architect.atlas.atlasflow

import kotlinx.coroutines.flow.StateFlow

class StateCFlow<T>(override val flow: StateFlow<T>) : CFlow<T>(flow) {

}