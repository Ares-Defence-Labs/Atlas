package com.architect.atlasResGen.plugins

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasResourceGenPlugin : Plugin<Project> {
    private val graphGen = "generateDependencyGraph"
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

        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    override fun apply(project: Project) {
        val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)
        val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)

        val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)
        val generateFontsResources = ResPluginHelpers.getFontsResourceTask(project)

        val generateAtlasXCAssetFileResources =
            AppleResPluginHelpers.getAtlasXCAssetFilePackagingTask(project)
        val generateAppleFontFiles =
            AppleResPluginHelpers.getAppleFontsResourceTask(project)
        val generateAppleScriptsGenFontFiles =
            AppleResPluginHelpers.getAppleFontsPackagingResourceTask(project)


        taskOrderConfig(project, generateStringsResources.get())
        taskOrderConfig(project, generateColorsResources.get())
        taskOrderConfig(project, generateImagesResources.get())
        taskOrderConfig(project, generateFontsResources.get())
        taskOrderConfig(project, generateAtlasXCAssetFileResources.get())
        taskOrderConfig(project, generateAppleFontFiles.get())
        taskOrderConfig(project, generateAppleScriptsGenFontFiles.get())

        generateColorsResources.configure {
            mustRunAfter(generateStringsResources)
        }
        generateImagesResources.configure {
            mustRunAfter(generateColorsResources)
        }
        generateFontsResources.configure {
            mustRunAfter(generateImagesResources)
        }
        generateAtlasXCAssetFileResources.configure {
            mustRunAfter(generateFontsResources)
        }
        generateAppleFontFiles.configure {
            mustRunAfter(generateAtlasXCAssetFileResources)
        }

        generateAppleScriptsGenFontFiles.configure {
            mustRunAfter(generateAppleFontFiles)
        }
    }
}