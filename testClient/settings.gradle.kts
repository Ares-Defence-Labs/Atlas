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

rootProject.name = "AtlasTestClient"
include(":androidApp")
include(":shared")

//include(":atlas-core")
//project(":atlas-core").projectDir = File("../shared")