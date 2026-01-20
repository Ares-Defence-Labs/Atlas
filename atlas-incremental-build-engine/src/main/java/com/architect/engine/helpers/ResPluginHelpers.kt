// ResPluginHelpers.kt (new full implementation for the builder task registration)
package com.architect.engine.helpers

import com.architect.atlas.common.helpers.AppleProjectFinder.isWatchBuildNow
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.engine.tasks.AtlasEmbedKmpFrameworkForXcodeTask
import com.architect.engine.tasks.CheckGraphInputChangesTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {

    private const val allModulesVerifier = "checkHashAllModulesChanges"

    fun getIncrementalVerifierForAllModulesTask(project: Project): TaskProvider<CheckGraphInputChangesTask> {
        val androidModule = ProjectFinder.findAndroidClientApp(project)
        val coreModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        return project.tasks.register(
            allModulesVerifier,
            CheckGraphInputChangesTask::class.java
        ) {
            sourceDirs.from(
                project.fileTree("$coreModuleName/src/commonMain"),
                project.fileTree("$coreModuleName/src/androidMain"),
                project.fileTree("$coreModuleName/src/iosMain"),
                project.fileTree("$coreModuleName/src/watchosMain"),
                project.fileTree("${androidModule?.rootDir}/src/main"),
            )
            hashOutputFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }
    }

    fun resolveBuildTypeFromRequestedTasks(project: Project): String {
        val xcodeCfg = System.getenv("CONFIGURATION")?.trim().orEmpty()
        if (xcodeCfg.equals("Debug", ignoreCase = true)) return "Debug"
        if (xcodeCfg.equals("Release", ignoreCase = true)) return "Release"

        val action = System.getenv("ACTION")?.trim().orEmpty()
        val archiveAction = System.getenv("ARCHIVE_ACTION")?.trim().orEmpty()
        val isArchive = action.equals("install", ignoreCase = true) || archiveAction.equals("install", ignoreCase = true)
        if (isArchive) return "Release"

        val forceRelease = project.findProperty("atlas.forceRelease")?.toString()?.toBooleanStrictOrNull() ?: false
        if (forceRelease) return "Release"

        val forceDebug = project.findProperty("atlas.forceDebug")?.toString()?.toBooleanStrictOrNull() ?: false
        if (forceDebug) return "Debug"

        val requested = project.gradle.startParameter.taskNames.joinToString(" ").lowercase()
        return when {
            "debug" in requested -> "Debug"
            "release" in requested -> "Release"
            else -> if (ProjectFinder.isDebugMode(project)) "Debug" else "Release"
        }
    }

    fun getIncrementalBuilderTask(project: Project): TaskProvider<AtlasEmbedKmpFrameworkForXcodeTask> {
        val isRunningAppleWatch =
            project.isWatchBuildNow() || (project.findProperty("atlas.forceAppleWatch")?.toString()
                ?.toBooleanStrictOrNull() ?: false)

        val forceLegacy =
            project.findProperty("atlas.forceLegacy")?.toString()
                ?.toBooleanStrictOrNull() ?: false

        val sharedModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        project.logger.lifecycle("AppleWatch is Connected -- $isRunningAppleWatch")
        project.logger.lifecycle("atlas.forceLegacy -- $forceLegacy")

        val buildType = resolveBuildTypeFromRequestedTasks(project) // "Debug"/"Release"

        val isSimulator = (System.getenv("EFFECTIVE_PLATFORM_NAME") ?: "")
            .contains("simulator", ignoreCase = true)

        fun firstExistingTask(names: List<String>): String? =
            names.firstNotNullOfOrNull { name ->
                project.tasks.findByName(name)?.also {
                    project.logger.lifecycle("AtlasEmbedKmpFrameworkForXcodeTask will dependOn -> $name")
                }?.name
            }

        val csdkName = System.getenv("SDK_NAME") ?: ""
        val buildingWatch = csdkName.startsWith("watch", ignoreCase = true) || isRunningAppleWatch

        val deps = mutableListOf<Any>()

        if (buildingWatch) {
            if (isSimulator) {
                val watchSimTaskName = firstExistingTask(
                    listOf(
                        "link${buildType}FrameworkWatchosSimulatorArm64",
                        "link${buildType}FrameworkWatchosX64",
                    )
                )
                watchSimTaskName?.let { n -> project.tasks.findByName(n)?.let(deps::add) }
                    ?: project.logger.warn("⚠️ Watch simulator link task not found for buildType=$buildType")
            } else {
                project.tasks.findByName("link${buildType}FrameworkWatchosDeviceArm64")?.let(deps::add)
                val legacyNames = buildList {
                    add("link${buildType}FrameworkWatchosArm64")
                    if (forceLegacy) add("link${buildType}FrameworkWatchosArm64_32")
                    add("link${buildType}FrameworkWatchosArm32") // fallback
                    add("link${buildType}FrameworkWatchosFat")  // fallback
                }

                legacyNames.forEach { name ->
                    project.tasks.findByName(name)?.let {
                        project.logger.lifecycle("AtlasEmbedKmpFrameworkForXcodeTask will dependOn -> $name")
                        deps += it
                    }
                }
            }
        } else {
            val iosTaskName =
                if (isSimulator) "link${buildType}FrameworkIosSimulatorArm64"
                else "link${buildType}FrameworkIosArm64"

            project.tasks.findByName(iosTaskName)?.let(deps::add)
                ?: project.logger.warn("⚠️ iOS link task not found: $iosTaskName")
        }

        return project.tasks.register(
            "xcodeAppleEmbedFrameworkTask",
            AtlasEmbedKmpFrameworkForXcodeTask::class.java
        ) {
            setDependsOn(deps)

            moduleName.set(sharedModuleName)
            projectBuildDir.set(project.layout.buildDirectory)

            allowMissing.set(false)
            allowForceLegacy.set(forceLegacy)

            targetBuildDir.set(System.getenv("TARGET_BUILD_DIR") ?: "")
            configuration.set(System.getenv("CONFIGURATION") ?: buildType)
            sdkName.set(System.getenv("SDK_NAME") ?: "")
            effectivePlatformName.set(System.getenv("EFFECTIVE_PLATFORM_NAME") ?: "")
            archs.set(System.getenv("ARCHS") ?: "")
            frameworksFolderPath.set(System.getenv("FRAMEWORKS_FOLDER_PATH") ?: "Frameworks")
            codeSigningAllowed.set(System.getenv("CODE_SIGNING_ALLOWED") ?: "NO")
            expandedCodeSignIdentity.set(System.getenv("EXPANDED_CODE_SIGN_IDENTITY") ?: "")
        }
    }
}

