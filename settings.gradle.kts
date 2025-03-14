enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Atlas"
include(":shared")
include(":atlas-graph-generator")
include(":atlas-droid-navigation-fragment-ktx")
include(":atlas-droid-navigation-compose")
include(":atlas-ios-navigation-coordinator")
include(":atlas-ios-navigation-swift-ui")
include(":atlas-droid-mvvm-viewbinding")
include(":atlas-droid-mvvm-compose")
include(":atlas_compiler")
include(":atlas-droid-live-data")
include(":atlas-resources-generator")
include(":atlas-flows")
include(":atlas-hot-reload-plugin")