import org.gradle.api.internal.provider.ValueSupplier.ValueProducer.task

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
         includeBuild("../atlas-graph-generator")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AtlasTestClient"
include(":androidApp")
include(":shared")
//
//include(":atlas-droid-mvvm-viewbinding")
//project(":atlas-droid-mvvm-viewbinding").projectDir = File("../atlas-droid-mvvm-viewbinding")


include(":atlas-core-shared")
project(":atlas-core-shared").projectDir = File("../shared")