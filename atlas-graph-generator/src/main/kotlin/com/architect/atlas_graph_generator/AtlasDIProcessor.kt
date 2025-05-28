package com.architect.atlasGraphGenerator

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        val droidModule = ProjectFinder.findAndroidClientApp(project)
        val generateDependencyGraphTask = project.tasks.register(
            "generateDependencyGraph",
            AtlasDIProcessorGraphTask::class.java
        ) {
            externalSourceDirs.from(
                droidModule?.layout?.projectDirectory?.dir("src/main/kotlin")
            )
            iosSourceDirs.from(
                project.layout.projectDirectory.dir("src/iosMain/kotlin")
            )

            projectRootDir.from(
                project.layout.projectDirectory.dir("src/commonMain/kotlin")
            )
            androidOutputDir.set(droidModule?.layout?.buildDirectory?.dir("generated/kotlin/container")!!)
            iOSOutputDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/container"))
            isAndroidTarget = ProjectFinder.isBuildingForAndroid(project)
            inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
        }

        TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask.get())
    }
}