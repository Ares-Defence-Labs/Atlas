package com.architect.atlas.navigationEngine.plugins

import com.architect.atlas.common.helpers.TaskDefinitions
import com.architect.atlas.common.helpers.TaskMngrHelpers
import com.architect.atlas.navigationEngine.helpers.ResPluginHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class NavigationEngineGenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val generateUIKitNavGraphTask = ResPluginHelpers.getSwiftUIKitGenTask(project)
        //val generateDroidComposeGraphTask = ResPluginHelpers.getDroidComposeResourceTask(project)

        // ✅ Ensure `generateDependencyGraph` runs **before** Kotlin compilation
        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(generateUIKitNavGraphTask)
            //     dependsOn(generateDroidComposeGraphTask)
        }

        // ✅ Ensure `generateDependencyGraph` runs before **Android Kotlin compilation**
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "Android"
            )
        }.configureEach {
            //       dependsOn(generateDroidComposeGraphTask)
        }

        val requiredTasks = TaskDefinitions.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                generateUIKitNavGraphTask.configure { dependsOn(dependencyTask) }
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }

        val androidTasks = TaskDefinitions.getAndroidTaskDependencies(project)
        project.afterEvaluate {
            TaskMngrHelpers.attachDependenciesToGraph(
                project,
                generateUIKitNavGraphTask.get(),
                androidTasks
            )
            TaskMngrHelpers.platformClientsBuildFolders(project)
        }
    }
}