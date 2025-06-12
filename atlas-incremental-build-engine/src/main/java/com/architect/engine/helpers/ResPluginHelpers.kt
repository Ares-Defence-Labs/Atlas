package com.architect.engine.helpers

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
                project.fileTree("${androidModule?.rootDir}/src/main"),
            )

            hashOutputFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }

        return genTask
    }

    fun getIncrementalBuilderTask(project: Project): TaskProvider<AtlasXCodeIncrementalBuildTask> {
        val sharedModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val appleModuleName = project.findProperty("atlas.iOSModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val forceCaching =
            project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull()
                ?: false

        val caching = ProjectFinder.isDebugMode(project) || forceCaching
        val iosProject = ProjectFinder.findIosClientApp(project)
        val debugTasksXCFramework = listOf(
            "linkDebugFrameworkIosArm64",
            "linkDebugFrameworkIosSimulatorArm64"
        )

        val releaseTasksXCFramework = listOf(
            "linkReleaseFrameworkIosArm64",
            "linkReleaseFrameworkIosSimulatorArm64"
        )

        val isSimulator =
            System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (forceCaching) "debug" else "release"
        val linkTaskName = if (isSimulator) {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosSimulatorArm64"
        } else {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosArm64"
        }

        val taskNameDependency =
            if (forceCaching) debugTasksXCFramework.single { it == linkTaskName } else releaseTasksXCFramework.single { it == linkTaskName }


        val generateDependencyGraphTask = project.tasks.register(
            appleTasks.first(),
            AtlasXCodeIncrementalBuildTask::class.java
        ) {
            dependsOn(taskNameDependency)
            commonMainSource.set(
                project.layout.projectDirectory.dir("src/commonMain/kotlin")
            )
            iosMainSource.set(
                project.layout.projectDirectory.dir("src/iosMain/kotlin")
            )

            moduleName.set(sharedModuleName)
            cacheXCFramework.set(caching)

            xcFrameworkOutputPath.set(iosProject?.resolve("XCFrameworks/${appleModuleName}.xcframework")?.absolutePath)
            projectBuildDir.set(project.buildDir)
            projectRootDir.set(project.rootDir)
            hashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            xchashFile.set(project.layout.buildDirectory.file("atlas/xcgraphInputHash.txt"))
        }

        return generateDependencyGraphTask
    }
}

