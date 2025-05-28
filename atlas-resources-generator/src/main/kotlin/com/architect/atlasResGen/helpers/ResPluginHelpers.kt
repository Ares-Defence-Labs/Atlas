package com.architect.atlasResGen.helpers

import com.architect.atlas.common.helpers.FileHelpers
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlasResGen.tasks.colors.AtlasColorsPluginTask
import com.architect.atlasResGen.tasks.fonts.AtlasFontPluginTask
import com.architect.atlasResGen.tasks.images.AtlasImagePluginTask
import com.architect.atlasResGen.tasks.strings.AtlasStringPluginTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    private val stringsGenTask = "generateAtlasStringsGraph"
    private val colorGenTask = "generateAtlasColorsGraph"
    private val imageGenTask = "generateAtlasImagesGraph"
    private val fontsGenTask = "generateAtlasFontsGraph"

    fun getStringResourceTask(project: Project): TaskProvider<AtlasStringPluginTask> {
        return project.tasks.register(
            stringsGenTask,
            AtlasStringPluginTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
            isAndroidTarget = !ProjectFinder.isBuildingForIos(project)
            inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
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
            inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
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
            inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
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
            inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }
    }
}

