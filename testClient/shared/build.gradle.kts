plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    //id("io.github.thearchitect123.atlasGraphGenerator") version "0.3.3"
    id("io.github.thearchitect123.atlasGraphGenerator")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }



    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/commonMain/kotlin")
            dependencies {
                implementation("io.github.thearchitect123:kmpEssentials:2.1.3")
                implementation("io.github.thearchitect123:atlas-core:0.1.9")
            }
        }

        val androidMain by getting {
            kotlin.srcDir("build/generated/androidMain/kotlin")
            dependencies {
                implementation("androidx.core:core:1.15.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
                implementation("androidx.fragment:fragment:1.8.5")
                implementation("androidx.activity:activity:1.10.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain) // ✅ Ensure iOS depends on `commonMain`
        }

        // ✅ Ensure iOS targets use iosMain
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateDependencyGraph")
}

android {
    namespace = "com.architect.atlastestclient"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


