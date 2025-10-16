package com.architect.atlas.architecture.mvvm

import kotlinx.coroutines.*
import kotlin.jvm.JvmField

enum class CrashPolicy { NONE, CANCEL_SCOPE, RETHROW }

object ApplicationScope : CoroutineScope {
    private val job = SupervisorJob()

    @JvmField var crashReporter: (Throwable) -> Unit = { t -> println("Unhandled: $t") }
    @JvmField var crashPolicy: CrashPolicy = CrashPolicy.CANCEL_SCOPE

    private val crashHandler = CoroutineExceptionHandler { ctx, t ->
        runCatching { crashReporter(t) }
        when (crashPolicy) {
            CrashPolicy.NONE -> Unit
            CrashPolicy.CANCEL_SCOPE -> job.cancel(CancellationException("Fatal in AppScope", t))
            CrashPolicy.RETHROW -> throw t
        }
    }

    override val coroutineContext = job + Dispatchers.Default + crashHandler

    fun cancel() = job.cancel()

    fun launchIO(block: suspend CoroutineScope.() -> Unit) =
        launch(Dispatchers.Default, block = block)

    fun launchCPU(block: suspend CoroutineScope.() -> Unit) =
        launch(Dispatchers.Default, block = block)
}