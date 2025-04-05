package com.architect.atlasResGen.plugins

import com.android.build.api.dsl.ApplicationExtension
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.FileHelpers.runShellScript
import com.architect.atlasResGen.helpers.ProjectFinder
import com.architect.atlasResGen.helpers.ResPluginHelpers
import com.architect.atlasResGen.helpers.TaskDefinitions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener

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
        project.logger.lifecycle("📁 Setting up Android project directories for generated resources")

        val androidProject = ProjectFinder.findAndroidClientApp(project)
        if (androidProject != null) {
            val androidExt = androidProject.extensions.findByType(ApplicationExtension::class.java)
            if (androidExt == null) {
                project.logger.error("❌ Could not find ApplicationExtension on androidApp project")
                return
            }

            androidExt.sourceSets.named("main").configure {
                java.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin").get().asFile
                )
                java.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources")
                        .get().asFile
                )
                java.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/resources").get().asFile
                )
                java.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources/fonts")
                        .get().asFile
                )
                java.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources/images")
                        .get().asFile
                )

                kotlin.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin").get().asFile
                )
                kotlin.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources")
                        .get().asFile
                )
                kotlin.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/resources").get().asFile
                )
                kotlin.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources/fonts")
                        .get().asFile
                )
                kotlin.srcDir(
                    androidProject.layout.buildDirectory.dir("generated/kotlin/resources/images")
                        .get().asFile
                )
            }

            project.logger.lifecycle("✅ Added generated sources to androidApp's sourceSets.main")
        } else {
            project.logger.warn("⚠️ androidApp module not found – cannot add generated source folders")
        }
    }

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

            configureBuildFolders(project)
            platformClientsBuildFolders(project)
        }
    }
}