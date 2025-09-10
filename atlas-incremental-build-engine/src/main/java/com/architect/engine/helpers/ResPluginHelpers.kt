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

    fun getIncrementalBuilderTask(project: Project): TaskProvider<AtlasXCodeIncrementalBuildTask> {
        val isRunningAppleWatch = project.isWatchBuildNow()
        val sharedModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val appleModuleName = project.findProperty("atlas.iOSModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val forceCaching =
            project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull()
                ?: false

        val caching = ProjectFinder.isDebugMode(project) || forceCaching
        val iosProject =
            AppleProjectFinder.findAllXcodeTargets(project.rootDir, project.logger).iosApps()
                .firstOrNull()
        val debugTasksXCFramework =
            if (isRunningAppleWatch)
                listOf(
                    "linkDebugFrameworkWatchosArm32",
                    "linkDebugFrameworkWatchosArm64",
                    "linkDebugFrameworkWatchosSimulatorArm64"
                )
            else
                listOf(
                    "linkDebugFrameworkIosArm64",
                    "linkDebugFrameworkIosSimulatorArm64"
                )

        val releaseTasksXCFramework =
            if (isRunningAppleWatch) // generates the fat framework for apple watch
                listOf(
                    "linkReleaseFrameworkWatchosArm64",
                    "linkReleaseFrameworkWatchosSimulatorArm64"
                )
            else listOf(
                "linkReleaseFrameworkIosArm64",
                "linkReleaseFrameworkIosSimulatorArm64"
            )

        // check if running on apple watch, then add the string keys
        val isSimulator =
            System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (forceCaching) "debug" else "release"
        val linkTaskName = if (isSimulator) {
            if (isRunningAppleWatch)
                "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkWatchosSimulatorArm64"
            else "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosSimulatorArm64"
        } else {
            if (isRunningAppleWatch)
                "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkWatchosArm64"
            else
                "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosArm64"
        }

        val taskNameDependency =
            if (forceCaching) debugTasksXCFramework.single { it == linkTaskName } else releaseTasksXCFramework.single { it == linkTaskName }

        val generateDependencyGraphTask = project.tasks.register(
            appleTasks.first(),
            AtlasXCodeIncrementalBuildTask::class.java
        ) {
            dependsOn(taskNameDependency)
            moduleName.set(sharedModuleName)
            cacheXCFramework.set(caching)

            runningAppleWatch.set(isRunningAppleWatch)
            xcFrameworkOutputPath.set(iosProject?.containerDir?.resolve("XCFrameworks/${appleModuleName}.xcframework")?.absolutePath)
            projectBuildDir.set(project.layout.buildDirectory)
            projectRootDir.set(project.rootDir)
            hashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            xchashFile.set(project.layout.buildDirectory.file("atlas/xcgraphInputHash.txt"))
        }

        return generateDependencyGraphTask
    }
}

