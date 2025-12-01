package com.architect.atlas.navigationEngine.helpers

object StringHelpers{

    fun String.normalizeToAscii(): String =
        this.map { if (it.code in 32..126) it else ' ' }.joinToString("")
}

