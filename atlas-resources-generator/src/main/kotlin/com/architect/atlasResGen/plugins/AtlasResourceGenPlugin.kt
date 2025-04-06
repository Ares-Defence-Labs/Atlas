package com.architect.atlasResGen.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskDefinitions
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasResourceGenPlugin : Plugin<Project> {
    private fun processRegisterTaskDependencies(
        project: Project,
        requiredExtraTasks: List<String>,
        generateStringsResources: Task,
        generateColorsResources: Task,
        generateImagesResources: Task,
        generateFontsResources: Task,
        generateXCAssetResources: Task,
        generateAppleFontFiles: Task
    ) {
        requiredExtraTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                generateStringsResources.dependsOn(dependencyTask)
                generateColorsResources.dependsOn(dependencyTask)

                if (!ProjectFinder.isBuildingForIos(project)) {
                    generateImagesResources.dependsOn(dependencyTask)
                    generateFontsResources.dependsOn(dependencyTask)
                } else {
                    generateXCAssetResources.dependsOn(dependencyTask)
                    generateAppleFontFiles.dependsOn(dependencyTask)
                }
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }
    }

    override fun apply(project: Project) {
        if (ProjectFinder.isBuildingForIos(project)) { // iOS
            project.logger.lifecycle("RUNNING IOS")
        }

        // configure src files (build files for all tasks)
        ResPluginHelpers.prepareResourcesDirectory(project)

        val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)
        val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)

        // common main dependencies
        generateColorsResources.configure { dependsOn(generateStringsResources) }

        // android specific tasks
        val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)
        val generateFontsResources = ResPluginHelpers.getFontsResourceTask(project)

        generateImagesResources.configure { dependsOn(generateColorsResources) }
        generateFontsResources.configure { dependsOn(generateImagesResources) }

        // xcode tasks
        val generateAtlasXCAssetFileResources =
            AppleResPluginHelpers.getAtlasXCAssetFilePackagingTask(project)
        val generateAppleFontFiles =
            AppleResPluginHelpers.getAppleFontsResourceTask(project)

        if (ProjectFinder.isBuildingForIos(project)) { // iOS
            generateAppleFontFiles.configure {
                dependsOn(
                    generateColorsResources
                )
            } // apple specific

            generateAtlasXCAssetFileResources.configure {
                dependsOn(
                    generateAppleFontFiles
                )
            }
        }

        // specify all tasks for all required resource generators
        project.tasks.matching {
            it.name.contains(
                "compileKotlin",
                ignoreCase = true
            ) || (it.name.contains("compile") && it.name.contains("kotlin") && it.name.contains(
                "android", ignoreCase = true
            ))
        }.configureEach {
            dependsOn(
                generateStringsResources,
                generateColorsResources,
                generateImagesResources,
                generateFontsResources
            )

            if (ProjectFinder.isBuildingForIos(project)) {
                dependsOn(
                    generateAtlasXCAssetFileResources,
                    generateAppleFontFiles
                )
            }
        }

        // ios dependencies
        processRegisterTaskDependencies(
            project,
            TaskDefinitions.getiOSTaskDependencies(),
            generateStringsResources.get(),
            generateColorsResources.get(),
            generateImagesResources.get(),
            generateFontsResources.get(),
            generateAtlasXCAssetFileResources.get(),
            generateAppleFontFiles.get(),
        )

        //extra tasks
        processRegisterTaskDependencies(
            project,
            TaskDefinitions.getExtraTaskDependencies(),
            generateStringsResources.get(),
            generateColorsResources.get(),
            generateImagesResources.get(),
            generateFontsResources.get(),
            generateAtlasXCAssetFileResources.get(),
            generateAppleFontFiles.get(),
        )

        val androidTasks = TaskDefinitions.getAndroidTaskDependencies(project)
        project.afterEvaluate {
            ResPluginHelpers.attachDependenciesToGraph(
                project,
                generateStringsResources.get(),
                androidTasks
            )
            ResPluginHelpers.attachDependenciesToGraph(
                project,
                generateColorsResources.get(),
                androidTasks
            )

            ResPluginHelpers.attachDependenciesToGraph(
                project,
                generateImagesResources.get(),
                androidTasks
            )
            ResPluginHelpers.attachDependenciesToGraph(
                project,
                generateFontsResources.get(),
                androidTasks
            )

            TaskMngrHelpers.configureBuildFolders(project)
            TaskMngrHelpers.platformClientsBuildFolders(project)
        }
    }
}