import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`

    id("org.gradle.maven-publish")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
    id("signing")
}

//////////////////////
repositories {
    google()
    mavenCentral()
    gradlePluginPortal() // Local Testing Only
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))  // ✅ Ensure Java 17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

gradlePlugin {
    plugins {
        create("resourcesGeneratorGraphPlugin") {
            id = "io.github.thearchitect123.atlasResourcesGenerator"
            implementationClass = "com.architect.atlasResGen.plugins.AtlasResourceGenPlugin"
        }
    }
}

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("com.android.tools.build:gradle:8.2.0")
    implementation("org.apache.pdfbox:fontbox:2.0.30")
    implementation("io.github.thearchitect123:atlas-plugin-common:0.1.6")

    implementation("org.apache.xmlgraphics:batik-transcoder:1.16")
    implementation("org.apache.xmlgraphics:batik-codec:1.16")
    implementation("org.apache.xmlgraphics:batik-dom:1.16")
    implementation("org.apache.xmlgraphics:batik-svggen:1.16")

    implementation("com.twelvemonkeys.imageio:imageio-core:3.12.0")
}
//////
//mavenPublishing {
//    // Define coordinates for the published artifact
//    coordinates(
//        groupId = "io.github.thearchitect123",
//        artifactId = "atlas-res-generator",
//        version = "0.5.8"
//    )
//
//    // Configure POM metadata for the published artifact
//    pom {
//        name.set("AtlasResGen")
//        description.set("A resource generator plugin for Atlas SDK. Used for generating strings, images, colors, based on definition files inside your project")
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
