package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class CheckGraphInputChangesTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirs: ConfigurableFileCollection

    @get:OutputFile
    abstract val hashOutputFile: RegularFileProperty

    init {
        group = "Atlas"
        description =
            "Generates a hash representing current input state of the project (tracks all modules)"
    }

    @TaskAction
    fun checkChanges() {
        val allFiles = sourceDirs.files.flatMap { dir ->
            dir.walkTopDown()
                .filter {
                    it.isFile && (it.extension in listOf(
                        "kt", "java", "xml", "gradle", "json",
                        "png", "svg", "jpeg", // track images for resource generator
                        "ttf", "otf" // track fonts for resource generator

                        // swift files will not be tracked, since xcode will handle it (on ios side)
                    ))
                }
                .toList()
        }

        val hash = hashFiles(allFiles)
        logger.lifecycle("📦 Calculated hash: $hash")

        hashOutputFile.get().asFile.writeText(hash)
    }

    private fun hashFiles(files: Collection<File>): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        files.sortedBy { it.absolutePath }.forEach { file ->
            digest.update(file.readBytes())
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}