package com.architect.atlas.common.helpers

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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

    fun isBuildingForAppleWatch(project: Project): Boolean {
        val requestedTasks = project.gradle.startParameter.taskNames
        val gradleLooksWatch = requestedTasks.any { it.contains("watchos", ignoreCase = true) } ||
                (project.gradle.taskGraph.allTasks?.any { it.name.contains("watchos", ignoreCase = true) } ?: false)

        val env = System.getenv()
        val sdkName              = env["SDK_NAME"].orEmpty()
        val effectivePlatform    = env["EFFECTIVE_PLATFORM_NAME"].orEmpty()
        val platformName         = env["PLATFORM_NAME"].orEmpty()
        val targetName           = env["TARGET_NAME"].orEmpty()
        val productBundle        = env["WRAPPER_EXTENSION"].orEmpty()

        val xcodeLooksWatch = listOf(sdkName, effectivePlatform, platformName, targetName)
            .any { it.contains("watch", ignoreCase = true) }

        return gradleLooksWatch || xcodeLooksWatch
    }

    fun isBuildingForIPhone(project: Project): Boolean {
        val requestedTasks = project.gradle.startParameter.taskNames
        val gradleLooksIos = requestedTasks.any { it.contains("ios", ignoreCase = true) && !it.contains("watch", true) } ||
                (project.gradle.taskGraph.allTasks.any { it.name.contains("ios", ignoreCase = true) && !it.name.contains("watch", true) } ?: false)

        val env = System.getenv()
        val sdkName           = env["SDK_NAME"].orEmpty()
        val effectivePlatform = env["EFFECTIVE_PLATFORM_NAME"].orEmpty()
        val platformName      = env["PLATFORM_NAME"].orEmpty()
        val targetName        = env["TARGET_NAME"].orEmpty()

        val xcodeLooksIphone = listOf(sdkName, effectivePlatform, platformName).any {
            it.contains("iphone", ignoreCase = true)
        } && !targetName.contains("Watch", ignoreCase = true)

        return gradleLooksIos || xcodeLooksIphone
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
        } || (System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("simulator") == true
                || System.getenv("EFFECTIVE_PLATFORM_NAME")?.contains("iphone") == true)
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

    fun findWearApp(sharedProject: Project): Project? {
        return sharedProject.rootProject.subprojects.firstOrNull { subproject ->
            subproject.name.contains("wear", ignoreCase = true)
        }
    }

    fun isBuildingForAndroid(project: Project): Boolean {
        val requestedTasks = project.gradle.startParameter.taskNames
        return requestedTasks.any { taskName ->
            taskName.contains("assemble", ignoreCase = true) ||
                    taskName.contains("build", ignoreCase = true) ||
                    taskName.contains("install", ignoreCase = true)
        }
    }
}

