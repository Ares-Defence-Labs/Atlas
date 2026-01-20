package com.architect.atlas.navigationEngine.plugins

import com.architect.atlas.common.helpers.AppleProjectFinder.isIPhoneBuildNow
import com.architect.atlas.common.helpers.AppleProjectFinder.isWatchBuildNow
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.navigationEngine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class NavigationEngineGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val masterKeyHandler = project.tasks.findByName("masterKeyHandler")

            val isIOSTarget = project.isWatchBuildNow() || project.isIPhoneBuildNow()
            if (isIOSTarget) {
                logger.lifecycle("Running iOS Navigation")
                val isAppleWatch = project.isWatchBuildNow()
                if (isAppleWatch) {
                    val watchTask = ResPluginHelpers.getAppleWatchNavGenTask(project)
                    project.registerNavTask(watchTask)

                    if (masterKeyHandler == null) {
                        TaskMngrHelpers.taskOrderConfig(project, watchTask.get())
                    }
                } else {
                    val iosTask = ResPluginHelpers.getIOSNavGenTask(project)
                    project.registerNavTask(iosTask)

                    if (masterKeyHandler == null) {
                        TaskMngrHelpers.taskOrderConfig(project, iosTask.get())
                    }
                }
            } else {

                val isComposeNavigation =
                    project.findProperty("atlas.composeNavigation")?.toString()?.toBooleanStrictOrNull()
                        ?: true

                logger.lifecycle("Running Android Navigation, is Compose - $isComposeNavigation")

                if (isComposeNavigation) {
                    val composeTask = ResPluginHelpers.getAndroidComposeNavGenTask(project)
                    project.registerNavTask(composeTask)

                    if (masterKeyHandler == null) {
                        TaskMngrHelpers.taskOrderConfig(project, composeTask.get())
                    }
                } else {
                    val classicalTask = ResPluginHelpers.getAndroidClassicalNavGenTask(project)
                    project.registerNavTask(classicalTask)

                    if (masterKeyHandler == null) {
                        TaskMngrHelpers.taskOrderConfig(project, classicalTask.get())
                    }
                }
            }
        }
    }
}

private fun Project.registerNavTask(navTask: TaskProvider<out Task>) {
    val graphsDep = tasks.findByName("generateDependencyGraph")
    if (graphsDep != null) {
        navTask.configure {
            mustRunAfter(graphsDep)
        }
    }

    // Always try to run after debugAssetsCopyForAGP (safe to reference by name)
    navTask.configure {
        mustRunAfter("debugAssetsCopyForAGP")
    }
}