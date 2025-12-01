plugins {
    `kotlin-dsl`
    `java-gradle-plugin`

    id("org.gradle.maven-publish")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.35.0"
    id("signing")
}

////
//repositories {
//    google()
//    mavenCentral()
//    gradlePluginPortal() // Local Testing Only
//}

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

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("com.android.tools.build:gradle:8.2.0")
}

//////
afterEvaluate {
    mavenPublishing {
        coordinates(
            groupId = "io.github.thearchitect123",
            artifactId = "atlas-plugin-common",
            version = "0.1.9"
        )

        pom {
            name.set("AtlasPluginCommon")
            description.set("Common utility functions to be shared between plugins")
            inceptionYear.set("2025")
            url.set("https://github.com/TheArchitect123/Atlas")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("DanGerchcovich")
                    name.set("TheArchitect123")
                    email.set("dan.developer789@gmail.com")
                }
            }

            scm {
                url.set("https://github.com/TheArchitect123/Atlas")
            }
        }

        // Central Portal is now the default
        publishToMavenCentral()

        // Use Gradle signing for all publications
        signAllPublications()
    }

//
    signing {
        val privateKeyFile = project.findProperty("signing.privateKeyFile") as? String
            ?: error("No Private key file found")
        val passphrase = project.findProperty("signing.password") as? String
            ?: error("No Passphrase found for signing")

        // Read the private key from the file
        val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)

        useInMemoryPgpKeys(privateKey, passphrase)
        sign(publishing.publications)
    }
}