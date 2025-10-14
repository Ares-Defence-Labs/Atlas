package com.architect.engine.helpers

import com.architect.atlas.common.helpers.AppleProjectFinder
import com.architect.atlas.common.helpers.AppleProjectFinder.iosApps
import com.architect.atlas.common.helpers.AppleProjectFinder.isWatchBuildNow
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.engine.tasks.AtlasXCodeIncrementalBuildTask
import com.architect.engine.tasks.CheckGraphInputChangesTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    private val appleTasks = listOf(
        "xcodeAppleIncrementalBuildTask",
        "checkXcFrameworkChanges"
    )

    private const val allModulesVerifier = "checkHashAllModulesChanges"
    fun getIncrementalVerifierForAllModulesTask(project: Project): TaskProvider<CheckGraphInputChangesTask> {
        val androidModule = ProjectFinder.findAndroidClientApp(project)
        val coreModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val genTask = project.tasks.register(
            allModulesVerifier,
            CheckGraphInputChangesTask::class.java
        ) {
            sourceDirs.from(
                // other modules need to be added (Browser, Compose for JVM targets as well)
                project.fileTree("$coreModuleName/src/commonMain"),
                project.fileTree("$coreModuleName/src/androidMain"),
                project.fileTree("$coreModuleName/src/iosMain"),
                project.fileTree("$coreModuleName/src/watchosMain"),
                project.fileTree("${androidModule?.rootDir}/src/main"),
            )

            hashOutputFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }

        return genTask
    }

    fun resolveBuildTypeFromRequestedTasks(project: Project): String {
        val requested = project.gradle.startParameter.taskNames.joinToString(" ").lowercase()
        return when {
            "debug" in requested -> "Debug"
            "release" in requested -> "Release"
            else -> if (ProjectFinder.isDebugMode(project)) "Debug" else "Release"
        }
    }

    fun findExistingTask(project: Project, vararg names: String): String? =
        names.firstOrNull { project.tasks.findByName(it) != null }

    fun getIncrementalBuilderTask(project: Project): TaskProvider<AtlasXCodeIncrementalBuildTask> {
        val isRunningAppleWatch = project.isWatchBuildNow()
        val sharedModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()
        val appleModuleName = project.findProperty("atlas.iOSModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val forceCaching = project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull() ?: false
        val caching = ProjectFinder.isDebugMode(project) || forceCaching

        val iosProject = AppleProjectFinder.findAllXcodeTargets(project.rootDir, project.logger).iosApps().firstOrNull()

        // Prefer deriving from requested tasks; fall back to forceCaching/debug-mode
        val buildType = resolveBuildTypeFromRequestedTasks(project)

        val isSimulator = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator", ignoreCase = true) == true

        // Accept both historic and new target name variants
        val candidates = buildList {
            if (isRunningAppleWatch) {
                if (isSimulator) {
                    add("link${buildType}FrameworkWatchosSimulatorArm64")
                } else {
                    add("link${buildType}FrameworkWatchosDeviceArm64") // older naming
                    add("link${buildType}FrameworkWatchosArm64")       // newer naming
                }
            } else {
                if (isSimulator) {
                    add("link${buildType}FrameworkIosSimulatorArm64")
                } else {
                    add("link${buildType}FrameworkIosArm64")
                }
            }
        }

        val taskNameDependency = candidates.firstOrNull { project.tasks.findByName(it) != null }
            ?: error("None of the expected Kotlin/Native link tasks exist: $candidates")

        val generateDependencyGraphTask = project.tasks.register(
            appleTasks.first(), AtlasXCodeIncrementalBuildTask::class.java
        ) {
            dependsOn(taskNameDependency)
            moduleName.set(sharedModuleName)
            runningAppleWatchLegacy.set(taskNameDependency.endsWith("FrameworkWatchosArm64"))
            cacheXCFramework.set(caching)
            runningAppleWatch.set(isRunningAppleWatch)
            xcFrameworkOutputPath.set(
                iosProject?.containerDir?.resolve("XCFrameworks/${sharedModuleName}.xcframework")?.absolutePath
            )
            projectBuildDir.set(project.layout.buildDirectory)
            projectRootDir.set(project.rootDir)
            hashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            xchashFile.set(project.layout.buildDirectory.file("atlas/xcgraphInputHash.txt"))
        }

        return generateDependencyGraphTask
    }
}

