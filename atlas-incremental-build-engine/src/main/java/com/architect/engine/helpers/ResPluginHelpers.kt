package com.architect.engine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.engine.tasks.AtlasXCCheckIfRequiredRebuildTask
import com.architect.engine.tasks.AtlasXCodeIncrementalBuildTask
import com.architect.engine.tasks.CheckGraphInputChangesTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    private val appleTasks = listOf(
        "xcodeAppleIncrementalBuildTask",
        "checkXcFrameworkChanges"
    )

    const val allModulesVerifier = "checkHashAllModulesChanges"

    fun getIncrementalVerifierForAllModulesTask(project: Project): TaskProvider<CheckGraphInputChangesTask> {
        val appleModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()
        val forceCaching =
            project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull()
                ?: false

        val caching = ProjectFinder.isDebugMode(project) || forceCaching
        val genTask = project.tasks.register(
            allModulesVerifier,
            CheckGraphInputChangesTask::class.java
        ) {
            sourceDirs.from(
                rootProject.fileTree("shared/src/commonMain"),
                rootProject.fileTree("shared/src/androidMain"),
                rootProject.fileTree("shared/src/iosMain"),
                rootProject.fileTree("clientApp/src/main"), // or wherever your app code lives
                rootProject.fileTree("anotherModule/src")
            )

            hashOutputFile.set(layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }

        return genTask
    }

    fun getIncrementalXCVerifierTask(project: Project): TaskProvider<AtlasXCCheckIfRequiredRebuildTask> {
        val appleModuleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()
        val forceCaching =
            project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull()
                ?: false

        val caching = ProjectFinder.isDebugMode(project) || forceCaching
        val genTask = project.tasks.register(
            appleTasks[1],
            AtlasXCCheckIfRequiredRebuildTask::class.java
        ) {
            commonMainSource.set(
                project.layout.projectDirectory.dir("src/commonMain/kotlin")
            )
            iosMainSource.set(
                project.layout.projectDirectory.dir("src/iosMain/kotlin")
            )

            cacheXCFramework.set(caching)
            xcFrameworkOutputPath.set(project.buildDir.resolve("XCFrameworks/${appleModuleName}.xcframework").absolutePath)
            projectBuildDir.set(project.buildDir)
            hashFile.set(project.layout.buildDirectory.file("xcframeworkInputsHash.txt"))
            flagFile.set(project.layout.buildDirectory.file("shouldRebuildXC.txt"))
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
        val debugTasksXCFramework = listOf(
            "linkDebugFrameworkIosArm64",
            "linkDebugFrameworkIosSimulatorArm64"
        )

        val releaseTasksXCFramework = listOf(
            "linkReleaseFrameworkIosArm64",
            "linkReleaseFrameworkIosSimulatorArm64"
        )

        val isSimulator = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
        val buildType = if (forceCaching) "debug" else "release"
        val linkTaskName = if (isSimulator) {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosSimulatorArm64"
        } else {
            "link${buildType.replaceFirstChar { it.uppercase() }}FrameworkIosArm64"
        }

        val iosProject = ProjectFinder.findIosClientApp(project)
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
            hashFile.set(project.layout.buildDirectory.file("xcframeworkInputsHash.txt"))
            flagFile.set(project.layout.buildDirectory.file("shouldRebuildXC.txt"))
        }

        return generateDependencyGraphTask
    }
}

