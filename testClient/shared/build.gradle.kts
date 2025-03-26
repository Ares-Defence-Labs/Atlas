plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    //id("io.github.thearchitect123.atlasGraphGenerator") version "0.5.8"
    //id("io.github.thearchitect123.atlasGraphGenerator")
    id("io.github.thearchitect123.atlasResourcesGenerator")
}

val copySwiftExtensionsDebug by tasks.registering(Copy::class) {
    val swiftFile = file("src/iosMain/swift")
    val frameworkModulesDir = buildDir.resolve("bin/iosX64/debugFramework/Shared.framework/Modules")

    from(swiftFile)
    into(frameworkModulesDir)
}
tasks.named("linkDebugFrameworkIosX64") {
    finalizedBy(copySwiftExtensionsDebug)
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

            val swiftExtras = project.file("src/iosMain/swift")
            if (swiftExtras.exists()) {
                linkerOpts("-F$swiftExtras")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDirs("build/generated/commonMain/kotlin", "build/generated/commonMain/resources")
            dependencies {
                implementation("io.github.thearchitect123:kmpEssentials:2.1.3")
                //api(projects.atlasCoreShared)

                api(libs.atlas.core)
            }
        }

        val androidMain by getting {
            kotlin.srcDirs("build/generated/androidMain/kotlin", "build/generated/androidMain/resources")
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
            kotlin.srcDirs("build/generated/iosMain/kotlin", "build/generated/iosMain/resources")
            dependsOn(commonMain) // ✅ Ensure iOS depends on `commonMain`
        }

        // ✅ Ensure iOS targets use iosMain
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}


private val stringsGenTask = "generateAtlasStringsGraph"
private val colorGenTask = "generateAtlasColorsGraph"
private val imageGenTask = "generateAtlasImagesGraph"
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(stringsGenTask)
    dependsOn(colorGenTask)
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


