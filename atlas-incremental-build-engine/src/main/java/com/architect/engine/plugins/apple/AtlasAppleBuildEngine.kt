package com.architect.engine.plugins.apple

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.engine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class AtlasAppleBuildEngine : Plugin<Project> {
    override fun apply(project: Project) {
        val appleGenPackage = project.tasks.register("appleGenerateXCPackage")
        val masterKeyHandler = project.tasks.register("masterKeyHandler")
        TaskMngrHelpers.taskOrderConfig(project, masterKeyHandler.get())

        val appleXcodeProcess = project.tasks.register("appleXCPackageFramework")
        appleXcodeProcess.configure {
            dependsOn(masterKeyHandler)
        }

        project.afterEvaluate {
            val generateGlobalHashChecker =
                ResPluginHelpers.getIncrementalVerifierForAllModulesTask(project)

            val isiOS = ProjectFinder.isBuildingForIos(project)
            val moduleTasks = if (isiOS) mutableListOf(
                // apple tasks
                "generateDependencyGraph",
                "generateNavAtlasEngine",
                "generateAtlasStringsGraph",
                "generateAtlasColorsGraph",

                // apple tasks
                "generateSwiftUIAtlasEngine",
                "appleFontsGenTask",
                "appleFontsPackagingGenTask",
                "applePackageXcodeGenTask",
            ) else mutableListOf(
                // default tasks
                "generateDependencyGraph",
                "generateNavAtlasEngine",
                "generateAtlasStringsGraph",
                "generateAtlasColorsGraph",
                "generateAtlasImagesGraph",
                "generateAtlasFontsGraph",
            )

            masterKeyHandler.configure {
                dependsOn(generateGlobalHashChecker, moduleTasks)
            }

            moduleTasks.forEach {
                val task = project.tasks.findByName(it)
                if (task != null) {
                    task.mustRunAfter(generateGlobalHashChecker)
                }
            }

            project.tasks.findByName("generateAtlasStringsGraph")
                ?.mustRunAfter("generateNavAtlasEngine")
            project.tasks.findByName("generateAtlasColorsGraph")
                ?.mustRunAfter("generateAtlasStringsGraph")
            project.tasks.findByName("generateAtlasImagesGraph")
                ?.mustRunAfter("generateAtlasColorsGraph")
            project.tasks.findByName("generateAtlasFontsGraph")
                ?.mustRunAfter("generateAtlasImagesGraph")

            if (isiOS) {
                project.tasks.findByName("generateSwiftUIAtlasEngine")
                    ?.mustRunAfter("generateDependencyGraph")
                project.tasks.findByName("generateSwiftUIAtlasEngine")
                    ?.mustRunAfter("generateNavAtlasEngine")
                project.tasks.findByName("generateSwiftUIAtlasEngine")
                    ?.mustRunAfter("generateAtlasStringsGraph")
                project.tasks.findByName("generateSwiftUIAtlasEngine")
                    ?.mustRunAfter("generateAtlasColorsGraph")
                project.tasks.findByName("appleFontsGenTask")
                    ?.mustRunAfter("generateSwiftUIAtlasEngine")
                project.tasks.findByName("appleFontsPackagingGenTask")
                    ?.mustRunAfter("appleFontsGenTask")
                project.tasks.findByName("applePackageXcodeGenTask")
                    ?.mustRunAfter("appleFontsPackagingGenTask")

                val handler = ResPluginHelpers.getIncrementalBuilderTask(project)
                appleGenPackage.configure {
                    mustRunAfter(appleXcodeProcess)
                    dependsOn(
                        handler
                    )
                }
            } else {
                if (project.tasks.map { it.name }.any { moduleTasks.contains(it) }) {
                    moduleTasks.forEach {
                        val name = project.tasks.findByName(it)
                        name?.mustRunAfter(generateGlobalHashChecker)
                    }
                }

                TaskMngrHelpers.taskOrderConfig(project, generateGlobalHashChecker.get())
            }
        }
    }
}