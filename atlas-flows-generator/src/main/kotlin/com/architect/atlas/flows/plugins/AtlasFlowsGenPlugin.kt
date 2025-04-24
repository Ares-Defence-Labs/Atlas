package com.architect.atlas.flows.plugins

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.flows.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasFlowsGenPlugin : Plugin<Project> {
    private val graphGen = "generateDependencyGraph"
    private val applePackageXcodeGenTask = "appleFontsGenTask"
    private val navEngineGenerator = "generateNavAtlasEngine"
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

        generateDependencyGraphTask.mustRunAfter(navEngineGenerator)
        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask)
    }

    override fun apply(project: Project) {
        project.afterEvaluate {
            taskOrderConfig(
                project,
                ResPluginHelpers.getSwiftUIBindingsEngineGenTask(project).get()
            )
        }
    }
}