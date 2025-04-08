package com.architect.atlas.navigationEngine.plugins

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.navigationEngine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class NavigationEngineGenPlugin : Plugin<Project> {
    private val graphGen = "generateDependencyGraph"
    private val applePackageXcodeGenTask = "appleFontsGenTask"
    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: Task
    ) {
        val dependencyGraphTask = project.rootProject.allprojects
            .flatMap { it.tasks }
            .filter { it.name in listOf(graphGen, applePackageXcodeGenTask) }

        if (dependencyGraphTask.isNotEmpty()) {
            dependencyGraphTask.forEach {
                project.logger.lifecycle("✅ Found and linking ${generateDependencyGraphTask.name} to task: ${it.path}")
                generateDependencyGraphTask.dependsOn(it)
                generateDependencyGraphTask.mustRunAfter(it)
            }
        }

        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    override fun apply(project: Project) {
        taskOrderConfig(project, ResPluginHelpers.getNavEngineGenTask(project).get())
    }
}