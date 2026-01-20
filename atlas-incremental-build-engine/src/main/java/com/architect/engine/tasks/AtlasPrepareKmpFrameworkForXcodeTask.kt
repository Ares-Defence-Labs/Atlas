package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class AtlasPrepareKmpFrameworkForXcodeTask @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:OutputDirectory
    abstract val stableOutputRoot: DirectoryProperty

    @get:Input
    abstract val allowForceLegacy: Property<Boolean>

    init {
        group = "AtlasXcode"
        description = "Prepares (copies) the raw KMP framework into a stable folder for Xcode compile-time imports."
        allowForceLegacy.convention(false)
    }

    @TaskAction
    fun prepare() {
        val module = moduleName.get()
        val buildDir = projectBuildDir.get().asFile
        val outRoot = stableOutputRoot.get().asFile

        val env = System.getenv()
        val sdkName = env["SDK_NAME"] ?: ""
        val effectivePlatform = env["EFFECTIVE_PLATFORM_NAME"] ?: ""
        val configuration = env["CONFIGURATION"] ?: "Debug"

        val isWatch = sdkName.startsWith("watch", ignoreCase = true)
        val isSim = sdkName.contains("simulator", ignoreCase = true) ||
                effectivePlatform.contains("simulator", ignoreCase = true)

        val buildTypeDir = if (configuration.equals("Debug", true)) "debug" else "release"

        fun frameworkDir(target: String) =
            buildDir.resolve("bin/$target/${buildTypeDir}Framework/$module.framework")

        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)

        // iOS
        val iosDevice = frameworkDir("iosArm64").takeIf { it.exists() }
        val iosSim = frameworkDir("iosSimulatorArm64").takeIf { it.exists() }

        // watch device
        val watchDeviceModern = frameworkDir("watchosDeviceArm64").takeIf { it.exists() }
        val watchArm64 = frameworkDir("watchosArm64").takeIf { it.exists() }
        val watchArm64_32 = frameworkDir("watchosArm64_32").takeIf { it.exists() }

        // watch sim
        val watchSimArm64 = frameworkDir("watchosSimulatorArm64").takeIf { it.exists() }
        val watchSimX64 = frameworkDir("watchosX64").takeIf { it.exists() }

        val platformFolder = when {
            isWatch && isSim -> "watchos-simulator"
            isWatch && !isSim -> "watchos-device"
            !isWatch && isSim -> "ios-simulator"
            else -> "ios-device"
        }

        val stableDir = outRoot.resolve(platformFolder).resolve(configuration)
        val stableFramework = stableDir.resolve("$module.framework")

        stableDir.mkdirs()
        if (stableFramework.exists()) stableFramework.deleteRecursively()

        val sourceFramework: File? = when {
            isWatch && isSim -> watchSimArm64 ?: watchSimX64
            isWatch && !isSim -> watchDeviceModern ?: watchArm64 ?: watchArm64_32
            !isWatch && isSim -> iosSim
            else -> iosDevice
        }

        if (sourceFramework == null || !sourceFramework.exists()) {
            throw GradleException(
                "No KMP framework found to prepare for Xcode.\n" +
                        "Looked under: ${buildDir.resolve("bin").absolutePath}\n" +
                        "platform=$platformFolder config=$configuration buildTypeDir=${buildTypeDir}Framework"
            )
        }

        val finalFrameworkToCopy: File =
            if (isWatch && !isSim &&
                allowForceLegacy.get() &&
                watchDeviceModern == null &&
                watchArm64 != null && watchArm64_32 != null
            ) {
                val tempFat = outRoot.resolve("tmp-fat-watch-device").resolve(configuration).resolve("$module.framework")
                if (tempFat.exists()) tempFat.deleteRecursively()
                tempFat.parentFile.mkdirs()

                watchArm64.copyRecursively(tempFat, overwrite = true)

                val mergedBin = frameworkBinary(tempFat)
                val arm64Bin = frameworkBinary(watchArm64)
                val arm64_32Bin = frameworkBinary(watchArm64_32)

                val out = ByteArrayOutputStream()
                val err = ByteArrayOutputStream()

                logger.lifecycle("🧬 forceLegacy=true → merging arm64 + arm64_32 for stable output")
                try {
                    execOps.exec {
                        isIgnoreExitValue = false
                        commandLine(
                            "xcrun", "lipo",
                            "-create",
                            "-output", mergedBin.absolutePath,
                            arm64Bin.absolutePath,
                            arm64_32Bin.absolutePath
                        )
                        standardOutput = out
                        errorOutput = err
                    }
                } catch (e: Exception) {
                    throw GradleException(
                        "❌ lipo merge failed.\n--- stderr ---\n${err.toString(Charsets.UTF_8.name())}\n" +
                                "--- stdout ---\n${out.toString(Charsets.UTF_8.name())}",
                        e
                    )
                }

                tempFat
            } else {
                sourceFramework
            }

        logger.lifecycle("➡️ Preparing stable framework: ${finalFrameworkToCopy.absolutePath} → ${stableFramework.absolutePath}")
        finalFrameworkToCopy.copyRecursively(stableFramework, overwrite = true)

        logger.lifecycle("✅ Stable framework ready at: ${stableFramework.absolutePath}")
    }
}