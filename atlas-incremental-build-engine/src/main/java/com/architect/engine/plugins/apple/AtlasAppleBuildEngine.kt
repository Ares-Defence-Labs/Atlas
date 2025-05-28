package com.architect.engine.plugins.apple

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.engine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasAppleBuildEngine : Plugin<Project> {
    override fun apply(project: Project) {
        val generateGlobalHashChecker =
            ResPluginHelpers.getIncrementalVerifierForAllModulesTask(project)
        project.afterEvaluate {
            val moduleTasks = listOf(
                "generateDependencyGraph",
                "generateNavAtlasEngine",
                "generateSwiftUIAtlasEngine",
                "generateAtlasStringsGraph",
                "generateAtlasColorsGraph",
                "generateAtlasImagesGraph",
                "generateAtlasFontsGraph"
            )

            val isiOS = System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
                    || System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("iphone") == true
            if (isiOS) {
                project.logger.lifecycle("RUNNING XCODE PACKAGING IOS")
                val generateXCChecker = ResPluginHelpers.getIncrementalXCVerifierTask(project)
                val generateDependencyGraphTask =
                    ResPluginHelpers.getIncrementalBuilderTask(project)
                val taskGen = project.tasks.register("appleXCPackageFramework")
                    .apply {
                        configure {
                            dependsOn(
                                generateGlobalHashChecker,
                                moduleTasks,
                                generateXCChecker,
                            )
                        }
                    }

                if (project.tasks.map { it.name }.any { moduleTasks.contains(it) }) {
                    moduleTasks.forEach {
                        val name = project.tasks.findByName(it)
                        if (name != null) {
                            taskGen.configure {
                                mustRunAfter(name)
                            }

                            generateXCChecker.configure {
                                mustRunAfter(name)
                            }

                            name.mustRunAfter(generateGlobalHashChecker)
                        }
                    }
                }

                TaskMngrHelpers.taskOrderConfig(project, taskGen.get())
                TaskMngrHelpers.taskOrderConfig(project, generateXCChecker.get())

                // configure order of the hash file
                generateDependencyGraphTask.configure {
                    mustRunAfter(generateXCChecker)
                    mustRunAfter(generateGlobalHashChecker)
                }
                taskGen.configure {
                    mustRunAfter(generateGlobalHashChecker)
                }
                generateXCChecker.configure {
                    mustRunAfter(generateGlobalHashChecker)
                }
            } else {
                if (project.tasks.map { it.name }.any { moduleTasks.contains(it) }) {
                    moduleTasks.forEach {
                        val name = project.tasks.findByName(it)
                        name?.mustRunAfter(generateGlobalHashChecker)
                    }
                }
            }

            TaskMngrHelpers.taskOrderConfig(project, generateGlobalHashChecker.get())
        }
    }
}