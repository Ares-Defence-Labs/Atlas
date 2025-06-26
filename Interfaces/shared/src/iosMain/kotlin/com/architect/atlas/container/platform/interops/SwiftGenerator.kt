package com.architect.atlas.container.platform.interops

import kotlinx.cinterop.getOriginalKotlinClass
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.ObjCProtocol

actual class SwiftClassGenerator {
    actual companion object {
        actual fun getClazz(type: Any): String {
           return when (type) {
                is ObjCClass -> getOriginalKotlinClass(type)?.qualifiedName
                is ObjCProtocol -> getOriginalKotlinClass(type)?.qualifiedName
                else -> null
            }!!
        }

        @Throws(IllegalArgumentException::class)
        fun getQualifiedNameOfInstance(instance: Any): String {
            return instance::class.qualifiedName
                ?: throw IllegalArgumentException("Could not resolve qualified name.")
        }
    }
}