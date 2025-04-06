package com.architect.atlasResGen.helpers

import org.apache.fontbox.ttf.TTFParser
import java.io.File

object ResGenFileHelpers{
    fun extractPostScriptName(fontFile: File): String? {
        return try {
            val parser = TTFParser()
            val ttf = parser.parse(fontFile)
            ttf.name
        } catch (e: Exception) {
            null
        }
    }
}