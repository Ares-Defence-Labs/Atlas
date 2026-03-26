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
    abstract val configurationBuildDir: Property<String>

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

    private fun missing(message: String): Nothing {
        throw GradleException(message)
    }


    private fun prepareWatchDeviceFramework(
        buildDir: File,
        module: String,
        buildTypeDir: String,
        execOps: ExecOperations
    ): File {

        fun binFrameworkDir(target: String) =
            buildDir.resolve("bin/$target/$buildTypeDir/$module.framework")

        fun frameworkBinary(frameworkDir: File) =
            frameworkDir.resolve(module)

        val deviceArm64Dir   = binFrameworkDir("watchosDeviceArm64")  // arm64 (Series 9+)
        val deviceArm64_32Dir = binFrameworkDir("watchosArm64")       // arm64_32 (Series 7–8)

        val arm64Framework   = deviceArm64Dir.takeIf { it.exists() }
        val arm64_32Framework = deviceArm64_32Dir.takeIf { it.exists() }

        if (arm64Framework == null && arm64_32Framework == null) {
            error(
                """
            ❌ No watchOS device frameworks found.
            Looked for:
             - ${deviceArm64Dir.absolutePath}
             - ${deviceArm64_32Dir.absolutePath}
            """.trimIndent()
            )
        }

        // If both exist → build FAT framework
        if (arm64Framework != null && arm64_32Framework != null) {

            val fatFrameworkDir =
                buildDir.resolve("atlas/tmp-fat-watch-device/$module.framework")

            if (fatFrameworkDir.exists()) fatFrameworkDir.deleteRecursively()
            fatFrameworkDir.parentFile.mkdirs()

            // Copy skeleton (resources, modulemap, headers, etc)
            arm64_32Framework.copyRecursively(fatFrameworkDir, overwrite = true)

            val mergedBinary = frameworkBinary(fatFrameworkDir)
            val bin32 = frameworkBinary(arm64_32Framework)
            val bin64 = frameworkBinary(arm64Framework)

            logger.lifecycle("🔧 Creating FAT watchOS framework")
            logger.lifecycle("   arm64_32: ${bin32.absolutePath}")
            logger.lifecycle("   arm64   : ${bin64.absolutePath}")

            execOps.exec {
                commandLine(
                    "xcrun", "lipo",
                    "-create",
                    "-output", mergedBinary.absolutePath,
                    bin32.absolutePath,
                    bin64.absolutePath
                )
            }

            // Verify architectures
            execOps.exec {
                isIgnoreExitValue = true
                commandLine("xcrun", "lipo", "-info", mergedBinary.absolutePath)
            }

            logger.lifecycle("📦 FAT watch device framework ready → ${fatFrameworkDir.absolutePath}")

            return fatFrameworkDir
        }

        // Only one slice available (fallback)
        val chosen = arm64Framework ?: arm64_32Framework!!

        logger.lifecycle("ℹ️ Only one watchOS device slice found → using ${chosen.name}")

        return chosen
    }

    @TaskAction
    fun embedFrameworkForXcode() {

        val module = moduleName.get()
        val buildDir = projectBuildDir.get().asFile

        val cfg = configuration.get().ifBlank { "Debug" }
        val buildTypeDir = if (cfg.equals("Debug", true)) "debugFramework" else "releaseFramework"

        val sdk = sdkName.get().ifBlank { System.getenv("SDK_NAME") ?: "" }
        val eff = effectivePlatformName.get().ifBlank { System.getenv("EFFECTIVE_PLATFORM_NAME") ?: "" }
        val archsStr = archs.get().ifBlank { System.getenv("ARCHS") ?: "" }

        val isWatch = sdk.startsWith("watch", true)
        val isSim = eff.contains("simulator", true) || sdk.contains("simulator", true)
        val forceLegacy = allowForceLegacy.getOrElse(false)

        logger.lifecycle("🔎 SDK_NAME=$sdk (watch=$isWatch sim=$isSim)")
        logger.lifecycle("🔎 CONFIGURATION=$cfg ARCHS=$archsStr")

        fun binFrameworkDir(target: String) =
            buildDir.resolve("bin/$target/$buildTypeDir/$module.framework")

        fun binary(frameworkDir: File) = File(frameworkDir, module)
        fun frameworkBinary(frameworkDir: File) = File(frameworkDir, module)
        // ---------------------------------------------------
        // 1️⃣ Locate framework slice
        // ---------------------------------------------------

        val preparedFramework: File = when {
            isWatch && isSim -> {
                binFrameworkDir("watchosSimulatorArm64")
                    .takeIf { it.exists() }
                    ?: binFrameworkDir("watchosX64")
                    ?: missing("watchOS simulator framework missing")
            }

            isWatch && !isSim -> {
                prepareWatchDeviceFramework(
                    buildDir = buildDir,
                    module = module,
                    buildTypeDir = buildTypeDir,
                    execOps = execOps
                )
            }

            !isWatch && isSim -> {
                binFrameworkDir("iosSimulatorArm64")
                    .takeIf { it.exists() }
                    ?: missing("iOS simulator framework missing")
            }

            else -> {
                binFrameworkDir("iosArm64")
                    .takeIf { it.exists() }
                    ?: missing("iOS device framework missing")
            }
        }

        // ---------------------------------------------------
        // 2️⃣ Copy to stable cache
        // ---------------------------------------------------

        val stablePlatform = when {
            isWatch && isSim -> "watchos-simulator"
            isWatch -> "watchos-device"
            !isWatch && isSim -> "ios-simulator"
            else -> "ios-device"
        }

        val stableDir = buildDir.resolve("atlas/xcode-frameworks/$stablePlatform/$cfg")
        val stableFramework = stableDir.resolve("$module.framework")

        stableDir.mkdirs()
        if (stableFramework.exists()) stableFramework.deleteRecursively()
        preparedFramework.copyRecursively(stableFramework, true)

        require(stableFramework.resolve("Modules/module.modulemap").exists()) {
            "module.modulemap missing in ${stableFramework.absolutePath}"
        }

        logger.lifecycle("✅ Stable framework ready")

        // ---------------------------------------------------
        // 3️⃣ Compile-time staging (SAFE)
        // ---------------------------------------------------

        val compileRoot = configurationBuildDir.orNull?.takeIf { it.isNotBlank() }
            ?: System.getenv("CONFIGURATION_BUILD_DIR")
            ?: buildDir.resolve("atlas/tmp-xcode-compile").absolutePath

        val compileProductsDir = File(compileRoot)

        if (looksLikeArchiveProductsApplications(compileProductsDir)) {
            throw GradleException(
                "Refusing to stage compile frameworks into Archive Products/Applications:\n" +
                        compileProductsDir.absolutePath
            )
        }

        val compileFrameworksDir = compileProductsDir.resolve("Frameworks")
        compileFrameworksDir.mkdirs()

        val compileFW = compileFrameworksDir.resolve("$module.framework")
        if (compileFW.exists()) compileFW.deleteRecursively()
        stableFramework.copyRecursively(compileFW, true)

        logger.lifecycle("✅ Compile-time staging OK → ${compileFW.absolutePath}")

        // ---------------------------------------------------
        // 4️⃣ Runtime embed inside .app / .appex
        // ---------------------------------------------------

        val runtimeRoot = targetBuildDir.orNull?.takeIf { it.isNotBlank() }
            ?: System.getenv("TARGET_BUILD_DIR")
            ?: return // nothing to embed

        val wrapper = wrapperName.orNull?.takeIf { it.isNotBlank() }
            ?: System.getenv("WRAPPER_NAME")
            ?: return

        val fwFolderRaw = frameworksFolderPath.orNull?.takeIf { it.isNotBlank() }
            ?: System.getenv("FRAMEWORKS_FOLDER_PATH")
            ?: "Frameworks"

        val fwFolder =
            if (fwFolderRaw.contains(".app/") || fwFolderRaw.contains(".appex/"))
                fwFolderRaw
            else
                "$wrapper/Frameworks"

        val runtimeFrameworksDir = File(runtimeRoot).resolve(fwFolder)
        runtimeFrameworksDir.mkdirs()

        val runtimeFW = runtimeFrameworksDir.resolve("$module.framework")
        if (runtimeFW.exists()) runtimeFW.deleteRecursively()
        stableFramework.copyRecursively(runtimeFW, true)

        logger.lifecycle("➡️ Embedded into bundle → ${runtimeFW.absolutePath}")

        // ---------------------------------------------------
        // 5️⃣ Codesign if required
        // ---------------------------------------------------

        maybeCodesign(runtimeFW)

        logger.lifecycle("✅ Atlas embed complete")
    }

    private fun looksLikeArchiveProductsApplications(dir: File): Boolean {
        val p = dir.absolutePath.replace('\\', '/')
        return p.endsWith("/Products/Applications") || p.contains("/InstallationBuildProductsLocation/Products/Applications")
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