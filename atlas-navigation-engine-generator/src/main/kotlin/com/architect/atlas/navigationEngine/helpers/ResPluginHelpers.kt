package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.navigationEngine.tasks.routingEngine.NavigationEngineGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

internal object ResPluginHelpers {


    fun Project.getSwiftImportModuleName(): String {
        val kotlinExt =
            extensions.findByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
                ?: return "shared"

        val appleTarget = kotlinExt.targets
            .filterIsInstance<KotlinNativeTarget>()
            .firstOrNull { it.konanTarget.family.isAppleFamily }

        val framework: Framework? =
            appleTarget?.binaries?.getFramework("DEBUG") // or "RELEASE" based on context

        return framework?.baseName ?: "shared"
    }

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

