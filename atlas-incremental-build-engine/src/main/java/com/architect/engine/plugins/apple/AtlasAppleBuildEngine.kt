package com.architect.engine.plugins.apple

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.engine.tasks.AtlasXCCheckIfRequiredRebuildTask
import com.architect.engine.tasks.AtlasXCodeIncrementalBuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasAppleBuildEngine : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val appleModuleName = project.findProperty("atlas.coreModuleName")?.toString()
                ?: project.getSwiftImportModuleName()

            val forceCaching =
                project.findProperty("atlas.forceCaching")?.toString()?.toBooleanStrictOrNull()
                    ?: false

            val debugTasksXCFramework = listOf(
                "linkDebugFrameworkIosArm64",
                "linkDebugFrameworkIosSimulatorArm64"
            )

            val releaseTasksXCFramework = listOf(
                "linkReleaseFrameworkIosArm64",
                "linkReleaseFrameworkIosSimulatorArm64"
            )

            val caching = ProjectFinder.isDebugMode(project) || forceCaching
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
            // for xcframework generation (this will depend these tasks + the above)

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
            project.tasks.register("checkXcFrameworkChanges", AtlasXCCheckIfRequiredRebuildTask::class)

            project.tasks.register("").configure {
                dependsOn(generateDependencyGraphTask)
            }

            TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask.get())
        }
    }
}