plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}