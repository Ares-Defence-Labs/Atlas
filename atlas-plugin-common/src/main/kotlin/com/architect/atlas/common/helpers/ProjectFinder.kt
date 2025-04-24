package com.architect.atlas.common.helpers

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

object ProjectFinder {
    fun isDebugMode(project: Project) =
        project.tasks.any { it.name.contains("debug", ignoreCase = true) }

    fun Project.getSwiftImportModuleName(): String {
        val kotlinExt =
            extensions.findByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
                ?: return "shared"

        val appleTarget = kotlinExt.targets
            .filterIsInstance<KotlinNativeTarget>()
            .firstOrNull { it.konanTarget.family.isAppleFamily }
            ?: return "shared"

        // Get any framework binary configured
        val framework = appleTarget.binaries
            .filterIsInstance<Framework>()
            .firstOrNull()

        return framework?.baseName ?: "shared"
    }


    fun getAndroidAppNamespace(androidProject: Project): String {
        return when (val extension = androidProject.extensions.findByName("android")) {
            is com.android.build.api.dsl.ApplicationExtension -> extension.namespace
            is com.android.build.api.dsl.LibraryExtension -> extension.namespace
            else -> error("Unsupported or missing Android extension in ${androidProject.path}")
        } ?: error("Namespace not defined in Android module ${androidProject.path}")
    }

    fun isAndroidPlatform(project: Project): Boolean {
        return project.plugins.hasPlugin("com.android.application") ||
                project.plugins.hasPlugin("com.android.library")
    }

    fun isBuildingForIos(project: Project): Boolean {
        val iosTargets = listOf(
            "iosArm64",
            "iosSimulatorArm64",
            "iosX64",
            "watchosArm64",
            "watchosX64",
            "watchosSimulatorArm64"
        )

        return project.gradle.taskGraph.allTasks.any { task ->
            iosTargets.any { target ->
                task.name.contains(target, ignoreCase = true)
            }
        }
    }

    fun findIosClientApp(sharedProject: Project): File? {
        val candidates = sharedProject.rootDir.walkTopDown()
            .filter {
                it.isDirectory && it.name.endsWith(".xcodeproj")
            }
            .toList()

        return candidates.firstOrNull()?.parentFile
    }

    fun findAndroidModule(sharedProject: Project): Project? {
        val root = sharedProject.rootProject
        return root.subprojects.firstOrNull { androidProj ->
            androidProj.plugins.hasPlugin("com.android.application") ||
                    androidProj.plugins.hasPlugin("com.android.library")
        }
    }

    fun findAndroidClientApp(sharedProject: Project): Project? {
        return sharedProject.rootProject.subprojects.firstOrNull { subproject ->
            subproject.plugins.hasPlugin("com.android.application")
        }
    }
}

