package com.architect.atlasResGen.helpers

import com.architect.atlasResGen.tasks.colors.AtlasColorsPluginTask
import com.architect.atlasResGen.tasks.fonts.AtlasFontPluginTask
import com.architect.atlasResGen.tasks.images.AtlasImagePluginTask
import com.architect.atlasResGen.tasks.strings.AtlasStringPluginTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    private val stringsGenTask = "generateAtlasStringsGraph"
    private val colorGenTask = "generateAtlasColorsGraph"
    private val imageGenTask = "generateAtlasImagesGraph"
    private val fontsGenTask = "generateAtlasFontsGraph"
    fun isDebugMode(project: Project) =
        project.tasks.any { it.name.contains("debug", ignoreCase = true) }

    fun attachDependenciesToGraph(
        project: Project,
        generateDependencyGraphTask: Task,
        androidTasks: List<String>
    ) {
        project.rootProject.allprojects.forEach { subProject ->
            androidTasks.forEach { taskName ->
                val dependencyTask: Task? = subProject.tasks.findByName(taskName)

                if (dependencyTask != null) {
                    generateDependencyGraphTask.dependsOn(dependencyTask)
                    project.logger.lifecycle("✅ `${dependencyTask.name}` now depends on `:${subProject.name}:$taskName`")
                } else {
                    project.logger.lifecycle("⚠️ `:${subProject.name}:$taskName` not found")
                }
            }
        }
    }

    fun getStringResourceTask(project: Project): TaskProvider<AtlasStringPluginTask> {
        return project.tasks.register(
            stringsGenTask,
            AtlasStringPluginTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
            isAndroidTarget = !ProjectFinder.isBuildingForIos(project)
        }
    }

    private fun getAndroidAppNamespace(androidProject: Project): String {
        return when (val extension = androidProject.extensions.findByName("android")) {
            is com.android.build.api.dsl.ApplicationExtension -> extension.namespace
            is com.android.build.api.dsl.LibraryExtension -> extension.namespace
            else -> error("Unsupported or missing Android extension in ${androidProject.path}")
        } ?: error("Namespace not defined in Android module ${androidProject.path}")
    }

    fun getImageResourceTask(project: Project): TaskProvider<AtlasImagePluginTask> {
        val androidProject = ProjectFinder.findAndroidModule(project)
        if (androidProject == null) {
            project.logger.warn("⚠️ AtlasImagePlugin: Could not locate Android module. Skipping Android asset integration.")
        }

        val isAndroid = androidProject != null
        return project.tasks.register(
            imageGenTask,
            AtlasImagePluginTask::class.java
        ) {
            forceRegenerate = FileHelpers.forceRecreateAllFiles(project)
            androidResourcePackageRef =
                if (isAndroid) getAndroidAppNamespace(androidProject!!) else ""
            androidAssetImageDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/assets/images"))
            androidResourcesDrawableDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/res"))
            projectBuildDir.set(project.layout.buildDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources"))
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            if (isAndroid) androidOutputDir.set(androidProject!!.layout.buildDirectory.dir("generated/kotlin/resources"))
        }
    }

    fun getFontsResourceTask(project: Project): TaskProvider<AtlasFontPluginTask> {
        val androidProject = ProjectFinder.findAndroidModule(project)
        if (androidProject == null) {
            project.logger.warn("⚠️ AtlasImagePlugin: Could not locate Android module. Skipping Android asset integration.")
        }

        val isAndroid = androidProject != null
        return project.tasks.register(
            fontsGenTask,
            AtlasFontPluginTask::class.java
        ) {
            forceRegenerate = FileHelpers.forceRecreateAllFiles(project)
            androidResourcesFontsDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/res/font"))
            androidResourcePackageRef =
                if (isAndroid) getAndroidAppNamespace(androidProject!!) else ""
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            if (isAndroid) androidOutputDir.set(androidProject!!.layout.buildDirectory.dir("generated/kotlin/resources"))
        }
    }

    fun getColorsResourceTask(project: Project): TaskProvider<AtlasColorsPluginTask> {
        return project.tasks.register(
            colorGenTask,
            AtlasColorsPluginTask::class.java
        ) {
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources"))
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
            isAndroidTarget = !ProjectFinder.isBuildingForIos(project)
        }
    }

    fun prepareResourcesDirectory(project: Project) {
        // 🧹 Clean the output directory first (to force regeneration)
        val outputBase =
            project.layout.buildDirectory.dir("generated/commonMain/resources").get().asFile
        if (outputBase.exists()) {
            outputBase.deleteRecursively()
        }
        outputBase.mkdirs() // destroy the tasks
    }
}

