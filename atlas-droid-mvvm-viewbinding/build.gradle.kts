import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    id("org.gradle.maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("kotlin-parcelize")
}
//
//repositories {
//    google()
//    mavenCentral()
//    gradlePluginPortal() // Local Testing Only
//}

android {
    sourceSets {
        getByName("main") {
            kotlin.srcDirs("src/main/kotlin")
            java.srcDirs("src/main/java", "src/main/kotlin")
        }
    }

    namespace = "com.architect.atlas.viewBinding"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures{
        viewBinding = true
    }
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core)
    implementation(libs.androidx.app.compat)
    implementation(libs.atlas.core)
    implementation(libs.androidx.app.lifecycle.viewmodel)
    implementation(libs.androidx.app.lifecycle.viewmodel.runtime)
}
////
//mavenPublishing {
//    // Define coordinates for the published artifact
//    coordinates(
//        groupId = "io.github.thearchitect123",
//        artifactId = "atlas-mvvm-view-binding",
//        version = "0.3.1"
//    )
//
//    // Configure POM metadata for the published artifact
//    pom {
//        name.set("AtlasMvvmViewBinding")
//        description.set("Atlas View Binding implementation library for atlas-core")
//        inceptionYear.set("2025")
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
////
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    outputs.upToDateWhen { false } // ✅ Always recompile Kotlin sources
}