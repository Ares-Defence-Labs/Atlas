package com.architect.atlasGraphGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        val task = project.tasks.register("generateDependencyGraph", AtlasDIProcessorGraphTask::class.java) {
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/kotlin"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/kotlin"))
        }

        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(task)
        }

        project.tasks.named("compileKotlinMetadata").configure {
            dependsOn(task)
        }

        project.tasks.named("compileKotlinAndroid").configure {
            dependsOn(task)
        }
    }
}