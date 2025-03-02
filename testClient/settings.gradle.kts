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

//include(":atlas-graph-generator")
//project(":atlas-graph-generator").projectDir = File("../atlas-graph-generator")