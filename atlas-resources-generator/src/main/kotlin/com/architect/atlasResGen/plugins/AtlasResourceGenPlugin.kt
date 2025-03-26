package com.architect.atlasResGen.plugins

import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasResourceGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        ResPluginHelpers.prepareResourcesDirectory(project)

        val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)
        val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)
        val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)

        generateColorsResources.configure { dependsOn(generateStringsResources) }
        generateImagesResources.configure { dependsOn(generateColorsResources) }

        // specify all tasks for all required resource generators
        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(generateStringsResources, generateColorsResources, generateImagesResources)
        }

        // ✅ Ensure `generateDependencyGraph` runs before **Android Kotlin compilation**
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "Android"
            )
        }.configureEach {
            dependsOn(generateStringsResources, generateColorsResources, generateImagesResources)
        }

        val requiredTasks = ResPluginHelpers.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                generateStringsResources.configure { dependsOn(dependencyTask) }
                generateColorsResources.configure { dependsOn(dependencyTask) }
                generateImagesResources.configure { dependsOn(dependencyTask) }
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }

        val androidTasks = ResPluginHelpers.getAndroidTaskDependencies(project)
        if (project.state.executed) {
            // **Project is already evaluated, use taskGraph.whenReady**
            project.gradle.taskGraph.whenReady {
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
            }
        } else {
            // **Project is not yet evaluated, use `afterEvaluate`**
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
            }
        }
    }
}