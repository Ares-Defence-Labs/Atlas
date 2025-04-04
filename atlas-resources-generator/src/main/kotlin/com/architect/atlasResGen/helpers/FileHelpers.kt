package com.architect.atlasResGen.helpers

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File

object FileHelpers {
    fun getTrimmedFilePath(name: File): File {
        return File(name.path.replace("-", "_").toLowerCase())
    }

    fun forceRecreateAllFiles(project: Project): Boolean {
        return project.findProperty("atlas.forceRegenerate")?.toString()?.toBoolean() ?: false
    }

    fun toSnakeCase(name: String): String {
        return name.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .lowercase()
    }


    fun Project.runShellScript(script: File) {
        if (!script.exists()) {
            logger.error("❌ Script not found: ${script.absolutePath}")
            return
        }

        logger.lifecycle("🚀 Running script: ${script.absolutePath}")

        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()

        val result = exec {
            workingDir = script.parentFile
            commandLine("sh", script.absolutePath)
            standardOutput = output
            errorOutput = error
            isIgnoreExitValue = true
        }

        if (result.exitValue != 0) {
            logger.error("❌ Script failed:\n${error.toString(Charsets.UTF_8)}")
        } else {
            logger.lifecycle("✅ Script succeeded:\n${output.toString(Charsets.UTF_8)}")
        }
    }
}