package com.architect.atlas.container.platform.interops

expect class SwiftClassGenerator {
    companion object {
        fun getClazz(type: Any): String
    }
}