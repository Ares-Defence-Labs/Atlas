package com.architect.atlas.atlasflow

interface DisposableHandle {
    fun dispose()
}

fun DisposableHandle(block: () -> Unit): DisposableHandle {
    return object : DisposableHandle {
        override fun dispose() {
            block()
        }
    }
}

operator fun DisposableHandle.plus(other: DisposableHandle): DisposableHandle {
    return DisposableHandle {
        this.dispose()
        other.dispose()
    }
}
