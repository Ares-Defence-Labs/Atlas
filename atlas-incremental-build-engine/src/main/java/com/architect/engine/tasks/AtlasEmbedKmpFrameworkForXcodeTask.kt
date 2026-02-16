// AtlasEmbedKmpFrameworkForXcodeTask.kt
package com.architect.engine.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

@CacheableTask
abstract class AtlasEmbedKmpFrameworkForXcodeTask @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:Input
    abstract val allowMissing: Property<Boolean>

    @get:Input
    abstract val allowForceLegacy: Property<Boolean>

    /** Xcode TARGET_BUILD_DIR (or BUILT_PRODUCTS_DIR). */
    @get:Input
    abstract val targetBuildDir: Property<String>

    @get:Input
    abstract val configuration: Property<String>

    @get:Input
    abstract val sdkName: Property<String>

    @get:Input
    abstract val effectivePlatformName: Property<String>

    @get:Input
    abstract val archs: Property<String>

    @get:Input
    abstract val frameworksFolderPath: Property<String> // FRAMEWORKS_FOLDER_PATH

    /** Xcode WRAPPER_NAME (e.g. otriOS.app or Extension.appex) */
    @get:Input
    abstract val wrapperName: Property<String>

    @get:Input
    abstract val codeSigningAllowed: Property<String> // CODE_SIGNING_ALLOWED (YES/NO)

    @get:Input
    abstract val expandedCodeSignIdentity: Property<String> // EXPANDED_CODE_SIGN_IDENTITY

    init {
        group = "AtlasXcode"
        description = "Embeds raw KMP .framework into Xcode build products AND inside the .app bundle (fixes Generic Archive)"
    }

    @TaskAction
    fun embedFrameworkForXcode() {
        val module = moduleName.get()
        val buildDir = projectBuildDir.get().asFile

        val cfg = configuration.get().ifBlank { "Debug" }
        val buildTypeDir = if (cfg.equals("Debug", ignoreCase = true)) "debugFramework" else "releaseFramework"

        val sdk = sdkName.get().ifBlank { System.getenv("SDK_NAME") ?: "" }
        val eff = effectivePlatformName.get().ifBlank { System.getenv("EFFECTIVE_PLATFORM_NAME") ?: "" }
        val archsStr = archs.get().ifBlank { System.getenv("ARCHS") ?: "" }

        val isWatch = sdk.startsWith("watch", ignoreCase = true)
        val isSim = eff.contains("simulator", ignoreCase = true) || sdk.contains("simulator", ignoreCase = true)
        val forceLegacy = allowForceLegacy.getOrElse(false)

        val requestedArchs: Set<String> = archsStr
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        fun wantsArch(a: String) = requestedArchs.contains(a)

        logger.lifecycle("🔎 SDK_NAME=$sdk (watch=$isWatch, sim=$isSim) CONFIGURATION=$cfg ARCHS='$archsStr'")
        logger.lifecycle("🔎 requestedArchs=$requestedArchs forceLegacy=$forceLegacy buildTypeDir=$buildTypeDir")
        logger.lifecycle("📦 build/bin root: ${buildDir.resolve("bin").absolutePath}")

        fun binFrameworkDir(target: String): File =
            buildDir.resolve("bin/$target/$buildTypeDir/$module.framework")

        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)

        // -------------------------
        // 1) Locate / prepare source framework (ARCH-aware)
        // -------------------------
        val preparedFramework: File = when {
            // watchOS simulator
            isWatch && isSim -> {
                val simArm64 = binFrameworkDir("watchosSimulatorArm64").takeIf { it.exists() }
                val simX64 = binFrameworkDir("watchosX64").takeIf { it.exists() }
                simArm64 ?: simX64 ?: missingOrFail(
                    "No watchOS simulator framework found",
                    lookedFor = listOf(
                        binFrameworkDir("watchosSimulatorArm64"),
                        binFrameworkDir("watchosX64")
                    )
                )
            }

            // watchOS device
            isWatch && !isSim -> {
                // Your folder naming:
                //  - arm64 device: watchosDeviceArm64
                //  - arm64_32 device: watchosArm64  (yes, odd, but that's what you have)
                val deviceArm64 = binFrameworkDir("watchosDeviceArm64").takeIf { it.exists() }
                val deviceArm64_32 = binFrameworkDir("watchosArm64").takeIf { it.exists() }
                val legacyArm32 = binFrameworkDir("watchosArm32").takeIf { it.exists() }

                val wantsArm64 = wantsArch("arm64")
                val wantsArm64_32 = wantsArch("arm64_32")
                val wantsArmv7k = wantsArch("armv7k")

                val picked: File = when {
                    wantsArm64_32 -> deviceArm64_32 ?: missingOrFail(
                        "Xcode requests arm64_32, but watchosArm64 framework not found",
                        lookedFor = listOf(binFrameworkDir("watchosArm64"))
                    )

                    wantsArm64 -> deviceArm64 ?: missingOrFail(
                        "Xcode requests arm64, but watchosDeviceArm64 framework not found",
                        lookedFor = listOf(binFrameworkDir("watchosDeviceArm64"))
                    )

                    wantsArmv7k -> legacyArm32 ?: missingOrFail(
                        "Xcode requests armv7k/arm32, but watchosArm32 framework not found",
                        lookedFor = listOf(binFrameworkDir("watchosArm32"))
                    )

                    else -> missingOrFail(
                        "Unsupported watch ARCHS='$archsStr' (refusing to guess)",
                        lookedFor = listOf(
                            binFrameworkDir("watchosDeviceArm64"),
                            binFrameworkDir("watchosArm64"),
                            binFrameworkDir("watchosArm32")
                        )
                    )
                }

                // Optional legacy merge: arm64 + arm64_32 only
                val needMerge = forceLegacy && deviceArm64 != null && deviceArm64_32 != null
                if (!needMerge) {
                    picked
                } else {
                    val tempFramework = buildDir.resolve("atlas/tmp-fat-watch-device/$module.framework")
                    if (tempFramework.exists()) tempFramework.deleteRecursively()
                    tempFramework.parentFile.mkdirs()

                    deviceArm64!!.copyRecursively(tempFramework, overwrite = true)

                    val mergedBin = frameworkBinary(tempFramework)
                    val a = frameworkBinary(deviceArm64)
                    val b = frameworkBinary(deviceArm64_32!!)

                    mergeOrCopyBinaries(
                        execOps = execOps,
                        mergedBin = mergedBin,
                        inputs = listOf(a, b),
                        lipoOut = ByteArrayOutputStream(),
                        lipoErr = ByteArrayOutputStream()
                    )

                    logger.lifecycle("📦 Prepared fat watch device framework (arm64 + arm64_32) → ${tempFramework.absolutePath}")
                    tempFramework
                }
            }

            // iOS simulator
            !isWatch && isSim -> {
                binFrameworkDir("iosSimulatorArm64").takeIf { it.exists() } ?: missingOrFail(
                    "No iOS simulator framework found",
                    lookedFor = listOf(binFrameworkDir("iosSimulatorArm64"))
                )
            }

            // iOS device
            else -> {
                binFrameworkDir("iosArm64").takeIf { it.exists() } ?: missingOrFail(
                    "No iOS device framework found",
                    lookedFor = listOf(binFrameworkDir("iosArm64"))
                )
            }
        }

        // -------------------------
        // 2) Copy to STABLE cache
        // -------------------------
        val stablePlatformDirName = when {
            isWatch && isSim -> "watchos-simulator"
            isWatch && !isSim -> "watchos-device"
            !isWatch && isSim -> "ios-simulator"
            else -> "ios-device"
        }

        val stableDir = buildDir.resolve("atlas/xcode-frameworks/$stablePlatformDirName/$cfg")
        val stableFramework = stableDir.resolve("$module.framework")

        stableDir.mkdirs()
        if (stableFramework.exists()) stableFramework.deleteRecursively()
        preparedFramework.copyRecursively(stableFramework, overwrite = true)

        val moduleMap = stableFramework.resolve("Modules/module.modulemap")
        if (!moduleMap.exists()) {
            throw GradleException("module.modulemap missing in stable framework: ${moduleMap.absolutePath}")
        }

        logger.lifecycle("✅ Stable framework ready: ${stableFramework.absolutePath}")

        // -------------------------
        // 3) Copy to compile-time frameworks dir (safe)
        //    IMPORTANT: do NOT put the .framework at the root of TARGET_BUILD_DIR
        //    because that creates a top-level archived product -> Generic Archive.
        // -------------------------
        val builtProductsDir = File(targetBuildDir.get())
        builtProductsDir.mkdirs()

        val compileFrameworksDir = builtProductsDir.resolve("Frameworks")
        compileFrameworksDir.mkdirs()

        val compileTimeFramework = compileFrameworksDir.resolve("$module.framework")
        if (compileTimeFramework.exists()) compileTimeFramework.deleteRecursively()
        stableFramework.copyRecursively(compileTimeFramework, overwrite = true)

        logger.lifecycle("✅ Copied for compile-time (-F .../Frameworks): ${compileTimeFramework.absolutePath}")

        // -------------------------
        // 4) Embed into the app/appex bundle Frameworks folder (runtime)
        //    FIX: must be inside WRAPPER_NAME/Frameworks, not Products/Applications/Frameworks
        // -------------------------
        val wrapper = wrapperName.get().ifBlank { System.getenv("WRAPPER_NAME") ?: "" }
        if (wrapper.isBlank()) {
            throw GradleException("WRAPPER_NAME is empty; cannot embed framework into app bundle.")
        }

        val fwFolderRaw = frameworksFolderPath.get().ifBlank {
            System.getenv("FRAMEWORKS_FOLDER_PATH") ?: "Frameworks"
        }

        // If FRAMEWORKS_FOLDER_PATH doesn't already include the wrapper, force it.
        val fwFolder = if (fwFolderRaw.contains(".app/") || fwFolderRaw.contains(".appex/")) {
            fwFolderRaw
        } else {
            "$wrapper/Frameworks"
        }

        val runtimeFrameworksDir = builtProductsDir.resolve(fwFolder)
        runtimeFrameworksDir.mkdirs()

        val runtimeFramework = runtimeFrameworksDir.resolve("$module.framework")
        if (runtimeFramework.exists()) runtimeFramework.deleteRecursively()
        stableFramework.copyRecursively(runtimeFramework, overwrite = true)

        logger.lifecycle("➡️ Embedding ${stableFramework.absolutePath} → ${runtimeFramework.absolutePath}")

        // -------------------------
        // 5) Codesign (if allowed)
        // -------------------------
        maybeCodesign(runtimeFramework)

        logger.lifecycle("✅ Embedded raw KMP framework into bundle: ${runtimeFramework.absolutePath}")
    }

    private fun missingOrFail(message: String, lookedFor: List<File>): File {
        val allow = allowMissing.getOrElse(false)
        val details = lookedFor.joinToString("\n") { " - ${it.absolutePath}" }

        val fullMsg = buildString {
            appendLine("❌ $message")
            appendLine("Looked for:")
            appendLine(details)
            appendLine("Tip: ensure the corresponding link<Debug/Release>Framework<target> task exists and ran.")
        }

        if (allow) {
            logger.warn(fullMsg)
            throw StopExecutionException(fullMsg)
        } else {
            throw GradleException(fullMsg)
        }
    }

    private fun maybeCodesign(frameworkDir: File) {
        val allowed = codeSigningAllowed.get().ifBlank { System.getenv("CODE_SIGNING_ALLOWED") ?: "NO" }
        if (!allowed.equals("YES", ignoreCase = true)) {
            logger.lifecycle("🔏 Codesigning skipped (CODE_SIGNING_ALLOWED=$allowed)")
            return
        }

        val identity = expandedCodeSignIdentity.get()
            .ifBlank { System.getenv("EXPANDED_CODE_SIGN_IDENTITY") ?: "" }

        if (identity.isBlank()) {
            logger.lifecycle("🔏 Codesigning skipped (EXPANDED_CODE_SIGN_IDENTITY empty)")
            return
        }

        logger.lifecycle("🔏 Codesigning ${frameworkDir.name} with identity: $identity")

        execOps.exec {
            isIgnoreExitValue = false
            commandLine(
                "/usr/bin/codesign",
                "--force",
                "--sign", identity,
                "--timestamp=none",
                "--preserve-metadata=identifier,entitlements,flags",
                frameworkDir.absolutePath
            )
        }
    }

    private fun mergeOrCopyBinaries(
        execOps: ExecOperations,
        mergedBin: File,
        inputs: List<File>,
        lipoOut: OutputStream,
        lipoErr: OutputStream
    ) {
        val existing = inputs.filter { it.exists() }.map { it.absolutePath }

        when (existing.size) {
            0 -> error("No binaries available to merge into ${mergedBin.absolutePath}")

            1 -> {
                mergedBin.parentFile.mkdirs()
                File(existing.first()).copyTo(mergedBin, overwrite = true)
                logger.lifecycle("ℹ️ Only one slice present → copied to merged binary")
            }

            else -> {
                mergedBin.parentFile.mkdirs()
                execOps.exec {
                    isIgnoreExitValue = false
                    commandLine(
                        "xcrun", "lipo",
                        "-create",
                        "-output", mergedBin.absolutePath,
                        *existing.toTypedArray()
                    )
                    standardOutput = lipoOut
                    errorOutput = lipoErr
                }
                execOps.exec {
                    isIgnoreExitValue = true
                    commandLine("xcrun", "lipo", "-info", mergedBin.absolutePath)
                }
            }
        }
    }
}