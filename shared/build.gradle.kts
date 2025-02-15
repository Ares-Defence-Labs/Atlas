import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

    id("org.gradle.maven-publish")
    id("signing")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
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
    js().browser()

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
                implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.10") // Kotlin Compiler API

                implementation("com.squareup.okio:okio:3.7.0")
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val jvmMain by getting {
            dependencies {

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

//tasks.named("compileKotlinMetadata") {
//    dependsOn("generateDependencyGraph")
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateDependencyGraph")
}

tasks.register("generateDependencyGraph") {
    group = "code generation"
    description = "Generates AtlasContainer at compile time for all modules"

    val outputDir = layout.buildDirectory.dir("generated/commonMain")
    val outputFile = File(outputDir.get().asFile, "AtlasContainer.kt")

    val androidOutputDir = layout.buildDirectory.dir("generated/androidMain")
    val androidOutputFile = File(androidOutputDir.get().asFile, "ViewModelExtensions.kt")

    doFirst {
        val outputFolder = outputDir.get().asFile
        val androidOutputFolder = androidOutputDir.get().asFile

        if (!outputFolder.exists()) outputFolder.mkdirs()
        if (!androidOutputFolder.exists()) androidOutputFolder.mkdirs()

        println("📂 Created output directories: ${outputFolder.absolutePath}, ${androidOutputFolder.absolutePath}")

        if (outputFile.exists()) outputFile.delete()
        if (androidOutputFile.exists()) androidOutputFile.delete()
    }

    doLast {
        val singletons = mutableSetOf<String>()
        val factories = mutableSetOf<String>()
        val scopedInstances = mutableSetOf<String>()
        val viewModels = mutableSetOf<String>()
        val modules = mutableSetOf<String>()

        val annotationMappings = mapOf(
            "com.architect.atlas.container.annotations.Singleton" to singletons,
            "com.architect.atlas.container.annotations.Factory" to factories,
            "com.architect.atlas.container.annotations.Scoped" to scopedInstances,
            "com.architect.atlas.container.annotations.ViewModel" to viewModels,
            "com.architect.atlas.container.annotations.Module" to modules
        )

        project.rootDir.walkTopDown()
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                println("🔍 Scanning: ${sourceDir.absolutePath}")
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    annotationMappings.forEach { (annotation, collection) ->
                        val annotationSimpleName = annotation.substringAfterLast(".")
                        val annotationRegex = "@$annotationSimpleName\\s+class\\s+(\\w+)".toRegex()

                        annotationRegex.findAll(content).forEach { match ->
                            val className = match.groupValues[1]
                            if (content.contains("import $annotation")) {
                                collection.add(className)
                            }
                        }
                    }
                }
            }

        val formattedSingletons = singletons.joinToString("\n        ") { "singletons[${it}::class] = ${it}()" }
        val formattedFactories = factories.joinToString("\n        ") { "factories[${it}::class] = { ${it}() }" }
        val formattedScopedInstances = scopedInstances.joinToString("\n        ") { "scoped[${it}::class] = mutableMapOf()" }
        val formattedViewModels = viewModels.joinToString("\n        ") { "viewModels[${it}::class] = lazy { ${it}() }" }

        // ✅ Generate `AtlasContainer.kt`
        val formattedCode = """
            package com.architect.atlas.container

            import kotlin.reflect.KClass

            object AtlasContainer {
                private val singletons: MutableMap<KClass<*>, Any> = mutableMapOf()
                private val factories: MutableMap<KClass<*>, () -> Any> = mutableMapOf()
                private val scoped: MutableMap<KClass<*>, MutableMap<String, Any>> = mutableMapOf()
                private val viewModels: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()

                init {
                    ${if (singletons.isNotEmpty()) "        $formattedSingletons" else "        // No singletons registered"}
                    ${if (factories.isNotEmpty()) "        $formattedFactories" else "        // No factories registered"}
                    ${if (scopedInstances.isNotEmpty()) "        $formattedScopedInstances" else "        // No scoped instances registered"}
                    ${if (viewModels.isNotEmpty()) "        $formattedViewModels" else "        // No ViewModels registered"}
                }

                fun <T : Any> resolve(clazz: KClass<T>): T {
                    return (singletons[clazz]
                        ?: factories[clazz]?.invoke()
                        ?: viewModels[clazz]?.value) as? T
                        ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
                }
            }
        """.trimIndent()

        outputFile.writeText(formattedCode)
        println("✅ Generated AtlasContainer.kt at: ${outputFile.absolutePath}")

        // ✅ Generate Android-specific ViewModel Delegate Extensions
        val androidCode = """
            package com.architect.atlas.container.android

            import androidx.activity.ComponentActivity
            import androidx.fragment.app.Fragment
            import androidx.appcompat.app.AppCompatActivity
            import kotlin.properties.ReadOnlyProperty

            inline fun <reified T : Any> Fragment.viewModels(): ReadOnlyProperty<Fragment, T> {
                return ReadOnlyProperty { thisRef, _ ->
                    AtlasContainer.resolve(T::class)
                }
            }

            inline fun <reified T : Any> AppCompatActivity.viewModels(): ReadOnlyProperty<AppCompatActivity, T> {
                return ReadOnlyProperty { thisRef, _ ->
                    AtlasContainer.resolve(T::class)
                }
            }

            inline fun <reified T : Any> ComponentActivity.viewModels(): ReadOnlyProperty<ComponentActivity, T> {
                return ReadOnlyProperty { thisRef, _ ->
                    AtlasContainer.resolve(T::class)
                }
            }
        """.trimIndent()

        androidOutputFile.writeText(androidCode)
        println("✅ Generated ViewModelExtensions.kt at: ${androidOutputFile.absolutePath}")
    }
}

afterEvaluate {
    tasks.named("generateDependencyGraph").configure {
        dependsOn("compileKotlinMetadata")
    }

    mavenPublishing {
        // Define coordinates for the published artifact
        coordinates(
            groupId = "io.github.thearchitect123",
            artifactId = "atlas-core",
            version = "0.0.5"
        )

        // Configure POM metadata for the published artifact
        pom {
            name.set("Atlas")
            description.set("Atlas is a powerful Kotlin Multiplatform (KMP) SDK that provides a complete ecosystem for building scalable, structured, and maintainable applications across JVM, Android, iOS, JS, and Native. It combines MVVM architecture, navigation, CLI tools, and an IoC container into one seamless experience.")
            inceptionYear.set("2024")
            url.set("https://github.com/TheArchitect123/Atlas")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            // Specify developers information
            developers {
                developer {
                    id.set("Dan Gerchcovich")
                    name.set("TheArchitect123")
                    email.set("dan.developer789@gmail.com")
                }
            }

            // Specify SCM information
            scm {
                url.set("https://github.com/TheArchitect123/Atlas")
            }
        }

        // Configure publishing to Maven Central
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

        // Enable GPG signing for all publications
        signAllPublications()
    }
}
//
//signing {
//    val privateKeyFile = project.properties["signing.privateKeyFile"] as? String
//        ?: error("No Private key file found")
//    val passphrase = project.properties["signing.password"] as? String
//        ?: error("No Passphrase found for signing")
//
//    // Read the private key from the file
//    val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)
//
//    useInMemoryPgpKeys(privateKey, passphrase)
//    sign(publishing.publications)
//}
