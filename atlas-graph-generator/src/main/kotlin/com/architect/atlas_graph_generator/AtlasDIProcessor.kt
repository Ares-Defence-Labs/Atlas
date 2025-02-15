package com.architect.atlasGraphGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        val task = project.tasks.register("generateDependencyGraph", AtlasDIProcessorGraphTask::class.java) {
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/kotlin"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/kotlin"))
        }

        // ✅ Ensure `generateDependencyGraph` runs before **any** Kotlin compilation
        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(task)
        }

        // ✅ Ensure the task runs for **Android Projects** (Application & Library)
        project.tasks.matching { it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains("Android") }.configureEach {
            dependsOn(task)
        }

        // ✅ Ensure the task runs before **Metadata Compilation (Multiplatform)**
        project.tasks.named("compileKotlinMetadata").configure {
            dependsOn(task)
        }
    }
}