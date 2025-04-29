package com.architect.atlas.navigationEngine.plugins

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.navigationEngine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class NavigationEngineGenPlugin : Plugin<Project> {
    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    override fun apply(project: Project) {
        val navTask = ResPluginHelpers.getNavEngineGenTask(project).get()
        taskOrderConfig(project, navTask)

        val graphsDep = project.tasks.findByName("generateDependencyGraph")
        if (graphsDep != null) {
            navTask.mustRunAfter(graphsDep)
        }
    }
}