package com.architect.engine.helpers

import java.io.File
import java.security.MessageDigest

object HashExtensions {

    fun hashFiles(files: Collection<File>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        files.forEach { file ->
            if (file.exists()) {
                digest.update(file.readBytes())
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}