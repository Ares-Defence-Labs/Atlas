package com.architect.atlas.flows.helpers

import com.architect.atlas.common.helpers.ProjectFinder.getSwiftImportModuleName
import com.architect.atlas.flows.routingEngine.SwiftUIFlowsGeneratorTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    fun getSwiftUIBindingsEngineGenTask(project: Project): TaskProvider<SwiftUIFlowsGeneratorTask> {
        return project.tasks.register(
            "generateSwiftUIAtlasEngine",
            SwiftUIFlowsGeneratorTask::class.java
        ) {
            projectCoreName = project.getSwiftImportModuleName()
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/flows"))
        }
    }
}

