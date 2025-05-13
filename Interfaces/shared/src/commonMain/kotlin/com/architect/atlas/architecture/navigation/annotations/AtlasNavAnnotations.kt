package com.architect.atlas.architecture.navigation.annotations

import com.architect.atlas.architecture.mvvm.ViewModel
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class AtlasScreen(
    val viewModel: KClass<out ViewModel>,
    val initial: Boolean = false,
    val isTabHolder: Boolean = false
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class AtlasTab(
    val viewModel: KClass<out ViewModel>,
    val position: Int = 0,
    val holder: KClass<out ViewModel>
)

