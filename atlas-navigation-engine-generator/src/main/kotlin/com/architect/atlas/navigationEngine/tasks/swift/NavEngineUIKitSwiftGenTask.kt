package com.architect.atlas.navigationEngine.tasks.swift

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class NavEngineUIKitSwiftGenTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    init {
        group = "AtlasNavigation"
        description = "Generates a navigation engine using the default Google Navigation Library & the xml file provided"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateColorClass() {
    }
}