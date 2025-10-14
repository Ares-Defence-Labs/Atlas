package com.architect.atlasGraphGenerator

import com.architect.atlas.common.helpers.AppleProjectFinder.isIPhoneBuildNow
import com.architect.atlas.common.helpers.AppleProjectFinder.isWatchBuildNow
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlas.common.helpers.TaskMngrHelpers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import java.io.File

class AtlasDIProcessor : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val masterKeyHandler = project.tasks.findByName("masterKeyHandler")

            val droidModule = ProjectFinder.findAndroidClientApp(project)
            val wearOSModule = ProjectFinder.findWearApp(project)

            val dataSets = project.providers
                .gradleProperty("atlas.extraViewModelBaseClasses")
                .forUseAtConfigurationTime()
                .orNull
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()

            val generateDependencyGraphTask = project.tasks.register(
                "generateDependencyGraph",
                AtlasDIProcessorGraphTask::class.java
            ) {
                extraViewModelBaseClasses.set(dataSets)
                dependencyCommonMainSources.setFrom(collectCommonMainSourcesFromDependenciesOnly(project))
                externalSourceDirs.from(
                    droidModule?.layout?.projectDirectory?.dir("src/main/kotlin")
                )
                iosSourceDirs.from(
                    project.layout.projectDirectory.dir("src/iosMain/kotlin")
                )

                appleWatchSourceDirs.from(
                    project.layout.projectDirectory.dir("src/watchosMain/kotlin")
                )

                projectRootDir.from(
                    project.layout.projectDirectory.dir("src/commonMain/kotlin")
                )

                androidMainRootDir.from(
                    project.layout.projectDirectory.dir("src/androidMain/kotlin")
                )
                androidOutputDir.set(droidModule?.layout?.buildDirectory?.dir("generated/kotlin/container")!!)

                iOSOutputDir.set(project.layout.buildDirectory.dir("generated/iosMain/kotlin/container"))
                appleWatchOutputDir.set(project.layout.buildDirectory.dir("generated/watchosMain/kotlin/container"))

                isAndroidTarget = !(project.isIPhoneBuildNow() || project.isWatchBuildNow())
                androidBasePackageRef = ProjectFinder.getAndroidAppNamespace(droidModule)
            }

            if(wearOSModule != null){
                generateDependencyGraphTask.apply {
                    configure {
                        wearOSModuleDirectory.set(wearOSModule.projectDir)
                        wearOSOutputDir.set(wearOSModule.layout.buildDirectory.dir("generated/kotlin/container"))
                        wearOSSourceDirs.from(
                            wearOSModule.layout.projectDirectory.dir("src/main/kotlin")
                        )
                        wearOSBasePackageRef = project.findProperty("atlas.wearableBasePackage")?.toString() ?: ""
                    }
                }
            }

            val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
                include("graphInputHash.txt")
            }
            if (!hashFileTree.isEmpty) {
                generateDependencyGraphTask.configure {
                    inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
                }
            }

            if (masterKeyHandler == null) {
                TaskMngrHelpers.taskOrderConfig(project, generateDependencyGraphTask.get())
            }
        }
    }

    fun extractCommonMainSourcesFromSourcesJars(project: Project): Set<File> {
        val outputDirs = mutableSetOf<File>()
        val sourcesConfig = project.configurations.detachedConfiguration().apply {
            isTransitive = false
        }

        val allDependencies = project.configurations
            .flatMap { it.dependencies }
            .filterIsInstance<ExternalModuleDependency>()

        allDependencies.forEach { dep ->
            // Attempt to resolve the :sources variant
            val sourceNotation = "${dep.group}:${dep.name}:${dep.version}:sources@jar"
            val sourceDep = project.dependencies.create(sourceNotation)
            sourcesConfig.dependencies.add(sourceDep)
        }

        try {
            sourcesConfig.resolve().forEach { jar ->
                val extractDir = File(project.buildDir, "extracted-sources/${jar.nameWithoutExtension}")
                if (!extractDir.exists()) {
                    project.copy {
                        from(project.zipTree(jar))
                        into(extractDir)
                    }
                }
                val commonMain = File(extractDir, "src/commonMain/kotlin")
                if (commonMain.exists()) outputDirs += commonMain
            }
        } catch (e: Exception) {
            project.logger.warn("⚠️ Failed to resolve or extract some source jars: ${e.message}")
        }

        return outputDirs
    }

    fun findCommonMainSourcesInProjectDependencies(project: Project): Set<File> {
        val visited = mutableSetOf<Project>()
        val result = mutableSetOf<File>()

        fun recurse(proj: Project) {
            if (!visited.add(proj)) return
            proj.configurations
                .flatMap { it.dependencies }
                .filterIsInstance<ProjectDependency>()
                .map { it.dependencyProject }
                .forEach { dep ->
                    val dir = File(dep.projectDir, "src/commonMain/kotlin")
                    if (dir.exists()) result += dir
                    recurse(dep)
                }
        }

        recurse(project)
        return result
    }

    fun collectCommonMainSourcesFromDependenciesOnly(project: Project): Set<File> {
        val projectDeps = findCommonMainSourcesInProjectDependencies(project)
        val extractedSources = extractCommonMainSourcesFromSourcesJars(project)
        return projectDeps + extractedSources
    }
}