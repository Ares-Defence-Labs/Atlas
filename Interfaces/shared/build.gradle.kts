import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    id("org.gradle.maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("kotlin-parcelize")
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
    watchosArm64()
    watchosArm32()
    watchosX64()
    watchosSimulatorArm64()

    // java target -- Compose UI (Mac, Linux, Desktop)
    jvm()

    // tvos (Apple)
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    // browser (Kotlin/JS)
    js(IR) { // Kotlin JS with IR (default)
        browser() // Browser Target
        nodejs() // Node.js Target
    }

    // 🟢 WebAssembly (WASM) - Experimental
    wasmJs()
    wasmWasi()

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
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
            }
        }

        commonTest.dependencies {
        }

        val jvmMain by getting {
            dependencies {

            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.github.thearchitect123:kmpEssentials:2.1.3")
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
////////////////////////
//mavenPublishing {
//    // Define coordinates for the published artifact
//    coordinates(
//        groupId = "io.github.thearchitect123",
//        artifactId = "atlas-core",
//        version = "0.6.7"
//    )
//
//    // Configure POM metadata for the published artifact
//    pom {
//        name.set("Atlas")
//        description.set("Atlas is a powerful Kotlin Multiplatform (KMP) SDK that provides a complete ecosystem for building scalable, structured, and maintainable applications across JVM, Android, iOS, JS, and Native. It combines MVVM architecture, navigation, CLI tools, and an IoC container into one seamless experience.")
//        inceptionYear.set("2024")
//        url.set("https://github.com/TheArchitect123/Atlas")
//
//        licenses {
//            license {
//                name.set("MIT")
//                url.set("https://opensource.org/licenses/MIT")
//            }
//        }
//
//        // Specify developers information
//        developers {
//            developer {
//                id.set("Dan Gerchcovich")
//                name.set("TheArchitect123")
//                email.set("dan.developer789@gmail.com")
//            }
//        }
//
//        // Specify SCM information
//        scm {
//            url.set("https://github.com/TheArchitect123/Atlas")
//        }
//    }
//
//    // Configure publishing to Maven Central
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//
//    // Enable GPG signing for all publications
//    signAllPublications()
//}
//
//signing {
//    val privateKeyFile = project.findProperty("signing.privateKeyFile") as? String
//        ?: error("No Private key file found")
//    val passphrase = project.findProperty("signing.password") as? String
//        ?: error("No Passphrase found for signing")
//
//    // Read the private key from the file
//    val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)
//
//    useInMemoryPgpKeys(privateKey, passphrase)
//    sign(publishing.publications)
//}