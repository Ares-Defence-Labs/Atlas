package com.architect.engine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.engine.tasks.AtlasXCodeIncrementalBuildTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    val appleTasks = listOf(
        "xcodeAppleIncrementalBuildTask",
        "checkXcFrameworkChanges"
    )

    fun getIncrementalBuilderTask(project: Project): TaskProvider<AtlasXCodeIncrementalBuildTask> {
        val appleModuleName = project.findProperty("atlas.coreModuleName")?.toString()
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
        val generateDependencyGraphTask = project.tasks.register(
            "xcodeAppleIncrementalBuildTask",
            AtlasXCodeIncrementalBuildTask::class.java
        ) {
            dependsOn("linkDebugFrameworkIosArm64", "linkDebugFrameworkIosSimulatorArm64")
            onlyIf {
                project.buildDir.resolve("shouldRebuildXC.txt").readText().trim() == "true"
            }

            commonMainSource.set(
                project.layout.projectDirectory.dir("src/commonMain/kotlin")
            )
            iosMainSource.set(
                project.layout.projectDirectory.dir("src/iosMain/kotlin")
            )

            moduleName.set(appleModuleName)
            cacheXCFramework.set(caching)

            xcFrameworkOutputPath.set(project.buildDir.resolve("XCFrameworks/${moduleName.get()}.xcframework").absolutePath)
            projectBuildDir.set(project.buildDir)
            projectRootDir.set(project.rootDir)
            isiOS.set(ProjectFinder.isBuildingForIos(project))
        }

        listOf(
            "generateDependencyGraph",
            "generateNavAtlasEngine",
            "generateSwiftUIAtlasEngine",
            "generateAtlasStringsGraph",
            "generateAtlasColorsGraph",
            "generateAtlasImagesGraph",
            "generateAtlasFontsGraph"
        ).forEach { taskName ->
            if (project.tasks.findByName(taskName) != null) {
                generateDependencyGraphTask.configure {
                    dependsOn(taskName)
                }
            }
        }

        return generateDependencyGraphTask
    }
}
