package com.architect.atlas.liveData

interface Closeable {
    fun close()

    operator fun plus(other: Closeable): Closeable {
        return Closeable {
            this.close()
            other.close()
        }
    }

    companion object {
        operator fun invoke(block: () -> Unit): Closeable {
            return object : Closeable {
                override fun close() {
                    block()
                }
            }
        }
    }
}