//
//#!/bin/bash
//set -euo pipefail
//
//echo "========== Atlas KMP (watch) Prepare + Embed =========="
//echo "SRCROOT=$SRCROOT"
//echo "CONFIGURATION=${CONFIGURATION:-}"
//echo "ACTION=${ACTION:-}"
//echo "SDK_NAME=${SDK_NAME:-}"
//echo "EFFECTIVE_PLATFORM_NAME=${EFFECTIVE_PLATFORM_NAME:-}"
//echo "ARCHS=${ARCHS:-}"
//echo "TARGET_NAME=${TARGET_NAME:-}"
//echo "BUILT_PRODUCTS_DIR=${BUILT_PRODUCTS_DIR:-}"
//echo "TARGET_BUILD_DIR=${TARGET_BUILD_DIR:-}"
//echo "WRAPPER_NAME=${WRAPPER_NAME:-}"
//echo "======================================================="
//
//cd "$SRCROOT/.."
//
//CFG="${CONFIGURATION:-Debug}"
//if [[ "${ACTION:-}" == "install" ]]; then
//CFG="Release"
//fi
//echo "Resolved CFG=$CFG"
//
//./gradlew :shared:xcodeAppleEmbedFrameworkTask \
//-Patlas.forceAppleWatch=true \
//-Patlas.forceLegacy=true \
//-Patlas.coreModuleName=shared
//
//STABLE_DEVICE_DIR="$SRCROOT/../shared/build/atlas/xcode-frameworks/watchos-device/$CFG"
//STABLE_SIM_DIR="$SRCROOT/../shared/build/atlas/xcode-frameworks/watchos-simulator/$CFG"
//
//echo "Stable device dir: $STABLE_DEVICE_DIR"
//echo "Stable sim dir   : $STABLE_SIM_DIR"
//
//IS_SIM=false
//if [[ "${EFFECTIVE_PLATFORM_NAME:-}" == *"simulator"* ]]; then
//IS_SIM=true
//fi
//
//SRC_FRAMEWORK=""
//if $IS_SIM; then
//SRC_FRAMEWORK="$STABLE_SIM_DIR/shared.framework"
//else
//SRC_FRAMEWORK="$STABLE_DEVICE_DIR/shared.framework"
//fi
//
//echo "Resolved SRC_FRAMEWORK=$SRC_FRAMEWORK"
//
//if [[ -f "$SRC_FRAMEWORK/Modules/module.modulemap" ]]; then
//echo "✅ modulemap ok"
//else
//echo "❌ shared.framework module.modulemap not found at: $SRC_FRAMEWORK/Modules/module.modulemap"
//echo "Listing:"
//ls -la "$SRC_FRAMEWORK/Modules" || true
//exit 1
//fi
//
//echo ""
//echo "Expected Swift compile flags (if using OTHER_SWIFT_FLAGS):"
//echo "  -F$STABLE_DEVICE_DIR"
//echo "  -F$STABLE_SIM_DIR"
//echo ""
//
//if [[ -n "${OTHER_SWIFT_FLAGS:-}" ]]; then
//echo "OTHER_SWIFT_FLAGS=$OTHER_SWIFT_FLAGS"
//else
//echo "ℹ️ OTHER_SWIFT_FLAGS env var not provided by Xcode here."
//echo "   That’s OK — we will stage the framework into BUILT_PRODUCTS_DIR, which Swift already uses via -F."
//fi
//
//if [[ -z "${BUILT_PRODUCTS_DIR:-}" ]]; then
//echo "❌ BUILT_PRODUCTS_DIR is empty. Cannot stage framework for Swift compile."
//exit 1
//fi
//
//echo ""
//echo "➡️ Staging shared.framework into BUILT_PRODUCTS_DIR for Swift compile:"
//echo "   $SRC_FRAMEWORK -> $BUILT_PRODUCTS_DIR/shared.framework"
//
//rm -rf "$BUILT_PRODUCTS_DIR/shared.framework"
//mkdir -p "$BUILT_PRODUCTS_DIR"
//ditto "$SRC_FRAMEWORK" "$BUILT_PRODUCTS_DIR/shared.framework"
//
//if [[ ! -f "$BUILT_PRODUCTS_DIR/shared.framework/Modules/module.modulemap" ]]; then
//echo "❌ Staged framework missing modulemap (unexpected)"
//exit 1
//fi
//echo "✅ Staged OK: $BUILT_PRODUCTS_DIR/shared.framework"
//
//if [[ -n "${TARGET_BUILD_DIR:-}" && -n "${WRAPPER_NAME:-}" ]]; then
//DEST_APP_FRAMEWORKS="$TARGET_BUILD_DIR/$WRAPPER_NAME/Frameworks"
//echo ""
//echo "➡️ Embedding into app bundle (optional runtime step):"
//echo "   $SRC_FRAMEWORK -> $DEST_APP_FRAMEWORKS/shared.framework"
//mkdir -p "$DEST_APP_FRAMEWORKS"
//rm -rf "$DEST_APP_FRAMEWORKS/shared.framework"
//ditto "$SRC_FRAMEWORK" "$DEST_APP_FRAMEWORKS/shared.framework"
//echo "✅ Embedded into app bundle"
//fi
//
//echo "✅ Atlas script completed."
