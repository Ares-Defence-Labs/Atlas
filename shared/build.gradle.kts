plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.google.devtools.ksp") version "2.1.10-1.0.29"
}

kotlin {
    kotlin.applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    // watchOS (Apple)
//    watchosArm64()
//    watchosArm32()
//    watchosX64()
//    watchosSimulatorArm64()

    // java target -- Compose UI (Mac, Linux, Desktop)
    jvm()

    // tvos (Apple)
//    tvosX64()
//    tvosArm64()
//    tvosSimulatorArm64()

    // browser (Kotlin/JS)
    //js().browser()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting
        {

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("com.google.auto.service:auto-service:1.1.1")
                implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            dependsOn(jvmMain)

            dependencies {
                implementation("androidx.core:core:1.15.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

            }
        }

        val iosArm64Main by getting
        val iosX64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependencies {
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
    }
}

android {
    namespace = "com.architect.atlas"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
