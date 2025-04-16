plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    //id("io.github.thearchitect123.atlasGraphGenerator") version "0.5.8"
    id("io.github.thearchitect123.atlasGraphGenerator")
    id("io.github.thearchitect123.atlasResourcesGenerator")
    id("io.github.thearchitect123.atlasNavigationEngineGenerator")
    id("io.github.thearchitect123.atlasFlowsGenPlugin")
}
//
//val copySwiftExtensionsDebug by tasks.registering(Copy::class) {
//    val swiftFile = file("src/iosMain/swift")
//    val frameworkModulesDir = buildDir.resolve("bin/iosX64/debugFramework/Shared.framework/Modules")
//
//    from(swiftFile)
//    into(frameworkModulesDir)
//}
//tasks.named("linkDebugFrameworkIosX64") {
//    finalizedBy(copySwiftExtensionsDebug)
//}


tasks.named("generateNavAtlasEngine").configure{
    mustRunAfter("appleFontsPackagingGenTask")
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
            export(libs.atlas.core)
            export(libs.atlas.flow)
            export(libs.coroutines.core)

            val swiftExtras = project.file("src/iosMain/swift")
            if (swiftExtras.exists()) {
                linkerOpts("-F$swiftExtras")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.thearchitect123:kmpEssentials:2.1.3")

                api(libs.atlas.core)
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

        val iosMain by creating {
            dependsOn(commonMain) // ✅ Ensure iOS depends on `commonMain`
        }

        // ✅ Ensure iOS targets use iosMain
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
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


