package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.navigationEngine.tasks.android.NavEngineGoogleDefaultGenTask
import com.architect.atlas.navigationEngine.tasks.swift.NavEngineUIKitSwiftGenTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

internal object ResPluginHelpers {
    val generateNavEngineUIKit = "generateNavEngineUIKit"
    val generateNavEngineDroidCompose = "generateNavEngineDroidCompose"

    fun getSwiftUIKitGenTask(project: Project): TaskProvider<NavEngineUIKitSwiftGenTask> {
        return project.tasks.register(
            generateNavEngineUIKit,
            NavEngineUIKitSwiftGenTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources"))
        }
    }

    fun getDroidComposeResourceTask(project: Project): TaskProvider<NavEngineGoogleDefaultGenTask> {
        return project.tasks.register(
            generateNavEngineDroidCompose,
            NavEngineGoogleDefaultGenTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
        }
    }
}