plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    id("org.gradle.maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("kotlin-parcelize")
}

android {
    namespace = "com.architect.atlasLiveData"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.live.data)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.app.compat)
    implementation(libs.material)
}
//
//mavenPublishing {
//    // Define coordinates for the published artifact
//    coordinates(
//        groupId = "io.github.thearchitect123",
//        artifactId = "atlas-live-data",
//        version = "0.0.1"
//    )
//
//    // Configure POM metadata for the published artifact
//    pom {
//        name.set("AtlasLiveData")
//        description.set("Atlas Live Data implementation")
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