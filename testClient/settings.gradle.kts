import org.gradle.api.internal.provider.ValueSupplier.ValueProducer.task

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("../atlas-graph-generator")
    includeBuild("../atlas-resources-generator")
    includeBuild("../atlas-navigation-engine-generator")
    includeBuild("../atlas-incremental-build-engine")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed%40Local/maven/v1")
        }
    }
}

rootProject.name = "AtlasTestClient"
include(":androidApp")
include(":shared")
//
//include(":atlas-droid-mvvm-viewbinding")
//project(":atlas-droid-mvvm-viewbinding").projectDir = File("../atlas-droid-mvvm-viewbinding")


include(":atlas-core-shared")
project(":atlas-core-shared").projectDir = File("../Interfaces/shared")
