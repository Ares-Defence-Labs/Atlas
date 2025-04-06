package com.architect.atlasGraphGenerator

import com.architect.atlas.common.helpers.TaskMngrHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            TaskMngrHelpers.configureBuildFolders(project)
        }
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

        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask.get())
    }
}