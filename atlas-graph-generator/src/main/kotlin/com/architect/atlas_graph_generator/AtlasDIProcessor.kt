package com.architect.atlasGraphGenerator

import com.architect.atlas.common.helpers.FileHelpers
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskDefinitions
import com.architect.atlas.common.helpers.TaskMngrHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        val generateDependencyGraphTask = project.tasks.register(
            "generateDependencyGraph",
            AtlasDIProcessorGraphTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/kotlin"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/kotlin"))
            isAndroidTarget = project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library")
        }

        // ✅ Ensure `generateDependencyGraph` runs **before** Kotlin compilation
        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        // ✅ Ensure `generateDependencyGraph` runs before **Android Kotlin compilation**
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "Android"
            )
        }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        val requiredTasks = TaskDefinitions.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                //dependencyTask.mustRunAfter(generateDependencyGraphTask)
                generateDependencyGraphTask.configure { dependsOn(dependencyTask) }
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }

        val androidTasks = TaskDefinitions.getAndroidTaskDependencies()
        if (project.state.executed) {
            // **Project is already evaluated, use taskGraph.whenReady**
            project.gradle.taskGraph.whenReady {
                TaskMngrHelpers.attachDependenciesToGraph(
                    project,
                    generateDependencyGraphTask.get(),
                    androidTasks
                )
            }
        } else {
            // **Project is not yet evaluated, use `afterEvaluate`**
            project.afterEvaluate {
                TaskMngrHelpers.attachDependenciesToGraph(
                    project,
                    generateDependencyGraphTask.get(),
                    androidTasks
                )
            }
        }
    }
}