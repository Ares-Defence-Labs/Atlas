package com.architect.atlas.navigationEngine.plugins

import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.navigationEngine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class NavigationEngineGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val masterKeyHandler = project.tasks.findByName("masterKeyHandler")
            val navTask = ResPluginHelpers.getNavEngineGenTask(project)
            if (masterKeyHandler == null) {
                project.logger.lifecycle("CANNOT FIND MASTER KEY")
                TaskMngrHelpers.taskOrderConfig(project, navTask.get())
            }

            val graphsDep = project.tasks.findByName("generateDependencyGraph")
            if (graphsDep != null) {
                navTask.configure {
                    mustRunAfter(graphsDep)
                }
            }

            navTask.configure {
                mustRunAfter("debugAssetsCopyForAGP")
            }
        }
    }
}