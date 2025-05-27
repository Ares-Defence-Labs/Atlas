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
abstract class AtlasXCodeIncrementalBuildTask @Inject constructor(
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
        logger.lifecycle("Detected Module Name: ${moduleName.get()}")

        logger.lifecycle("🔁 Preparing Incremental Plugin")
        val inputFiles = (commonMainSource.asFileTree + iosMainSource.asFileTree).files
        val currentHash = hashFiles(inputFiles)
        val previousHash = if (hashFile.exists()) hashFile.readText() else ""

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())
        val shouldRebuild = (currentHash != previousHash || !xcOutDir.exists() || !isDebug)

        flagFile.writeText(if (shouldRebuild) "true" else "false")

        logger.lifecycle(if (shouldRebuild) "✅ Changes detected. Rebuild flag set." else "✅ No changes detected. XCFramework is up to date.")

        if (!shouldRebuild) return

        val isSimulator = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (isDebug) "debug" else "release"

        val linkTaskName = if (isSimulator) {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosSimulatorArm64"
        } else {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosArm64"
        }

        val createXCFrameworkCommand: (String) -> Unit = {
            val result = execOps.exec { // this needs to point to the wrapped gradle file
                commandLine("./gradlew :${moduleName.get()}:$it")
                isIgnoreExitValue = true
                standardOutput = System.out
                errorOutput = System.err
            }

            if (result.exitValue != 0) {
                throw GradleException("❌ xcodebuild failed to generate XCFramework with exit code ${result.exitValue}")
            }
        }

        val arm64Framework =
            projectBuildDir.get().asFile.resolve("bin/iosArm64/${buildType}Framework/${moduleName.get()}.framework")
        val simFramework =
            projectBuildDir.get().asFile.resolve("bin/iosSimulatorArm64/${buildType}Framework/${moduleName.get()}.framework")

        val frameworksArgs = mutableListOf<String>().apply {
            if (isDebug) {
                add("-framework")
                add(if (isSimulator) simFramework.absolutePath else arm64Framework.absolutePath)
            } else {
                add("-framework")
                add(arm64Framework.absolutePath)
            }
        }

        if (xcOutDir.exists() && xcOutDir.isFile) {
            logger.warn("⚠️ Deleting invalid .xcframework file at ${xcOutDir.absolutePath}")
            xcOutDir.delete()
        }
        if (xcOutDir.exists()) xcOutDir.deleteRecursively()
        xcOutDir.parentFile.mkdirs()

        logger.lifecycle("🔧 Building XCFramework...")
        val result = execOps.exec {
            commandLine(
                "xcodebuild",
                "-create-xcframework",
                *frameworksArgs.toTypedArray(),
                "-output", xcOutDir.absolutePath
            )
            isIgnoreExitValue = true
            standardOutput = System.out
            errorOutput = System.err
        }

        if (result.exitValue != 0) {
            throw GradleException("❌ xcodebuild failed with exit code ${result.exitValue}")
        }

        hashFile.writeText(currentHash)

        logger.lifecycle("✅ Updating Build Hash")
        val xcodeFrameworksDir = projectRootDir.get().asFile.resolve("otriOS/Frameworks")
        val targetFramework = xcodeFrameworksDir.resolve("${moduleName.get()}.xcframework")

        if (targetFramework.exists()) {
            targetFramework.deleteRecursively()
        }

        xcOutDir.copyRecursively(targetFramework, overwrite = true)

        logger.lifecycle("✅ XCFramework copied to: ${targetFramework.absolutePath}")
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

