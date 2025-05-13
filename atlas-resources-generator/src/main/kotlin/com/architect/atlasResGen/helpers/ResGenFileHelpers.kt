package com.architect.atlasResGen.helpers

import org.apache.fontbox.ttf.OTFParser
import org.apache.fontbox.ttf.TTFParser
import java.io.File

object ResGenFileHelpers {
    fun extractPostScriptName(fontFile: File): String? {
        return try {
            when (fontFile.extension) {
                "otf" -> {
                    OTFParser().parse(fontFile).name
                }
                else -> {
                    TTFParser().parse(fontFile).name
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}