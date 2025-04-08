package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.navigationEngine.tasks.routingEngine.NavigationEngineGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

internal object ResPluginHelpers {
    fun getNavEngineGenTask(project: Project): TaskProvider<NavigationEngineGeneratorTask> {
        val androidApp = ProjectFinder.findAndroidClientApp(project)!!
        return project.tasks.register(
            "generateNavAtlasEngine",
            NavigationEngineGeneratorTask::class.java
        ) {
            outputFiles = project.rootProject.allprojects.map { File(it.projectDir, "src") }.toList()
            outputAndroidDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/atlas/navigation"))
            projectRootDir.set(project.layout.projectDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/atlas/navigation"))
        }
    }
}

