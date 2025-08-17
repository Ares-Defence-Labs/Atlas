plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
}
//
//val runningWearOS = tasks.register("runningWearOS") {
//    group = "Wear"; description = "Runs Wear-only steps"
//    enabled = false // default off
//    doLast { println("✅ runningWearOS active for ${project.path}") }
//}
//
//gradle.taskGraph.whenReady { graph ->
//    runningWearOS.configure {
//        enabled = graph.allTasks.any { it.project == project } &&
//                graph.allTasks.none { it.path.startsWith(":app:") || it.path.startsWith(":androidApp:") } // adjust
//    }
//}



project.afterEvaluate {
    val genGraph = project(":shared").tasks.named("generateDependencyGraph")
// Make the Wear tasks depend on it (covers debug + others if present)
    tasks.matching {
        name in setOf(
            ":wearosapp:generateDebugResources",
            ":wearosapp:extractDeepLinksDebug",
            ":wearosapp:mergeDebugShaders",
            ":wearosapp:mergeDebugJniLibFolders",
            ":wearosapp:mergeExtDexDebug"
        )
    }.configureEach {
        logger.lifecycle("TRIGGERING NAMED ${name}")
        dependsOn(genGraph)
    }

// Optional: also tie variant pre-tasks
    tasks.matching { name.startsWith("preDebug") }.configureEach {
        dependsOn(genGraph)
    }
}

android {
    sourceSets.named("main") {
        // this is the *source root* where your .kt files live
        java.srcDir(layout.buildDirectory.dir("generated/kotlin"))
    }

    namespace = "com.architect.wearosapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.architect.wearosapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(projects.shared)
    implementation(libs.atlas.core.binding)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation("androidx.navigation:navigation-compose:2.8.9")

    implementation("com.caverock:androidsvg:1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1")
    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.app.compat)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}