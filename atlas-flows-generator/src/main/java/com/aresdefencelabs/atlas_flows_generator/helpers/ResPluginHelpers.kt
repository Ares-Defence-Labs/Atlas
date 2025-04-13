package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.navigationEngine.tasks.routingEngine.SwiftUIFlowsGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
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

    fun getSwiftUIBindingsEngineGenTask(project: Project): TaskProvider<SwiftUIFlowsGeneratorTask> {
        return project.tasks.register(
            "generateSwiftUIAtlasEngine",
            SwiftUIFlowsGeneratorTask::class.java
        ) {
            projectCoreName = project.getSwiftImportModuleName()
            projectRootDir.set(project.layout.projectDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/flows"))
        }
    }
}

