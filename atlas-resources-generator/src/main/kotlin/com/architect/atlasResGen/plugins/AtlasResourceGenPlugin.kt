package com.architect.atlasResGen.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasResourceGenPlugin : Plugin<Project> {
    private fun configureBuildFolders(project: Project) {
        val kmpExt =
            project.extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
                ?: error("Kotlin Multiplatform plugin not applied")

        // android main
        val androidMain = kmpExt.sourceSets.getByName("androidMain")
        androidMain.kotlin.srcDirs(
            project.layout.buildDirectory.dir("generated/androidMain/kotlin").get().asFile,
            project.layout.buildDirectory.dir("generated/androidMain/resources").get().asFile
        )

        val iosMain = kmpExt.sourceSets.getByName("iosMain")
        iosMain.kotlin.srcDirs(
            project.layout.buildDirectory.dir("generated/iosMain/kotlin").get().asFile,
            project.layout.buildDirectory.dir("generated/iosMain/resources").get().asFile
        )

        val commonMain = kmpExt.sourceSets.getByName("commonMain")
        commonMain.kotlin.srcDirs(
            project.layout.buildDirectory.dir("generated/commonMain/kotlin").get().asFile,
            project.layout.buildDirectory.dir("generated/commonMain/resources").get().asFile
        )
    }

    private fun platformClientsBuildFolders(project: Project) {
        val androidProject = ResPluginHelpers.findAndroidModule(project)
        if (androidProject != null) {
            val androidExt = androidProject.extensions.getByType(ApplicationExtension::class.java)
            androidExt.sourceSets.getByName("main").apply {
                kotlin.srcDirs(
                    androidProject.layout.buildDirectory.dir("generated/kotlin").get().asFile,
                    androidProject.layout.buildDirectory.dir("generated/resources").get().asFile,
                )
              //  resources.srcDir(androidProject.layout.buildDirectory.dir("generated/resources").get().asFile)
            }
        }
    }

    override fun apply(project: Project) {
        // configure src files (build files for all tasks)
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
                //   project.configureKmpGeneratedSourceSets()
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

                configureBuildFolders(project)
                platformClientsBuildFolders(project)
            }
        }
    }
}