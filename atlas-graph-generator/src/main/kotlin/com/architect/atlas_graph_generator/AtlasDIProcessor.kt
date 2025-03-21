package com.architect.atlasGraphGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        val isDebugMode = project.tasks.any { it.name.contains("debug", ignoreCase = true) }
        val generateDependencyGraphTask = project.tasks.register(
            "generateDependencyGraph",
            AtlasDIProcessorGraphTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/kotlin"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/kotlin"))
            isAndroidTarget = project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library")
        }

        // ✅ Ensure `generateDependencyGraph` runs **before** Kotlin compilation
        project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        // ✅ Ensure `generateDependencyGraph` runs before **Android Kotlin compilation**
        project.tasks.matching {
            it.name.contains("compile") && it.name.contains("Kotlin") && it.name.contains(
                "Android"
            )
        }.configureEach {
            dependsOn(generateDependencyGraphTask)
        }

        val requiredTasks = listOf(
            "transformAppleMainDependenciesMetadata",
            "transformIosMainDependenciesMetadata",
            "transformNativeMainDependenciesMetadata",
            "metadataAppleMainProcessResources",
            "metadataIosMainProcessResources",
            "compileIosMainKotlinMetadata",
            "kspKotlinIosArm64",
            "generateMRiosArm64Main",
            "kspKotlinIosSimulatorArm64",
            "generateMRiosSimulatorMain",
            "xcodeVersion"
        )

        requiredTasks.forEach { taskName ->
            val dependencyTask = project.tasks.findByName(taskName)
            if (dependencyTask != null) {
                //dependencyTask.mustRunAfter(generateDependencyGraphTask)
                generateDependencyGraphTask.configure { dependsOn(dependencyTask) }
            } else {
                project.logger.lifecycle("⚠️ Task `$taskName` not found. Skipping dependency assignment.")
            }
        }

        val androidTasks = mutableListOf(
            "mergeDebugResources",
            "debugAssetsCopyForAGP",
            "generateDebugResValues",
            "processDebugManifest",
            "writeDebugAarMetadata",
            "mergeDebugShaders",
            "generateDebugResources",
            "extractDeepLinksDebug",
            "mergeDebugJniLibFolders",
            "copyDebugJniLibsProjectAndLocalJars",
            "copyDebugJniLibsProjectOnly",
            "packageDebugResources",
            "parseDebugLocalResources",
            "compileDebugLibraryResources",
            "javaPreCompileDebug",
            "packageDebugAssets",
            "compileDebugShaders",
            "generateDebugRFile",
            "processDebugAndroidTestResources",
            "javaPreCompileDebugAndroidTest",
            "javaPreCompileDebugUnitTest",
            "mergeDebugAndroidTestAssets",
            "generateDebugUnitTestStubRFile",
            "compressDebugAndroidTestAssets",
            "mergeExtDexDebugAndroidTest",
            "checkDebugAndroidTestDuplicateClasses",
            "desugarDebugAndroidTestFileDependencies",
            "mergeDebugAndroidTestJniLibFolders",
            "prepareDebugArtProfile",
            "prepareLintJarForPublish",
            "mergeDebugAndroidTestNativeLibs",
            "extractDeepLinksForAarDebug",
            "writeDebugAndroidTestSigningConfigVersions",
            "stripDebugAndroidTestDebugSymbols",
            "validateSigningDebugAndroidTest",
            "compileCommonMainKotlinMetadata",
            "releaseAssetsCopyForAGP",
            "packageReleaseResources",
            "generateReleaseResValues",
            "allMetadataJar",
            "writeReleaseAarMetadata",
            "javaPreCompileRelease",
            "mergeReleaseResources",
            "compileIosMainKotlinMetadata",
            "compileNativeMainKotlinMetadata",
            "processReleaseManifest",
            "generateReleaseRFile",
            "mergeReleaseJniLibFolders",
            "packageReleaseAssets",
            "copyReleaseJniLibsProjectAndLocalJars",
            "prepareReleaseArtProfile",
            "extractDeepLinksForAarRelease",
            "verifyReleaseResources",
        )

        if(!isDebugMode){
            // if running on release mode, then add the tasks that are missing from the implementation
            androidTasks.add("copyReleaseJniLibsProjectOnly")
            androidTasks.add("writeReleaseLintModelMetadata")
            androidTasks.add("mergeReleaseAssets")
            androidTasks.add("extractProguardFiles")
            androidTasks.add("mergeReleaseShaders")
            androidTasks.add("compileReleaseShaders")
        }

        if (project.state.executed) {
            // **Project is already evaluated, use taskGraph.whenReady**
            project.gradle.taskGraph.whenReady {
                attachDependenciesToGraph(project, generateDependencyGraphTask.get(), androidTasks)
            }
        } else {
            // **Project is not yet evaluated, use `afterEvaluate`**
            project.afterEvaluate {
                attachDependenciesToGraph(project, generateDependencyGraphTask.get(), androidTasks)
            }
        }
    }

    private fun attachDependenciesToGraph(
        project: Project,
        generateDependencyGraphTask: Task,
        androidTasks: List<String>
    ) {
        project.rootProject.allprojects.forEach { subProject ->
            androidTasks.forEach { taskName ->
                val dependencyTask: Task? = subProject.tasks.findByName(taskName)

                if (dependencyTask != null) {
                    generateDependencyGraphTask.dependsOn(dependencyTask)
                    project.logger.lifecycle("✅ `generateDependencyGraph` now depends on `:${subProject.name}:$taskName`")
                } else {
                    project.logger.lifecycle("⚠️ `:${subProject.name}:$taskName` not found. Skipping dependency assignment.")
                }
            }
        }
    }
}