package com.architect.atlasGraphGenerator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AtlasDIProcessor : Plugin<Project> {
    private val stringsGenTask = "generateAtlasStringsGraph"
    private val colorGenTask = "generateAtlasColorsGraph"
    private val imageGenTask = "generateAtlasImagesGraph"
    private val fontsGenTask = "generateAtlasFontsGraph"

    private fun configureBuildFolders(project: Project) {
        val kmpExt =
            project.extensions.getByName("kotlin") as? org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
                ?: error("Kotlin Multiplatform plugin not applied")

        val commonMain = kmpExt.sourceSets.getByName("commonMain")
        commonMain.kotlin.srcDirs(
            project.layout.buildDirectory.dir("generated/commonMain/kotlin"),
        )
    }

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

        if (!isDebugMode) {
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

    private fun configureResourceModuleTask(
        project: Project,
        generateDependencyGraphTask: Task,
    ) {
        val stringsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(stringsGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$stringsGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val fontsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(fontsGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$fontsGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val imagesResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(imageGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$imageGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()
        val colorsResourceTask = project.rootProject.allprojects
            .mapNotNull { subProject ->
                subProject.tasks.findByName(colorGenTask)?.also {
                    project.logger.lifecycle("🔍 Found `$colorGenTask` in ${subProject.path}")
                }
            }
            .firstOrNull()

        val usingResourceMode =
            stringsResourceTask != null && fontsResourceTask != null && imagesResourceTask != null && colorsResourceTask != null
        if (usingResourceMode) { // if using resources module, requires dependency to avoid issues with kotlin compile
            generateDependencyGraphTask.dependsOn(stringsResourceTask)
            generateDependencyGraphTask.dependsOn(fontsResourceTask)
            generateDependencyGraphTask.dependsOn(imagesResourceTask)
            generateDependencyGraphTask.dependsOn(colorsResourceTask)
        }
    }

    private fun attachDependenciesToGraph(
        project: Project,
        generateDependencyGraphTask: Task,
        androidTasks: List<String>
    ) {
        configureBuildFolders(project)
        configureResourceModuleTask(project, generateDependencyGraphTask)
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