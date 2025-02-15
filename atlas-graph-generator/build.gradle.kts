plugins {


}

dependencies {
    //implementation(kotlin("stdlib"))

    // ✅ Include KSP API
//    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
//    ksp(files("${buildDir}/libs/atlas-graph-generator-1.0.0.jar"))
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

tasks.jar {
    archiveBaseName.set("atlas-graph-generator")
    archiveVersion.set("1.0.0")

    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Implementation-Title" to "Atlas Graph Generator",
            "Implementation-Version" to archiveVersion.get(),
        )
    }
}



// ✅ Ensure KSP waits for the JAR to be built
tasks.named("build").configure {
    dependsOn("jar", "runSymbolProcessor")  // ✅ Forces JAR compilation before running KSP
}

tasks.register<JavaExec>("runSymbolProcessor") {
    dependsOn("classes") // ✅ Ensure the project is compiled first
    mainClass.set("com.architect.atlas_graph_generator.MainProcessorRunner") // ✅ Entry point
    classpath = sourceSets["main"].runtimeClasspath
}

//
//tasks.register("cleanGeneratedSources") {
//    doLast {
//        val generatedDir = file("build/generated/ksp/main/kotlin/com/architect/atlas/container/")
//        if (generatedDir.exists()) {
//            generatedDir.deleteRecursively()
//            println("🧹 Cleaned old generated KSP files!")
//        }
//    }
//}
//
//// ✅ Ensure KSP runs after cleaning old files
//tasks.named("kspKotlin").configure {
//    dependsOn("cleanGeneratedSources")
//}