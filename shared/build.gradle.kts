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


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // ✅ Enforce Java 17
    }
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
            kotlin.srcDirs("build/generated/commonMain")
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0") // Use the same Kotlin version as your project
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {

            }
        }
        val androidMain by getting {
            dependsOn(commonMain)

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

tasks.named("compileKotlinMetadata") {
    dependsOn("generateDependencyGraph")
}
tasks.register("generateDependencyGraph") {
    group = "code generation"
    description = "Generates AtlasContainer at compile time for all modules"

    val projectRoot = project.rootDir
    val outputDir = layout.buildDirectory.dir("generated/commonMain")
    val outputFile = File(outputDir.get().asFile, "AtlasContainer.kt")

    doFirst {
        // Ensure output directory exists
        val outputFolder = outputDir.get().asFile
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
            println("📂 Created output directory: ${outputFolder.absolutePath}")
        }

        // Delete old AtlasContainer.kt before regenerating
        if (outputFile.exists()) {
            outputFile.delete()
            println("🗑️ Deleted old AtlasContainer.kt to ensure a fresh build")
        }
    }

    doLast {
        val singletons = mutableSetOf<String>()
        val factories = mutableSetOf<String>()
        val scopedInstances = mutableSetOf<String>()
        val viewModels = mutableSetOf<String>()
        val modules = mutableSetOf<String>()

        // Fully qualified annotation names from your shared module
        val annotationMappings = mapOf(
            "com.architect.atlas.container.annotations.Singleton" to singletons,
            "com.architect.atlas.container.annotations.Factory" to factories,
            "com.architect.atlas.container.annotations.Scoped" to scopedInstances,
            "com.architect.atlas.container.annotations.ViewModel" to viewModels,
            "com.architect.atlas.container.annotations.Module" to modules
        )

        // Scan all modules for Kotlin source files
        projectRoot.walkTopDown()
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                println("🔍 Scanning: ${sourceDir.absolutePath}")
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    // Process annotations correctly
                    annotationMappings.forEach { (annotation, collection) ->
                        val annotationSimpleName = annotation.substringAfterLast(".") // Extract 'Singleton' from full path
                        val annotationRegex = "@$annotationSimpleName\\s+class\\s+(\\w+)".toRegex()

                        annotationRegex.findAll(content).forEach { match ->
                            val className = match.groupValues[1]

                            // Ensure the annotation actually comes from `com.architect.atlas.container.annotations`
                            if (content.contains("import $annotation")) {
                                collection.add(className) // Ensures uniqueness
                            }
                        }
                    }
                }
            }

        // Ensure output directory exists before writing the file
        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        // Properly formatted dependency registrations
        val formattedSingletons = singletons.joinToString("\n        ") { "singletons[${it}::class] = ${it}()" }
        val formattedFactories = factories.joinToString("\n        ") { "factories[${it}::class] = { ${it}() }" }
        val formattedScopedInstances = scopedInstances.joinToString("\n        ") { "scoped[${it}::class] = mutableMapOf()" }
        val formattedViewModels = viewModels.joinToString("\n        ") { "viewModels[${it}::class] = lazy { ${it}() }" }

        // Generate clean and formatted `AtlasContainer.kt`
        val formattedCode = """
            package com.architect.atlas.container

            import kotlin.reflect.KClass

            object AtlasContainer {
                private val singletons: MutableMap<KClass<*>, Any> = mutableMapOf()
                private val factories: MutableMap<KClass<*>, () -> Any> = mutableMapOf()
                private val scoped: MutableMap<KClass<*>, MutableMap<String, Any>> = mutableMapOf()
                private val viewModels: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()

                init {
                    // ✅ Singleton instances (shared across the entire app)
                    ${if (singletons.isNotEmpty()) "        $formattedSingletons" else "        // No singletons registered"}

                    // ✅ Factory instances (new instance every request)
                    ${if (factories.isNotEmpty()) "        $formattedFactories" else "        // No factories registered"}

                    // ✅ Scoped instances (unique instance per scope)
                    ${if (scopedInstances.isNotEmpty()) "        $formattedScopedInstances" else "        // No scoped instances registered"}

                    // ✅ ViewModel instances (created once per class)
                    ${if (viewModels.isNotEmpty()) "        $formattedViewModels" else "        // No ViewModels registered"}
                }

                /**
                 * Resolves a dependency.
                 * - Returns a Singleton if available.
                 * - If not, creates a new instance using Factory.
                 * - If not found, tries ViewModel storage.
                 * - Throws an exception if no matching class is found.
                 */
                fun <T : Any> resolve(clazz: KClass<T>): T {
                    return (singletons[clazz]
                        ?: factories[clazz]?.invoke()
                        ?: viewModels[clazz]?.value) as? T
                        ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
                }

                /**
                 * Resolves a Scoped instance per scope ID.
                 * - If the scope exists, returns the existing instance.
                 * - If not, creates a new instance and stores it in the scope.
                 */
                fun <T : Any> resolveScoped(clazz: KClass<T>, scopeId: String): T {
                    val scope = scoped[clazz] ?: throw IllegalArgumentException("No scope found for dependency")
                    return scope.getOrPut(scopeId) { (factories[clazz]?.invoke() ?: throw IllegalArgumentException("No factory found for scoped class")) } as T
                }
            }
        """.trimIndent()

        outputFile.writeText(formattedCode)

        println("✅ Generated dependency graph at: ${outputFile.absolutePath}")
    }
}


afterEvaluate {
    mavenPublishing {
        // Define coordinates for the published artifact
        coordinates(
            groupId = "io.github.thearchitect123",
            artifactId = "atlasCore",
            version = "0.0.1"
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

signing {
    val privateKeyFile = project.properties["signing.privateKeyFile"] as? String
        ?: error("No Private key file found")
    val passphrase = project.properties["signing.password"] as? String
        ?: error("No Passphrase found for signing")

    // Read the private key from the file
    val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)

    useInMemoryPgpKeys(privateKey, passphrase)
    sign(publishing.publications)
}
