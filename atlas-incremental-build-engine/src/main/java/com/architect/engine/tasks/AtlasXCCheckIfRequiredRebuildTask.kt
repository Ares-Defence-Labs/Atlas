package com.architect.engine.tasks

import com.architect.engine.helpers.HashExtensions
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class AtlasXCCheckIfRequiredRebuildTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val commonMainSource: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iosMainSource: DirectoryProperty

    @get:Input
    abstract val cacheXCFramework: Property<Boolean>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:Input
    abstract val xcFrameworkOutputPath: Property<String>

    @get:OutputFile
    abstract val flagFile: RegularFileProperty

    @get:OutputFile
    abstract val hashFile: RegularFileProperty

    init {
        group = "AtlasXcode"
        description = "XCFramework Generator for Apple's Caching & Verifying Hash File"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun checkAndBuildXCFramework() {
        val inputFiles = (commonMainSource.asFileTree + iosMainSource.asFileTree).files
        val currentHash = HashExtensions.hashFiles(inputFiles)
        val hash = hashFile.asFile.get()
        val previousHash = if (hash.exists()) hash.readText() else ""

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())
        val shouldRebuild = (currentHash != previousHash || !xcOutDir.exists() || !isDebug)

        flagFile.asFile.get().writeText(if (shouldRebuild) "true" else "false")

        logger.lifecycle(if (shouldRebuild) "✅ Changes detected. Rebuild flag set." else "✅ No changes detected. XCFramework is up to date.")

        if (!shouldRebuild) return
    }
}

