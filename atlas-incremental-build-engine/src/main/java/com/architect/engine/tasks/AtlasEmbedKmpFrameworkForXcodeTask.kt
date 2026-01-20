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

    @get:Input
    abstract val codeSigningAllowed: Property<String> // CODE_SIGNING_ALLOWED (YES/NO)

    @get:Input
    abstract val expandedCodeSignIdentity: Property<String> // EXPANDED_CODE_SIGN_IDENTITY

    init {
        group = "AtlasXcode"
        description = "Embeds raw KMP .framework into Xcode build products BEFORE Swift compile and into app bundle"
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

        logger.lifecycle("🔎 SDK_NAME=$sdk (watch=$isWatch, sim=$isSim) CONFIGURATION=$cfg ARCHS='$archsStr'")
        logger.lifecycle("🔎 forceLegacy=$forceLegacy buildTypeDir=$buildTypeDir")
        logger.lifecycle("📦 build/bin root: ${buildDir.resolve("bin").absolutePath}")

        fun binFrameworkDir(target: String): File =
            buildDir.resolve("bin/$target/$buildTypeDir/$module.framework")

        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)

        // -------------------------
        // 1) Locate / prepare source framework
        // -------------------------

        val preparedFramework: File = when {
            isWatch && isSim -> {
                // watch simulator: arm64 preferred, x64 fallback
                val simArm64 = binFrameworkDir("watchosSimulatorArm64").takeIf { it.exists() }
                val simX64 = binFrameworkDir("watchosX64").takeIf { it.exists() }
                (simArm64 ?: simX64) ?: missingOrFail(
                    "No watchOS simulator framework found",
                    lookedFor = listOf(
                        binFrameworkDir("watchosSimulatorArm64"),
                        binFrameworkDir("watchosX64")
                    )
                )
            }

            isWatch && !isSim -> {
                // watch device:
                // - Prefer modern device target if present
                // - Else use legacy watchosArm64 + (optional) watchosArm64_32 if forceLegacy=true
                val modern = binFrameworkDir("watchosDeviceArm64").takeIf { it.exists() }
                if (modern != null && !forceLegacy) {
                    modern
                } else {
                    val arm64 = binFrameworkDir("watchosArm64").takeIf { it.exists() }
                    val arm64_32 = binFrameworkDir("watchosArm64_32").takeIf { it.exists() }
                    val arm32 = binFrameworkDir("watchosArm32").takeIf { it.exists() } // rare fallback
                    val base = modern ?: arm64 ?: arm32 ?: missingOrFail(
                        "No watchOS device framework found",
                        lookedFor = listOf(
                            binFrameworkDir("watchosDeviceArm64"),
                            binFrameworkDir("watchosArm64"),
                            binFrameworkDir("watchosArm64_32"),
                            binFrameworkDir("watchosArm32")
                        )
                    )

                    // If forceLegacy=true and we have both arm64 + arm64_32, merge binaries into one fat framework
                    val needMerge = forceLegacy && (arm64 != null || modern != null) && (arm64_32 != null)

                    if (!needMerge) {
                        // If modern exists, prefer it. Else arm64/arm32.
                        modern ?: arm64 ?: arm32!!
                    } else {
                        val tempFramework = buildDir.resolve("atlas/tmp-fat-watch-device/$module.framework")
                        if (tempFramework.exists()) tempFramework.deleteRecursively()
                        tempFramework.parentFile.mkdirs()

                        // Copy one of them as the base (prefer modern, else arm64)
                        val baseToCopy = modern ?: arm64!!
                        baseToCopy.copyRecursively(tempFramework, overwrite = true)

                        val mergedBin = frameworkBinary(tempFramework)
                        val a = (modern ?: arm64!!).let(::frameworkBinary)
                        val b = frameworkBinary(arm64_32!!)

                        val out = ByteArrayOutputStream()
                        val err = ByteArrayOutputStream()

                        mergeOrCopyBinaries(
                            execOps = execOps,
                            mergedBin = mergedBin,
                            inputs = listOfNotNull(a, b),
                            lipoOut = out,
                            lipoErr = err
                        )

                        logger.lifecycle("📦 Prepared fat watch device framework (arm64 + arm64_32) → ${tempFramework.absolutePath}")
                        tempFramework
                    }
                }
            }

            !isWatch && isSim -> {
                // iOS simulator
                binFrameworkDir("iosSimulatorArm64").takeIf { it.exists() } ?: missingOrFail(
                    "No iOS simulator framework found",
                    lookedFor = listOf(binFrameworkDir("iosSimulatorArm64"))
                )
            }

            else -> {
                // iOS device
                binFrameworkDir("iosArm64").takeIf { it.exists() } ?: missingOrFail(
                    "No iOS device framework found",
                    lookedFor = listOf(binFrameworkDir("iosArm64"))
                )
            }
        }

        // -------------------------
        // 2) Copy to STABLE compile-time dir (your own stable cache)
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
        // 3) Copy to BUILT_PRODUCTS_DIR for Swift compile-time import
        //    (THIS is what removes the need for OTHER_SWIFT_FLAGS)
        // -------------------------

        val builtProductsDir = File(targetBuildDir.get())
        if (!builtProductsDir.exists()) builtProductsDir.mkdirs()

        val compileTimeFramework = builtProductsDir.resolve("$module.framework")
        if (compileTimeFramework.exists()) compileTimeFramework.deleteRecursively()
        stableFramework.copyRecursively(compileTimeFramework, overwrite = true)

        logger.lifecycle("✅ Copied for Swift compile-time (-F BUILT_PRODUCTS_DIR): ${compileTimeFramework.absolutePath}")

        // -------------------------
        // 4) Embed into app bundle frameworks folder (runtime packaging)
        // -------------------------

        val fwFolder = frameworksFolderPath.get().ifBlank { "Frameworks" }
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

        logger.lifecycle("✅ Embedded raw KMP framework: ${runtimeFramework.absolutePath}")
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
                    commandLine("xcrun", "lipo", "-info", mergedBin.absolutePath)
                }
            }
        }
    }
}