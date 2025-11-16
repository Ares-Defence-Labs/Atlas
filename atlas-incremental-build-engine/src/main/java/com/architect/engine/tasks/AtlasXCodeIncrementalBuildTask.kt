package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
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

    @get:Input
    abstract val cacheXCFramework: Property<Boolean>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:Input
    abstract val xcFrameworkOutputPath: Property<String>

    @get:Optional
    @get:Input
    abstract val runningAppleWatchLegacy: Property<Boolean>

    @get:Input
    abstract val runningAppleWatch: Property<Boolean>

    @get:OutputFile
    abstract val hashFile: RegularFileProperty

    @get:OutputFile
    abstract val xchashFile: RegularFileProperty

    init {
        group = "AtlasXcode"
        description = "XCFramework Generator for Apple's Incremental Building"
    }

    @TaskAction
    fun checkAndBuildXCFramework() {
        val isAppleWatch = runningAppleWatch.get()
        val module = moduleName.get()
        logger.lifecycle("Detected Module Name: $module")
        logger.lifecycle("🔁 Preparing Incremental Plugin")

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())

        val prevHashFile = hashFile.asFile.get()
        val currentXcHash = xchashFile.asFile.orNull
        if (currentXcHash?.exists() == true && currentXcHash.readText() == prevHashFile.readText()) {
            logger.lifecycle("🔁 XCFramework unchanged (hash match). Skipping.")
            return
        }

        val isSimulator = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (isDebug) "debug" else "release"
        val legacyRequested = runningAppleWatchLegacy.getOrElse(false)

        // Resolve outputs
        val deviceModernFramework = if (isAppleWatch)
            projectBuildDir.get().asFile.resolve("bin/watchosDeviceArm64/${buildType}Framework/$module.framework")
        else
            projectBuildDir.get().asFile.resolve("bin/iosArm64/${buildType}Framework/$module.framework")

        val deviceLegacyFramework: File? = if (isAppleWatch && legacyRequested)
            projectBuildDir.get().asFile.resolve("bin/watchosArm64/${buildType}Framework/$module.framework")
        else null

        val simFramework = if (isAppleWatch)
            projectBuildDir.get().asFile.resolve("bin/watchosSimulatorArm64/${buildType}Framework/$module.framework")
        else
            projectBuildDir.get().asFile.resolve("bin/iosSimulatorArm64/${buildType}Framework/$module.framework")

        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)

        // Decide which framework(s) to pass to -create-xcframework
        val frameworksArgs = mutableListOf<String>()

        if (isSimulator) {
            // Simulator run: only simulator slice is required
            if (!simFramework.exists()) {
                throw GradleException("Simulator run requested, but simulator framework not found at: ${simFramework.absolutePath}")
            }
            logger.lifecycle("🧪 Simulator build detected → using simulator framework only.")
            frameworksArgs += listOf("-framework", simFramework.absolutePath)
        } else {
            // Device run: prefer merged (modern+legacy) if requested & available, else modern only
            if (!deviceModernFramework.exists()) {
                throw GradleException("Device build requested, but modern device framework not found at: ${deviceModernFramework.absolutePath}")
            }

            val deviceFrameworkForXC = if (isAppleWatch && legacyRequested) {
                if (deviceLegacyFramework == null || !deviceLegacyFramework.exists()) {
                    logger.warn("⚠️ Legacy requested but legacy device framework missing; falling back to modern only.")
                    deviceModernFramework
                } else {
                    // Merge arm64 (modern) + arm64_32 (legacy) → fat device framework
                    val tempMergeDir = File(project.layout.buildDirectory.get().asFile, "atlas/tmp-fat-device/$module.framework")
                    if (tempMergeDir.exists()) tempMergeDir.deleteRecursively()
                    tempMergeDir.parentFile.mkdirs()

                    deviceModernFramework.copyRecursively(tempMergeDir, overwrite = true)

                    val modernBin = frameworkBinary(deviceModernFramework)
                    val legacyBin = frameworkBinary(deviceLegacyFramework)
                    val mergedBin = frameworkBinary(tempMergeDir)

                    val lipoOut = ByteArrayOutputStream()
                    val lipoErr = ByteArrayOutputStream()
                    execOps.exec {
                        isIgnoreExitValue = false
                        commandLine(
                            "xcrun", "lipo",
                            "-create",
                            "-output", mergedBin.absolutePath,
                            modernBin.absolutePath,
                            legacyBin.absolutePath
                        )
                        standardOutput = lipoOut
                        errorOutput = lipoErr
                    }
                    execOps.exec { commandLine("xcrun", "lipo", "-info", mergedBin.absolutePath) }

                    logger.lifecycle("📦 Created fat device framework (arm64 + arm64_32) for watchOS.")
                    tempMergeDir
                }
            } else {
                deviceModernFramework
            }

            frameworksArgs += listOf("-framework", deviceFrameworkForXC.absolutePath)
        }

        // Prepare output dir
        if (xcOutDir.exists() && xcOutDir.isFile) xcOutDir.delete()
        if (xcOutDir.exists()) xcOutDir.deleteRecursively()
        xcOutDir.parentFile.mkdirs()

        logger.lifecycle("🔧 Building XCFramework…")
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        try {
            execOps.exec {
                isIgnoreExitValue = false
                commandLine(
                    "xcodebuild", "-create-xcframework",
                    *frameworksArgs.toTypedArray(),
                    "-output", xcOutDir.absolutePath
                )
                standardOutput = stdout
                errorOutput = stderr
            }
        } catch (e: Exception) {
            val out = stdout.toString(Charsets.UTF_8.name())
            val err = stderr.toString(Charsets.UTF_8.name())
            val msg = buildString {
                appendLine("❌ xcodebuild -create-xcframework failed")
                appendLine("Command: xcodebuild -create-xcframework ${frameworksArgs.joinToString(" ")} -output ${xcOutDir.absolutePath}")
                appendLine("--- stderr ---"); appendLine(err.ifBlank { "<empty>" })
                appendLine("--- stdout ---"); appendLine(out.ifBlank { "<empty>" })
            }
            logger.error("MessageError - ${e.message}")
            logger.error("Exception - ${e.stackTraceToString()}")
            throw GradleException(msg, e)
        }

        // Install into Xcode project’s Frameworks/
        val xcodeFrameworksDir = projectRootDir.get().asFile.resolve("$module/Frameworks")
        val targetFramework = xcodeFrameworksDir.resolve("$module.xcframework")
        if (targetFramework.exists()) targetFramework.deleteRecursively()
        xcOutDir.copyRecursively(targetFramework, overwrite = true)

        xchashFile.asFile.get().writeText(hashFile.asFile.get().readText())
        logger.lifecycle("✅ Updating Build Hash")
        logger.lifecycle("✅ XCFramework copied to: ${targetFramework.absolutePath}")
    }
}

