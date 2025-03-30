import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
//    // watchOS (Apple)
//    watchosArm64()
//    watchosArm32()
//    watchosX64()
//    watchosSimulatorArm64()
//
//    // java target -- Compose UI (Mac, Linux, Desktop)
//    jvm()
//
//    // tvos (Apple)
//    tvosX64()
//    tvosArm64()
//    tvosSimulatorArm64()
//
//    // browser (Kotlin/JS)
//    js(IR) { // Kotlin JS with IR (default)
//        browser() // Browser Target
//        nodejs() // Node.js Target
//    }
//

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true

            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            transitiveExport = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.live.data)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {

            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }


//        //
//        val watchosMain by creating {
//            dependsOn(commonMain)
//            dependencies {
//
//            }
//        }
//
//        val watchosX64Main by getting { dependsOn(watchosMain) }
//        val watchosArm32Main by getting { dependsOn(watchosMain) }
//        val watchosArm64Main by getting { dependsOn(watchosMain) }
//        val watchosSimulatorArm64Main by getting { dependsOn(watchosMain) }
    }
}

android {
    namespace = "com.example.atlas_live_data"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
