import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`

    id("org.gradle.maven-publish")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("signing")
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
        create("dependencyGraphPlugin") {
            id = "io.github.thearchitect123.atlasGraphGenerator"
            implementationClass = "com.architect.atlasGraphGenerator.AtlasDIProcessor"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", "2.0.0")) // Force Kotlin 2.0.0
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0") // Align with Gradle Kotlin DSL
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.thearchitect123",
        artifactId = "atlas-graph-generator",
        version = "0.1.1"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("AtlasGraph")
        description.set("Atlas Plugin used to generate the compile time dependency graph for your project")
        inceptionYear.set("2025")
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