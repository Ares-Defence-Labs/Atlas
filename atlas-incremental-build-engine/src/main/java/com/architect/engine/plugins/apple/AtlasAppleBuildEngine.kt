package com.architect.engine.plugins.apple

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.engine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasAppleBuildEngine : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val generateGlobalHashChecker = ResPluginHelpers.getIncrementalVerifierForAllModulesTask(project)

            val generateXCChecker = ResPluginHelpers.getIncrementalXCVerifierTask(project)
            val generateDependencyGraphTask = ResPluginHelpers.getIncrementalBuilderTask(project)
            val moduleTasks = listOf(
                "generateDependencyGraph",
                "generateNavAtlasEngine",
                "generateSwiftUIAtlasEngine",
                "generateAtlasStringsGraph",
                "generateAtlasColorsGraph",
                "generateAtlasImagesGraph",
                "generateAtlasFontsGraph"
            )

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
                    if(name != null){
                        taskGen.configure {
                            mustRunAfter(name)
                        }

                        generateXCChecker.configure {
                            mustRunAfter(name)
                        }
                    }
                }
            }

            generateXCChecker.configure {
                mustRunAfter(name)
            }

            TaskMngrHelpers.taskOrderConfig(project, generateGlobalHashChecker.get())
            TaskMngrHelpers.taskOrderConfig(project, taskGen.get())
            TaskMngrHelpers.taskOrderConfig(project, generateXCChecker.get())
            generateDependencyGraphTask.configure {
                mustRunAfter(generateXCChecker)
            }
        }
    }
}