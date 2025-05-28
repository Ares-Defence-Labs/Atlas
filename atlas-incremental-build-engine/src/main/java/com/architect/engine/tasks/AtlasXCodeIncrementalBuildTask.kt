package com.architect.engine.tasks

import com.architect.engine.helpers.HashExtensions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class AtlasXCodeIncrementalBuildTask @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {
    @get:Input
    abstract val moduleName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

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
    abstract val hashFile: RegularFileProperty

    @get:OutputFile
    abstract val flagFile: RegularFileProperty

    init {
        group = "AtlasXcode"
        description = "XCFramework Generator for Apple's Incremental Building"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun checkAndBuildXCFramework() {
        val shouldRebuild = flagFile.asFile.get().readText() == "true"
        logger.lifecycle(if (shouldRebuild) "✅ Incremental Building is Required. Regenerating XCFramework" else "✅ No changes detected. XCFramework is up to date.")
        if(!shouldRebuild) return

        val module = moduleName.get()
        logger.lifecycle("Detected Module Name: $module")
        logger.lifecycle("🔁 Preparing Incremental Plugin")

        val inputFiles = (commonMainSource.asFileTree + iosMainSource.asFileTree).files
        val currentHash = HashExtensions.hashFiles(inputFiles)

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())

        val isSimulator = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (isDebug) "debug" else "release"
        val arm64Framework =
            projectBuildDir.get().asFile.resolve("bin/iosArm64/${buildType}Framework/$module.framework")
        val simFramework =
            projectBuildDir.get().asFile.resolve("bin/iosSimulatorArm64/${buildType}Framework/$module.framework")

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

        val xcodeFrameworksDir = projectRootDir.get().asFile.resolve("$module/Frameworks")
        val targetFramework = xcodeFrameworksDir.resolve("$module.xcframework")

        if (targetFramework.exists()) {
            targetFramework.deleteRecursively()
        }

        xcOutDir.copyRecursively(targetFramework, overwrite = true)
        hashFile.get().asFile.writeText(currentHash)

        logger.lifecycle("✅ Updating Build Hash")
        logger.lifecycle("✅ XCFramework copied to: ${targetFramework.absolutePath}")
    }
}

