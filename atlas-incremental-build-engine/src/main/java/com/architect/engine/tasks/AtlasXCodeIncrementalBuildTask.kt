package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
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

    @get:Input
    abstract val forceXCFrameworkGeneration: Property<Boolean>

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
        val module = moduleName.get()
        logger.lifecycle("Detected Module Name: $module")
        logger.lifecycle("🔁 Preparing Incremental Plugin")

        val isDebug = cacheXCFramework.getOrElse(false)
        val xcOutDir = File(xcFrameworkOutputPath.get())

        val prevHashFile = hashFile.asFile.get()
        val currentXcHash = xchashFile.asFile.orNull
        val forceXC = forceXCFrameworkGeneration.getOrElse(false)
        if (currentXcHash?.exists() == true && currentXcHash.readText() == prevHashFile.readText() && !forceXC) {
            logger.lifecycle("🔁 XCFramework unchanged (hash match). Skipping.")
            return
        }

        val buildType = if (isDebug) "debug" else "release"
        val buildDir = projectBuildDir.get().asFile

        fun frameworkDir(target: String) =
            buildDir.resolve("bin/$target/${buildType}Framework/$module.framework")

        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)

        // ----------------------
        // 1) Collect all existing slices
        // ----------------------

        val frameworksArgs = mutableListOf<String>()

        // iOS device/simulator
        val iosDevice = frameworkDir("iosArm64").takeIf { it.exists() }
        val iosSim = frameworkDir("iosSimulatorArm64").takeIf { it.exists() }

        iosDevice?.let {
            logger.lifecycle("📱 Including iOS device slice → ${it.absolutePath}")
            frameworksArgs += listOf("-framework", it.absolutePath)
        }
        iosSim?.let {
            logger.lifecycle("🧪 Including iOS simulator slice → ${it.absolutePath}")
            frameworksArgs += listOf("-framework", it.absolutePath)
        }

        val watchDeviceArm64Candidates = listOf(
            frameworkDir("watchosDeviceArm64"),
        )
        val watchDeviceModern = watchDeviceArm64Candidates.firstOrNull { it.exists() }
        val watchDeviceLegacy = frameworkDir("watchosArm64").takeIf { it.exists() }

        logger.lifecycle("WatchDeviceModern : $watchDeviceModern")
        logger.lifecycle("WatchDeviceLegacy : $watchDeviceLegacy")

        val baseWatchFramework = watchDeviceModern ?: watchDeviceLegacy

        if (baseWatchFramework != null) {
            val tempMergeDir = File(
                project.layout.buildDirectory.get().asFile,
                "atlas/tmp-fat-watch-device/$module.framework"
            )
            if (tempMergeDir.exists()) tempMergeDir.deleteRecursively()
            tempMergeDir.parentFile.mkdirs()

            baseWatchFramework.copyRecursively(tempMergeDir, overwrite = true)

            val modernBin = watchDeviceModern?.let { frameworkBinary(it) }
            val legacyBin = watchDeviceLegacy?.let { frameworkBinary(it) }
            val mergedBin = frameworkBinary(tempMergeDir)

            val lipoOut = ByteArrayOutputStream()
            val lipoErr = ByteArrayOutputStream()

            mergeOrCopyBinaries(
                execOps = execOps,
                mergedBin = mergedBin,
                modernBin = modernBin,
                legacyBin = legacyBin,
                lipoOut = lipoOut,
                lipoErr = lipoErr
            )

            logger.lifecycle("📦 Prepared watchOS device framework (merged or single slice).")
            logger.lifecycle("⌚ Including watchOS device slice → ${tempMergeDir.absolutePath}")
            frameworksArgs += listOf("-framework", tempMergeDir.absolutePath)
        } else {
            logger.lifecycle("⚠️ No watchOS device frameworks found. Skipping watchOS device slice.")
        }

        // watchOS simulator (arm64 or x64)
        val watchSimArm64 = frameworkDir("watchosSimulatorArm64").takeIf { it.exists() }
        val watchSimX64 = frameworkDir("watchosX64").takeIf { it.exists() }
        val watchSim = watchSimArm64 ?: watchSimX64

        watchSim?.let {
            logger.lifecycle("⌚🧪 Including watchOS simulator slice → ${it.absolutePath}")
            frameworksArgs += listOf("-framework", it.absolutePath)
        }

        if (frameworksArgs.isEmpty()) {
            throw GradleException(
                "No frameworks found to package into XCFramework. " +
                        "Looked for iosArm64, iosSimulatorArm64, watchosArm64/deviceArm64, " +
                        "watchosArm32, watchosSimulatorArm64, watchosX64 under bin/*/${buildType}Framework."
            )
        }

        // ----------------------
        // 2) Build XCFramework
        // ----------------------
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
                appendLine(
                    "Command: xcodebuild -create-xcframework " +
                            frameworksArgs.joinToString(" ") +
                            " -output ${xcOutDir.absolutePath}"
                )
                appendLine("--- stderr ---"); appendLine(err.ifBlank { "<empty>" })
                appendLine("--- stdout ---"); appendLine(out.ifBlank { "<empty>" })
            }
            logger.error("MessageError - ${e.message}")
            logger.error("Exception - ${e.stackTraceToString()}")
            throw GradleException(msg, e)
        }

        // ----------------------
        // 3) Copy into Xcode project
        // ----------------------
        val xcodeFrameworksDir = projectRootDir.get().asFile.resolve("$module/Frameworks")
        val targetFramework = xcodeFrameworksDir.resolve("$module.xcframework")
        if (targetFramework.exists()) targetFramework.deleteRecursively()
        xcOutDir.copyRecursively(targetFramework, overwrite = true)

        xchashFile.asFile.get().writeText(prevHashFile.readText())
        logger.lifecycle("✅ Updating Build Hash")
        logger.lifecycle("✅ XCFramework copied to: ${targetFramework.absolutePath}")
    }

    fun mergeOrCopyBinaries(
        execOps: ExecOperations,
        mergedBin: File,
        modernBin: File?,
        legacyBin: File?,
        lipoOut: OutputStream,
        lipoErr: OutputStream
    ) {
        val inputs = listOfNotNull(
            modernBin?.absolutePath,
            legacyBin?.absolutePath
        )

        when (inputs.size) {
            0 -> error("No watchOS device binaries available to merge into ${mergedBin.absolutePath}")

            1 -> {
                // ✅ Only one slice: just copy it to mergedBin
                val src = File(inputs.first())
                mergedBin.parentFile.mkdirs()
                src.copyTo(mergedBin, overwrite = true)
                println("Only one watchOS slice present → copied ${src.absolutePath} to ${mergedBin.absolutePath}")
            }

            else -> {
                // ✅ Multiple slices: use lipo to create a universal binary
                mergedBin.parentFile.mkdirs()

                execOps.exec {
                    isIgnoreExitValue = false
                    commandLine(
                        "xcrun", "lipo",
                        "-create",
                        "-output", mergedBin.absolutePath,
                        *inputs.toTypedArray()
                    )
                    standardOutput = lipoOut
                    errorOutput = lipoErr
                }

                execOps.exec {
                    commandLine("xcrun", "lipo", "-info", mergedBin.absolutePath)
                }
            }
        }
    }
}

