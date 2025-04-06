package com.architect.atlas.common.helpers

import org.gradle.api.Project

object SourceSetsHelper{
    fun prepareResourcesDirectory(project: Project) {
        // 🧹 Clean the output directory first (to force regeneration)
        val outputBase =
            project.layout.buildDirectory.dir("generated/commonMain/resources").get().asFile
        if (outputBase.exists()) {
            outputBase.deleteRecursively()
        }
        outputBase.mkdirs() // destroy the tasks
    }
}