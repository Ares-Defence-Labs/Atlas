import com.architect.engine.tasks.AtlasXCodeIncrementalBuildTask
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    id("io.github.thearchitect123.atlasGraphGenerator")
    id("io.github.thearchitect123.atlasResourcesGenerator")
    id("io.github.thearchitect123.atlasNavigationEngineGenerator")
    id("io.github.thearchitect123.incrementalBuildEngine") // latest version
}


project.afterEvaluate{
    tasks.named("extractDeepLinksDebug").configure{
        dependsOn(":shared:generateAtlasImagesGraph")
    }

    tasks.named("generateDebugResources").configure{
        dependsOn(":shared:generateAtlasImagesGraph")
    }

    tasks.named("generateDebugResources").configure{
        dependsOn(":shared:generateAtlasFontsGraph")
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    listOf(iosArm64, iosSimulatorArm64).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            export(projects.atlasCoreShared)
            export(libs.atlas.flow)
            export(libs.coroutines.core)

            val swiftExtras = project.file("src/iosMain/swift")
            if (swiftExtras.exists()) {
                linkerOpts("-F$swiftExtras")
            }
        }
    }

    sourceSets {
        kotlin.applyDefaultHierarchyTemplate()
        val commonMain by getting {
            dependencies {
                implementation("io.github.thearchitect123:kmpEssentials:2.1.3")

                api(projects.atlasCoreShared)
                api(libs.atlas.flow)
                api(libs.coroutines.core)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core:1.15.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
                implementation("androidx.fragment:fragment:1.8.5")
                implementation("androidx.activity:activity:1.10.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
            }
        }

        val iosMain by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
    }
}

android {
    namespace = "com.architect.atlastestclient"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

