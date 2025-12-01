package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.common.helpers.AppleProjectFinder
import com.architect.atlas.common.helpers.AppleProjectFinder.iosApps
import com.architect.atlas.common.helpers.AppleProjectFinder.watchExtensions
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.atlas.navigationEngine.tasks.routingEngine.android.ClassicalNavigationEngineGeneratorTask
import com.architect.atlas.navigationEngine.tasks.routingEngine.android.ComposeNavigationEngineGeneratorTask
import com.architect.atlas.navigationEngine.tasks.routingEngine.apple.AppleIOSNavigationEngineGeneratorTask
import com.architect.atlas.navigationEngine.tasks.routingEngine.apple.AppleWatchNavigationEngineGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun File.isUnderAny(roots: List<File>): Boolean {
    val p = this.toPath().normalize()
    return roots.any { p.startsWith(it.toPath().normalize()) }
}

internal object ResPluginHelpers {

    fun getAndroidClassicalNavGenTask(project: Project): TaskProvider<ClassicalNavigationEngineGeneratorTask> {
        val appleClients = AppleProjectFinder.findAllXcodeTargets(project.rootDir, project.logger)
        val iosXcodeModule = appleClients.iosApps().first()
        val androidApp = ProjectFinder.findAndroidClientApp(project)!!
        val coutputFiles =
            project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()
        val iosoutputs = coutputFiles.toMutableList()
        iosoutputs.add(iosXcodeModule.targetDir)

        val projectTask = project.tasks.register(
            "generateNavAtlasEngine",
            ClassicalNavigationEngineGeneratorTask::class.java
        ) {
            androidBasePackageRef = ProjectFinder.getAndroidAppNamespace(androidApp)
            outputFiles = coutputFiles
            outputAndroidDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation"))
            androidSourceFiles.from(
                androidApp.layout.projectDirectory.dir("src/main/kotlin")
            )
        }

        val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
            include("graphInputHash.txt")
        }
        if (!hashFileTree.isEmpty) {
            projectTask.configure {
                inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            }
        }

        return projectTask
    }


    fun getAndroidComposeNavGenTask(project: Project): TaskProvider<ComposeNavigationEngineGeneratorTask> {
        val androidApp = ProjectFinder.findAndroidClientApp(project)!!
        val wearApp = ProjectFinder.findWearApp(project)
        val projectTask = project.tasks.register(
            "generateNavAtlasEngine",
            ComposeNavigationEngineGeneratorTask::class.java
        ) {
            outputFiles =
                project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()
            outputAndroidDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation"))
            outputAndroidTabsDir.set(androidApp.layout.buildDirectory.dir("generated/kotlin/navigation/tabs"))
            androidSourceFiles.from(
                androidApp.layout.projectDirectory.dir("src/main/kotlin")
            )
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

    // apple tasks
    fun getIOSNavGenTask(project: Project): TaskProvider<AppleIOSNavigationEngineGeneratorTask> {
        val appleClients = AppleProjectFinder.findAllXcodeTargets(project.rootDir, project.logger)
        val iosXcodeModule = appleClients.iosApps().first()
        val appleWatchXcodeModule = appleClients.watchExtensions().firstOrNull()
        val coutputFiles =
            project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()

        val iosoutputs = coutputFiles.toMutableList()
        iosoutputs.add(iosXcodeModule.targetDir)

        // apple watch outputs
        val appleWatchOutputs = coutputFiles.toMutableList()
        if (appleWatchXcodeModule != null) {
            appleWatchOutputs.add(appleWatchXcodeModule.targetDir)
        }

        val moduleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()
        val projectTask = project.tasks.register(
            "generateNavAtlasEngine",
            AppleIOSNavigationEngineGeneratorTask::class.java
        ) {
            projectCoreName = moduleName
            iOSOutputFiles = iosoutputs
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/navigation"))
        }

        val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
            include("graphInputHash.txt")
        }
        if (!hashFileTree.isEmpty) {
            projectTask.configure {
                inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            }
        }

        return projectTask
    }

    fun getAppleWatchNavGenTask(project: Project): TaskProvider<AppleWatchNavigationEngineGeneratorTask> {
        val appleClients = AppleProjectFinder.findAllXcodeTargets(project.rootDir, project.logger)
        val appleWatchXcodeModule = appleClients.watchExtensions().firstOrNull()
        val coutputFiles =
            project.rootProject.allprojects.map { File(it.projectDir, "src") }.toMutableList()

        // apple watch outputs
        val appleWatchOutputs = coutputFiles.toMutableList()
        if (appleWatchXcodeModule != null) {
            appleWatchOutputs.add(appleWatchXcodeModule.targetDir)
        }

        val moduleName = project.findProperty("atlas.coreModuleName")?.toString()
            ?: project.getSwiftImportModuleName()

        val projectTask = project.tasks.register(
            "generateNavAtlasEngine",
            AppleWatchNavigationEngineGeneratorTask::class.java
        ) {
            projectCoreName = moduleName
            appleWatchOutputFiles = appleWatchOutputs
            outputAppleWatchDir.set(project.layout.buildDirectory.dir("generated/watchosMain/kotlin/navigation"))
        }

        val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
            include("graphInputHash.txt")
        }
        if (!hashFileTree.isEmpty) {
            projectTask.configure {
                inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
            }
        }

        return projectTask
    }
}

