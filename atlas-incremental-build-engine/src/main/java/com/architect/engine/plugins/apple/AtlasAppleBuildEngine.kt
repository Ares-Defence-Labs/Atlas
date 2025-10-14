package com.architect.engine.plugins.apple

import com.architect.atlas.common.helpers.AppleProjectFinder.isIPhoneBuildNow
import com.architect.atlas.common.helpers.AppleProjectFinder.isWatchBuildNow
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.engine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

import java.text.Normalizer
import kotlin.math.abs

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

            val isiOS = project.isWatchBuildNow() || project.isIPhoneBuildNow()
            val moduleTasks = mutableListOf<String>()

            if (project.tasks.any { it.name == "generateDependencyGraph" }) {
                moduleTasks.add("generateDependencyGraph")
            }

            if (project.tasks.any { it.name == "generateNavAtlasEngine" }) {
                moduleTasks.add("generateNavAtlasEngine")
            }

            if (isiOS) {
                if (project.tasks.any { it.name == "generateAtlasStringsGraph" }) {
                    moduleTasks.addAll(
                        listOf(
                            "generateAtlasStringsGraph",
                            "generateAtlasColorsGraph",

                            "appleFontsGenTask",
                            "appleFontsPackagingGenTask",
                            "applePackageXcodeGenTask",
                        )
                    )
                }
            } else {
                if (project.tasks.any { it.name == "generateAtlasStringsGraph" }) {
                    moduleTasks.addAll(
                        listOf(
                            "generateAtlasStringsGraph",
                            "generateAtlasColorsGraph",
                            "generateAtlasImagesGraph",
                            "generateAtlasFontsGraph",
                        )
                    )
                }
            }

            masterKeyHandler.configure {
                dependsOn(generateGlobalHashChecker, moduleTasks)
            }

            moduleTasks.forEach {
                project.tasks.findByName(it)?.mustRunAfter(generateGlobalHashChecker)
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
                project.tasks.findByName("appleFontsGenTask")
                    ?.mustRunAfter("generateAtlasColorsGraph")
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