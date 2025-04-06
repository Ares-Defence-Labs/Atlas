package com.architect.atlasResGen.plugins

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.SourceSetsHelper
import com.architect.atlas.common.helpers.TaskDefinitions
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.ResPluginHelpers
import com.architect.atlasResGen.tasks.strings.AtlasStringPluginTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure

class AtlasResourceGenPlugin : Plugin<Project> {
    val graphGen = "generateDependencyGraph"
    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        val dependencyGraphTask = project.rootProject.allprojects
            .flatMap { it.tasks }
            .firstOrNull { it.name.equals(graphGen, ignoreCase = true) }

        if (dependencyGraphTask != null) {
            project.logger.lifecycle("✅ Found and linking to task: ${dependencyGraphTask.path}")
            generateDependencyGraphTask.dependsOn(dependencyGraphTask)
            generateDependencyGraphTask.mustRunAfter(dependencyGraphTask)
        }

        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            mustRunAfter(generateDependencyGraphTask)
        }
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "android"
            )
        }.configureEach {
            mustRunAfter(generateDependencyGraphTask)
        }

        project.tasks.matching {
            it.name in listOf("preBuild", "assemble", "build")
        }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        project.tasks.named("generateDependencyGraph").configure {
            mustRunAfter("debugAssetsCopyForAGP", "prepareLintJarForPublish")
        }

        val requiredTasks = TaskDefinitions.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                dependencyTask.dependsOn(generateDependencyGraphTask)
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }
    }

    override fun apply(project: Project) {
        // configure src files (build files for all tasks)
        val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)

        val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)
        generateColorsResources.configure { dependsOn(generateStringsResources) }

        if (!ProjectFinder.isBuildingForIos(project)) { // iOS
            project.logger.lifecycle("BUILDING FOR IOS")
            val generateAtlasXCAssetFileResources =
                AppleResPluginHelpers.getAtlasXCAssetFilePackagingTask(project)
            val generateAppleFontFiles =
                AppleResPluginHelpers.getAppleFontsResourceTask(project)

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

            taskOrderConfig(project, generateStringsResources.get())
            taskOrderConfig(project, generateAtlasXCAssetFileResources.get())
        } else {
//            project.logger.lifecycle("BUILDING FOR ANDROID")
//            val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)
//            val generateFontsResources = ResPluginHelpers.getFontsResourceTask(project)
//
//            generateImagesResources.configure { dependsOn(generateColorsResources) }
//            generateFontsResources.configure { dependsOn(generateImagesResources) }
//
//            project.tasks.matching {
//                it.name.contains(
//                    "compileKotlin",
//                    ignoreCase = true
//                ) || (it.name.contains("compile") && it.name.contains("kotlin") && it.name.contains(
//                    "android", ignoreCase = true
//                ))
//            }.configureEach {
//                dependsOn(
//                    generateStringsResources,
//                    generateColorsResources,
//                    generateImagesResources,
//                    generateFontsResources
//                )
//            }
//
//            processRegisterTaskDependencies(
//                project,
//                TaskDefinitions.getiOSTaskDependencies(),
//                generateStringsResources.get(),
//                generateColorsResources.get(),
//                generateImagesResources.get(),
//                generateFontsResources.get(),
//            )
//
//            //extra tasks
//            processRegisterTaskDependencies(
//                project,
//                TaskDefinitions.getExtraTaskDependencies(),
//                generateStringsResources.get(),
//                generateColorsResources.get(),
//                generateImagesResources.get(),
//                generateFontsResources.get(),
//            )
//
//            val androidTasks = TaskDefinitions.getAndroidTaskDependencies(project)
//            project.afterEvaluate {
//                TaskMngrHelpers.attachDependenciesToGraph(
//                    project,
//                    generateStringsResources.get(),
//                    androidTasks
//                )
//                TaskMngrHelpers.attachDependenciesToGraph(
//                    project,
//                    generateColorsResources.get(),
//                    androidTasks
//                )
//
//                TaskMngrHelpers.attachDependenciesToGraph(
//                    project,
//                    generateImagesResources.get(),
//                    androidTasks
//                )
//                TaskMngrHelpers.attachDependenciesToGraph(
//                    project,
//                    generateFontsResources.get(),
//                    androidTasks
//                )
//            }
        }

//        project.afterEvaluate {
//            SourceSetsHelper.prepareResourcesDirectory(project)
//            TaskMngrHelpers.configureBuildFolders(project)
//            TaskMngrHelpers.platformClientsBuildFolders(project)
//        }
    }
}