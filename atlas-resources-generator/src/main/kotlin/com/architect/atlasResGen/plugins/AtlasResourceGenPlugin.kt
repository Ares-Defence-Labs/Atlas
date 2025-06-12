package com.architect.atlasResGen.plugins

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlasResGen.helpers.AppleResPluginHelpers
import com.architect.atlasResGen.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasResourceGenPlugin : Plugin<Project> {
    private fun configureResourceTaskGraph(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        generateDependencyGraphTask.mustRunAfter("debugAssetsCopyForAGP")

        val masterKeyHandler = project.tasks.findByName("masterKeyHandler")
        if (masterKeyHandler == null) {
            val graphsDep = project.tasks.findByName("generateDependencyGraph")
            if (graphsDep != null) {
                generateDependencyGraphTask.mustRunAfter(graphsDep)
            }

            val navDep = project.tasks.findByName("generateNavAtlasEngine")
            if (navDep != null) {
                generateDependencyGraphTask.mustRunAfter(navDep)
            }

            TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
        }
    }

    private fun configureAppleResourceTaskGraph(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        val stringsGraph = project.tasks.findByName("generateAtlasStringsGraph")
        val colorGraph = project.tasks.findByName("generateAtlasColorsGraph")

        generateDependencyGraphTask.mustRunAfter(stringsGraph)
        generateDependencyGraphTask.mustRunAfter(colorGraph)
    }

    override fun apply(project: Project) {

        project.afterEvaluate {
            val generateStringsResources = ResPluginHelpers.getStringResourceTask(project)
            val generateColorsResources = ResPluginHelpers.getColorsResourceTask(project)

            configureResourceTaskGraph(project, generateStringsResources.get())
            configureResourceTaskGraph(project, generateColorsResources.get())
            generateColorsResources.configure {
                mustRunAfter(generateStringsResources)
            }

            val isiOSTarget = ProjectFinder.isBuildingForIos(project)
            if (isiOSTarget) {
                // apple tasks
                val generateAtlasXCAssetFileResources =
                    AppleResPluginHelpers.getAtlasXCAssetFilePackagingTask(project)
                val generateAppleFontFiles =
                    AppleResPluginHelpers.getAppleFontsResourceTask(project)
                val generateAppleScriptsGenFontFiles =
                    AppleResPluginHelpers.getAppleFontsPackagingResourceTask(project)

                val masterKeyHandler = project.tasks.findByName("masterKeyHandler")
                if (masterKeyHandler == null) {
                    configureResourceTaskGraph(project, generateAtlasXCAssetFileResources.get())
                    configureResourceTaskGraph(project, generateAppleFontFiles.get())
                    configureResourceTaskGraph(project, generateAppleScriptsGenFontFiles.get())

                    configureAppleResourceTaskGraph(
                        project,
                        generateAtlasXCAssetFileResources.get()
                    )
                    configureAppleResourceTaskGraph(project, generateAppleFontFiles.get())
                    configureAppleResourceTaskGraph(project, generateAppleScriptsGenFontFiles.get())

                    generateAppleFontFiles.configure {
                        mustRunAfter(generateAtlasXCAssetFileResources)
                    }

                    generateAppleScriptsGenFontFiles.configure {
                        mustRunAfter(generateAppleFontFiles)
                    }
                }
            } else {
                // android tasks
                val generateImagesResources = ResPluginHelpers.getImageResourceTask(project)
                val generateFontsResources = ResPluginHelpers.getFontsResourceTask(project)

                configureResourceTaskGraph(project, generateImagesResources.get())
                configureResourceTaskGraph(project, generateFontsResources.get())

                generateImagesResources.configure {
                    mustRunAfter(generateColorsResources)
                }
                generateFontsResources.configure {
                    mustRunAfter(generateImagesResources)
                }
            }
        }
    }
}