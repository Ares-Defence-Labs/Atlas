package com.architect.atlas.flows.plugins

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.flows.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasFlowsGenPlugin : Plugin<Project> {
    private val resourceGenTasks = listOf(
        "generateAtlasStringsGraph",
        "generateAtlasColorsGraph",
        "appleFontsGenTask",
        "appleFontsPackagingGenTask",
        "applePackageXcodeGenTask"
    )

    override fun apply(project: Project) {
        project.afterEvaluate {
            val isIOS = ProjectFinder.isBuildingForIos(project)
            if (isIOS) {
                val swiftFlows = ResPluginHelpers.getSwiftUIBindingsEngineGenTask(project)
                val masterKeyHandler = project.tasks.findByName("masterKeyHandler")
                if (masterKeyHandler == null) {
                    val graphsDep = project.tasks.findByName("generateDependencyGraph")
                    if (graphsDep != null) {
                        swiftFlows.configure {
                            mustRunAfter(graphsDep)
                        }
                    }

                    val navDep = project.tasks.findByName("generateNavAtlasEngine")
                    if (navDep != null) {
                        swiftFlows.configure {
                            mustRunAfter(navDep)
                        }
                    }

                    TaskMngrHelpers.taskOrderConfig(project, swiftFlows.get())
                    resourceGenTasks.forEach { taskName ->
                        val resTask = project.tasks.findByName(taskName)
                        if (resTask != null) {
                            swiftFlows.configure {
                                mustRunAfter(taskName)
                            }
                        }
                    }
                }
            }
        }
    }
}