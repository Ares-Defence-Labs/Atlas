package com.architect.atlas.architecture.navigation.extensions

import com.architect.atlas.architecture.mvvm.ViewModel
import com.architect.atlas.architecture.navigation.Poppable
import com.architect.atlas.architecture.navigation.Pushable

@Suppress("UNCHECKED_CAST")
fun ViewModel.tryHandlePush(params: Any) {
    (this as? Pushable<Any>)?.onPushParams(params)
}

@Suppress("UNCHECKED_CAST")
fun ViewModel.tryHandlePop(params: Any) {
    (this as? Poppable<Any>)?.onPopParams(params)
}