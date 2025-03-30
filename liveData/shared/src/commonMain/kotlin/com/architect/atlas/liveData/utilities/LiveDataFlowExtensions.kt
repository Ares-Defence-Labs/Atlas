package com.architect.atlas.liveData.utilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeSource

fun <T> Flow<T>.throttleFirst(windowDuration: Duration): Flow<T> = flow {
    val mutex = Mutex()
    var lastEmissionTime = 0L
    val timeSource = TimeSource.Monotonic

    collect { value ->
        val currentTime = timeSource.markNow().elapsedNow().inWholeMilliseconds

        mutex.withLock {
            if (currentTime - lastEmissionTime >= windowDuration.inWholeMilliseconds) {
                lastEmissionTime = currentTime
                emit(value)
            }
        }
    }
}


fun <T> Flow<T>.throttleFirst(windowDurationMs: Long): Flow<T> = flow {
    val mutex = Mutex()
    var lastEmissionTime = 0L
    val timeSource = TimeSource.Monotonic

    collect { value ->
        val currentTime = timeSource.markNow().elapsedNow().inWholeMilliseconds

        mutex.withLock {
            if (currentTime - lastEmissionTime >= windowDurationMs) {
                lastEmissionTime = currentTime
                emit(value)
            }
        }
    }
}