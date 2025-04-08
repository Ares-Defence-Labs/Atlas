package com.architect.atlas.architecture.navigation.annotations

import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class AtlasScreen(
    val viewModel: KClass<out ViewModel>,
    val initial: Boolean = false
)