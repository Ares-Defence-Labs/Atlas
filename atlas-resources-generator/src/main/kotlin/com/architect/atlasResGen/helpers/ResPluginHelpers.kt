package com.architect.atlasResGen.helpers

import com.android.build.api.dsl.CommonExtension
import com.architect.atlasResGen.tasks.colors.AtlasColorsPluginTask
import com.architect.atlasResGen.tasks.fonts.AtlasFontPluginTask
import com.architect.atlasResGen.tasks.images.AtlasImagePluginTask
import com.architect.atlasResGen.tasks.strings.AtlasStringPluginTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal object ResPluginHelpers {
    private val stringsGenTask = "generateAtlasStringsGraph"
    private val colorGenTask = "generateAtlasColorsGraph"
    private val imageGenTask = "generateAtlasImagesGraph"
    private val fontsGenTask = "generateAtlasFontsGraph"
    fun isDebugMode(project: Project) =
        project.tasks.any { it.name.contains("debug", ignoreCase = true) }

    fun getiOSTaskDependencies(): List<String> {
        return listOf(
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
            "xcodeVersion",
        )
    }

    fun getExtraTaskDependencies(): List<String> {
        return listOf(
            "compileDebugKotlin",
            "compileReleaseKotlin",
            "javaPreCompileDebug",
        )
    }

    fun getAndroidTaskDependencies(project: Project): List<String> {
        val androidTasks = mutableListOf(
//            "bundleLibRuntimeToDirDebug",
//            "bundleLibCompileToJarDebug",
//            "processDebugJavaRes",
//            "compileDebugJavaWithJavac",
//            "compileDebugKotlinAndroid",
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

        if (!isDebugMode(project)) {
            // if running on release mode, then add the tasks that are missing from the implementation
            androidTasks.add("copyReleaseJniLibsProjectOnly")
            androidTasks.add("writeReleaseLintModelMetadata")
            androidTasks.add("mergeReleaseAssets")
            androidTasks.add("extractProguardFiles")
            androidTasks.add("mergeReleaseShaders")
            androidTasks.add("compileReleaseShaders")
        }

        return androidTasks
    }

    fun attachDependenciesToGraph(
        project: Project,
        generateDependencyGraphTask: Task,
        androidTasks: List<String>
    ) {
        project.rootProject.allprojects.forEach { subProject ->
            androidTasks.forEach { taskName ->
                val dependencyTask: Task? = subProject.tasks.findByName(taskName)

                if (dependencyTask != null) {
                    generateDependencyGraphTask.dependsOn(dependencyTask)
                    project.logger.lifecycle("✅ `${dependencyTask.name}` now depends on `:${subProject.name}:$taskName`")
                } else {
                    project.logger.lifecycle("⚠️ `:${subProject.name}:$taskName` not found")
                }
            }
        }
    }

    fun getStringResourceTask(project: Project): TaskProvider<AtlasStringPluginTask> {
        return project.tasks.register(
            stringsGenTask,
            AtlasStringPluginTask::class.java
        ) {
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
            isAndroidTarget = project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library")
        }
    }

    private fun getAndroidAppNamespace(androidProject: Project): String {
        return when (val extension = androidProject.extensions.findByName("android")) {
            is com.android.build.api.dsl.ApplicationExtension -> extension.namespace
            is com.android.build.api.dsl.LibraryExtension -> extension.namespace
            else -> error("Unsupported or missing Android extension in ${androidProject.path}")
        } ?: error("Namespace not defined in Android module ${androidProject.path}")
    }

    fun getImageResourceTask(project: Project): TaskProvider<AtlasImagePluginTask> {
        val androidProject = findAndroidModule(project)
        if (androidProject == null) {
            project.logger.warn("⚠️ AtlasImagePlugin: Could not locate Android module. Skipping Android asset integration.")
        }

        val isAndroid = androidProject != null
        return project.tasks.register(
            imageGenTask,
            AtlasImagePluginTask::class.java
        ) {
            androidResourcePackageRef =
                if (isAndroid) getAndroidAppNamespace(androidProject!!) else ""
            androidAssetImageDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/assets/images"))
            androidResourcesDrawableDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/res"))
            projectBuildDir.set(project.layout.buildDirectory)
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources"))
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            if (isAndroid) androidOutputDir.set(androidProject!!.layout.buildDirectory.dir("generated/kotlin/resources"))
            isAndroidTarget = isAndroid
        }
    }

    fun findAndroidModule(sharedProject: Project): Project? {
        val root = sharedProject.rootProject
        return root.subprojects.firstOrNull { androidProj ->
            androidProj.plugins.hasPlugin("com.android.application") ||
                    androidProj.plugins.hasPlugin("com.android.library")
        }
    }

    fun findAndroidClientApp(sharedProject: Project): Project? {
        return sharedProject.rootProject.subprojects.firstOrNull { it.name == "androidApp" }
    }

    fun getFontsResourceTask(project: Project): TaskProvider<AtlasFontPluginTask> {
        val androidProject = findAndroidModule(project)
        if (androidProject == null) {
            project.logger.warn("⚠️ AtlasImagePlugin: Could not locate Android module. Skipping Android asset integration.")
        }

        val isAndroid = androidProject != null
        return project.tasks.register(
            fontsGenTask,
            AtlasFontPluginTask::class.java
        ) {
            androidResourcesFontsDir.set(androidProject?.layout?.projectDirectory?.dir("src/main/res/font"))
            androidResourcePackageRef =
                if (isAndroid) getAndroidAppNamespace(androidProject!!) else ""
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            if (isAndroid) androidOutputDir.set(androidProject!!.layout.buildDirectory.dir("generated/kotlin/resources"))
            isAndroidTarget = isAndroid
        }
    }

    fun getColorsResourceTask(project: Project): TaskProvider<AtlasColorsPluginTask> {
        return project.tasks.register(
            colorGenTask,
            AtlasColorsPluginTask::class.java
        ) {
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources"))
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/commonMain/resources"))
            androidOutputDir.set(project.layout.buildDirectory.dir("generated/androidMain/resources"))
            isAndroidTarget = project.plugins.hasPlugin("com.android.application") ||
                    project.plugins.hasPlugin("com.android.library")
        }
    }

    fun prepareResourcesDirectory(project: Project) {
        // 🧹 Clean the output directory first (to force regeneration)
        val outputBase =
            project.layout.buildDirectory.dir("generated/commonMain/resources").get().asFile
        if (outputBase.exists()) {
            outputBase.deleteRecursively()
        }
        outputBase.mkdirs() // destroy the tasks
    }


    fun toSnakeCase(name: String): String {
        return name.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .replace(Regex("_+"), "_")
            .lowercase()
    }

    fun Project.configureKotlinSourceSets() {
        val kotlinExt = extensions.findByName("kotlin") ?: return
        (kotlinExt as? org.gradle.api.plugins.ExtensionAware)?.let {
            val sourceSets = it.extensions.getByName("sourceSets") as NamedDomainObjectContainer<*>

            // Find androidMain and iosMain and add generated source dirs
            (sourceSets.findByName("androidMain") as? KotlinSourceSet)?.kotlin?.srcDir("$buildDir/generated/androidMain")
            (sourceSets.findByName("iosMain") as? KotlinSourceSet)?.kotlin?.srcDir("$buildDir/generated/iosMain")
        }
    }

    fun Project.configureKmpGeneratedSourceSets() {
        extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmp ->
            kmp.sourceSets.findByName("androidMain")?.kotlin?.srcDir("$buildDir/generated/androidMain")
            kmp.sourceSets.findByName("iosMain")?.kotlin?.srcDir("$buildDir/generated/iosMain")
        }
    }
}