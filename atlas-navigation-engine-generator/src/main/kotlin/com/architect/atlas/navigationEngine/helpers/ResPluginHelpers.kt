package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.atlas.navigationEngine.tasks.routingEngine.NavigationEngineGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun File.isUnderAny(roots: List<File>): Boolean {
    val p = this.toPath().normalize()
    return roots.any { p.startsWith(it.toPath().normalize()) }
}

internal object ResPluginHelpers {

    fun getNavEngineGenTask(project: Project): TaskProvider<NavigationEngineGeneratorTask> {
        val iosXcodeModule = ProjectFinder.findIosClientApp(project)!!
        val androidApp = ProjectFinder.findAndroidClientApp(project)!!
        val wearApp = ProjectFinder.findWearApp(project)
        val coutputFiles =
            project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()
        val iosoutputs = coutputFiles.toMutableList()
        iosoutputs.add(iosXcodeModule)

        val moduleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val projectTask = project.tasks.register(
            "generateNavAtlasEngine",
            NavigationEngineGeneratorTask::class.java
        ) {
            projectCoreName = moduleName
            iOSOutputFiles = iosoutputs
            outputFiles = coutputFiles
            outputAndroidDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation"))
            outputAndroidTabsDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation/tabs"))
            androidSourceFiles.from(
                androidApp.layout.projectDirectory.dir("src/main/kotlin")
            )
            projectRootDir.set(project.layout.projectDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/navigation"))
            isIOSTarget = ProjectFinder.isBuildingForIos(project)
        }

        val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
            include("graphInputHash.txt")
        }
        if (!hashFileTree.isEmpty) {
            projectTask.configure {
                inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            }
        }

        if (wearApp != null) {
            projectTask.configure {
                wearOSDir.set(wearApp.layout.buildDirectory.dir("generated/kotlin/navigation"))
                wearOSSourceFiles.from(
                    wearApp.layout.projectDirectory.dir("src/main/kotlin")
                )
            }
        }

        return projectTask
    }
}

