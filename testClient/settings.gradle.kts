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
    includeBuild("../atlas-flows-generator")
    includeBuild("../atlas-incremental-build-engine")
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
project(":atlas-core-shared").projectDir = File("../Interfaces/shared")
