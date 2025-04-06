package com.architect.atlasGraphGenerator

import com.architect.atlas.common.helpers.TaskDefinitions
import com.architect.atlas.common.helpers.TaskMngrHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class AtlasDIProcessor : Plugin<Project> {
    val graphGen = "generateDependencyGraph"
    private fun taskOrderConfig(
        project: Project,
        generateDependencyGraphTask: TaskProvider<AtlasDIProcessorGraphTask>
    ) {
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            mustRunAfter(generateDependencyGraphTask)
        }
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "android"
            )
        }.configureEach {
            mustRunAfter(generateDependencyGraphTask)
        }

        project.tasks.matching {
            it.name in listOf("preBuild", "assemble", "build")
        }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        project.tasks.named("generateDependencyGraph").configure {
            mustRunAfter("debugAssetsCopyForAGP", "prepareLintJarForPublish")
        }

        val requiredTasks = TaskDefinitions.getiOSTaskDependencies()
        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                dependencyTask.dependsOn(generateDependencyGraphTask)
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }
    }

    override fun apply(project: Project) {
        project.afterEvaluate{
            TaskMngrHelpers.configureBuildFolders(project)
        }
        val generateDependencyGraphTask = project.tasks.register(
            graphGen,
            AtlasDIProcessorGraphTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/kotlin"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/kotlin"))
            isAndroidTarget = project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library")
        }

        taskOrderConfig(project, generateDependencyGraphTask)
    }
}