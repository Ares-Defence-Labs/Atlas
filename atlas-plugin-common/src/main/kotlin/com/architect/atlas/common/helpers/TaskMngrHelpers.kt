package com.architect.atlas.common.helpers

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

object TaskMngrHelpers{
    private val stringsGenTask = "generateAtlasStringsGraph"
    private val colorGenTask = "generateAtlasColorsGraph"
    private val imageGenTask = "generateAtlasImagesGraph"
    private val fontsGenTask = "generateAtlasFontsGraph"

    fun configureBuildFolders(project: Project) {
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

    fun platformClientsBuildFolders(project: Project) {
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

    fun configureResourceModuleTask(
        project: Project,
        generateNavEngineGraphTask: Task,
    ) {
        val stringsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(stringsGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$stringsGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val fontsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(fontsGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$fontsGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val imagesResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(imageGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$imageGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val colorsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(colorGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$colorGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()

        val usingResourceMode =
            stringsResourceTask != null && fontsResourceTask != null && imagesResourceTask != null && colorsResourceTask != null
        if (usingResourceMode) { // if using resources module, requires dependency to avoid issues with kotlin compile
            generateNavEngineGraphTask.dependsOn(stringsResourceTask)
            generateNavEngineGraphTask.dependsOn(fontsResourceTask)
            generateNavEngineGraphTask.dependsOn(imagesResourceTask)
            generateNavEngineGraphTask.dependsOn(colorsResourceTask)
        }
    }

    fun attachDependenciesToGraph(
        project: Project,
        generateDependencyGraphTask: Task,
        androidTasks: List<String>
    ) {
        configureBuildFolders(project)
        configureResourceModuleTask(project, generateDependencyGraphTask)
        project.rootProject.allprojects.forEach { subProject ->
            androidTasks.forEach { taskName ->
                val dependencyTask: Task? = subProject.tasks.findByName(taskName)

                if (dependencyTask != null) {
                    generateDependencyGraphTask.dependsOn(dependencyTask)
                    project.logger.lifecycle("✅ `${generateDependencyGraphTask.name}` now depends on `:${subProject.name}:$taskName`")
                } else {
                    project.logger.lifecycle("⚠️ `:${subProject.name}:$taskName` not found. Skipping dependency assignment.")
                }
            }
        }
    }

    fun taskOrderConfig(
        project: Project,
        generateDependencyTask: Task
    ) {
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            mustRunAfter(generateDependencyTask)
        }
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "android"
            )
        }.configureEach {
            mustRunAfter(generateDependencyTask)
        }

        project.tasks.matching {
            it.name in listOf("preBuild", "assemble", "build")
        }.configureEach {
            dependsOn(generateDependencyTask)
        }

        project.tasks.named("generateDependencyGraph").configure {
            mustRunAfter("debugAssetsCopyForAGP", "prepareLintJarForPublish")
        }

        val requiredTasks = TaskDefinitions.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                dependencyTask.dependsOn(generateDependencyTask)
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }
    }
}