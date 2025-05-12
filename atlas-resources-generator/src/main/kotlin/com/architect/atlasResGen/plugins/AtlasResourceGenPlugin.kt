package com.architect.atlasResGen.plugins

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasResourceGenPlugin : Plugin<Project> {
    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    private fun configureResourceTaskGraph(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        val graphsDep = project.tasks.findByName("generateDependencyGraph")
        if (graphsDep != null) {
            generateDependencyGraphTask.mustRunAfter(graphsDep)
        }

        val navDep = project.tasks.findByName("generateNavAtlasEngine")
        if (navDep != null) {
            generateDependencyGraphTask.mustRunAfter(navDep)
        }

        taskOrderConfig(project, generateDependencyGraphTask)
    }

    private fun configureAppleResourceTaskGraph(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        val stringsGraph = project.tasks.findByName("generateAtlasStringsGraph")
        val colorGraph = project.tasks.findByName("generateAtlasColorsGraph")
        val imagesGraph = project.tasks.findByName("generateAtlasImagesGraph")
        val fontsGraph = project.tasks.findByName("generateAtlasFontsGraph")

        generateDependencyGraphTask.dependsOn(stringsGraph)
        generateDependencyGraphTask.mustRunAfter(stringsGraph)

        generateDependencyGraphTask.dependsOn(colorGraph)
        generateDependencyGraphTask.mustRunAfter(colorGraph)

        generateDependencyGraphTask.dependsOn(imagesGraph)
        generateDependencyGraphTask.mustRunAfter(imagesGraph)

        generateDependencyGraphTask.dependsOn(fontsGraph)
        generateDependencyGraphTask.mustRunAfter(fontsGraph)
    }

    override fun apply(project: Project) {
        val isiOSTarget = !ProjectFinder.isBuildingForAndroid(project)
        project.afterEvaluate {
            val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)
            val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)
            val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)
            val generateFontsResources = ResPluginHelpers.getFontsResourceTask(project)

            configureResourceTaskGraph(project, generateStringsResources.get())
            configureResourceTaskGraph(project, generateColorsResources.get())
            configureResourceTaskGraph(project, generateImagesResources.get())
            configureResourceTaskGraph(project, generateFontsResources.get())

            generateColorsResources.configure()
            {
                mustRunAfter(generateStringsResources)
            }

            generateImagesResources.configure {
                dependsOn(generateColorsResources)
            }
            generateFontsResources.configure {
                dependsOn(generateImagesResources)
            }

            if (isiOSTarget) {
                val generateAtlasXCAssetFileResources =
                    AppleResPluginHelpers.getAtlasXCAssetFilePackagingTask(project)
                val generateAppleFontFiles =
                    AppleResPluginHelpers.getAppleFontsResourceTask(project)
                val generateAppleScriptsGenFontFiles =
                    AppleResPluginHelpers.getAppleFontsPackagingResourceTask(project)

                configureResourceTaskGraph(project, generateAtlasXCAssetFileResources.get())
                configureResourceTaskGraph(project, generateAppleFontFiles.get())
                configureResourceTaskGraph(project, generateAppleScriptsGenFontFiles.get())

                configureAppleResourceTaskGraph(project, generateAtlasXCAssetFileResources.get())
                configureAppleResourceTaskGraph(project, generateAppleFontFiles.get())
                configureAppleResourceTaskGraph(project, generateAppleScriptsGenFontFiles.get())

                generateAppleFontFiles.configure {
                    dependsOn(generateAtlasXCAssetFileResources)
                }

                generateAppleScriptsGenFontFiles.configure {
                    dependsOn(generateAppleFontFiles)
                }
            }
        }
    }
}