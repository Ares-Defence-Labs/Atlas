package com.architect.atlas.common.helpers

import org.gradle.api.Project

object TaskDefinitions{
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
            "releaseAssetsCopyForAGP",
            "packageDebugResources",
            "packageReleaseResources",
            "generateDebugResValues"
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

        if (!ProjectFinder.isDebugMode(project)) {
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

}


