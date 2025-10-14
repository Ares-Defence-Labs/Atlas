package com.architect.atlas.container.platform.interops

import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.ObjCProtocol
import kotlinx.cinterop.getOriginalKotlinClass

actual class SwiftClassGenerator {
    actual companion object {
        actual fun getClazz(type: Any): String {
            return when (type) {
                is ObjCClass -> getOriginalKotlinClass(type)?.qualifiedName
                is ObjCProtocol -> getOriginalKotlinClass(type)?.qualifiedName
                else -> null
            }!!
        }
    }
}