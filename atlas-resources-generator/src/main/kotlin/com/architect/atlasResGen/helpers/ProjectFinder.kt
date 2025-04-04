package com.architect.atlasResGen.helpers

import org.gradle.api.Project
import java.io.File

object ProjectFinder {
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