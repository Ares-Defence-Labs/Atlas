package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

@CacheableTask
abstract class AtlasXCCheckIfRequiredRebuildTask @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val commonMainSource: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iosMainSource: DirectoryProperty

    @get:Input
    abstract val moduleName: Property<String>

    @get:Input
    abstract val isiOS: Property<Boolean>

    @get:Input
    abstract val cacheXCFramework: Property<Boolean>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    abstract val xcFrameworkOutputPath: Property<String>

    @get:OutputFile
    val flagFile: File
        get() = projectBuildDir.get().asFile.resolve("shouldRebuildXC.txt")

    @get:OutputFile
    val hashFile: File
        get() = projectBuildDir.get().asFile.resolve("xcframeworkInputsHash.txt")

    init {
        group = "AtlasXcode"
        description = "XCFramework Generator for Apple's Incremental Building"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun checkAndBuildXCFramework() {
        val inputFiles = (commonMainSource.asFileTree + iosMainSource.asFileTree).files
        val currentHash = hashFiles(inputFiles)
        val previousHash = if (hashFile.exists()) hashFile.readText() else ""

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())
        val shouldRebuild = (currentHash != previousHash || !xcOutDir.exists() || !isDebug)

        flagFile.writeText(if (shouldRebuild) "true" else "false")

        logger.lifecycle(if (shouldRebuild) "✅ Changes detected. Rebuild flag set." else "✅ No changes detected. XCFramework is up to date.")

        if (!shouldRebuild) return

        hashFile.writeText(currentHash)
    }

    private fun hashFiles(files: Collection<File>): String {
        val digest = MessageDigest.getInstance("SHA-256")
        files.forEach { file ->
            if (file.exists()) {
                digest.update(file.readBytes())
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}