package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.atlas.navigationEngine.tasks.routingEngine.NavigationEngineGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

internal object ResPluginHelpers {

    fun getNavEngineGenTask(project: Project): TaskProvider<NavigationEngineGeneratorTask> {
        val androidApp = ProjectFinder.findAndroidClientApp(project)!!
        val coutputFiles =
            project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()
        val iosoutputs = coutputFiles.toMutableList()
        iosoutputs.add(ProjectFinder.findIosClientApp(project)!!)

        return project.tasks.register(
            "generateNavAtlasEngine",
            NavigationEngineGeneratorTask::class.java
        ) {
            projectCoreName = project.getSwiftImportModuleName()
            iOSOutputFiles = iosoutputs
            outputFiles = coutputFiles
            outputAndroidDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation"))
            projectRootDir.set(project.layout.projectDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/navigation"))
        }
    }
}

