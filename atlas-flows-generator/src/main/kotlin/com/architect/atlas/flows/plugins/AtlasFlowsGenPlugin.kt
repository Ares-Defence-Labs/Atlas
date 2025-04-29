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
        "generateAtlasImagesGraph",
        "generateAtlasFontsGraph",
        "appleFontsGenTask",
        "appleFontsPackagingGenTask",
        "applePackageXcodeGenTask"
    )

    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    override fun apply(project: Project) {
        val isIOS = !ProjectFinder.isBuildingForAndroid(project)
        if (isIOS) {
            project.afterEvaluate {
                val swiftFlows = ResPluginHelpers.getSwiftUIBindingsEngineGenTask(project).get()
                taskOrderConfig(
                    project,
                    swiftFlows
                )

                val graphsDep = project.tasks.findByName("generateDependencyGraph")
                if (graphsDep != null) {
                    swiftFlows.mustRunAfter(graphsDep)
                }

                val navDep = project.tasks.findByName("generateNavAtlasEngine")
                if (navDep != null) {
                    swiftFlows.mustRunAfter(navDep)
                }

                resourceGenTasks.forEach { taskName ->
                    val resTask = project.tasks.findByName(taskName)
                    if (resTask != null) {
                        swiftFlows.mustRunAfter(taskName)
                    }
                }
            }
        }
    }
}