package com.architect.atlasResGen.helpers

import com.architect.atlas.common.helpers.FileHelpers
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlasResGen.tasks.fonts.AppleAtlasFontPluginTask
import com.architect.atlasResGen.tasks.platform.XcAssetPackagingTask
import com.architect.atlasResGen.tasks.platform.XcFontAssetsPackagingTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

object AppleResPluginHelpers {
    private val appleFontsGenTask = "appleFontsGenTask"
    private val appleFontsPackagingGenTask = "appleFontsPackagingGenTask"
    private val applePackageXcodeGenTask = "applePackageXcodeGenTask"

    fun getAtlasXCAssetFilePackagingTask(project: Project): TaskProvider<XcAssetPackagingTask> {
        val iosProject = ProjectFinder.findIosClientApp(project)
        val forceConvertSVG =
            project.findProperty("atlas.forceSVGs")?.toString()?.toBoolean() ?: false
        val appName = File(iosProject?.name!!).name
        return project.tasks.register(
            applePackageXcodeGenTask,
            XcAssetPackagingTask::class.java
        ) {
            forceRegenerate = FileHelpers.forceRecreateAllFiles(project)
            forceSVGs = forceConvertSVG
            xcAssetDirectoryPath = "$iosProject/$appName/Assets.xcassets"
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/images"))
            iosAssetsDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/AtlasAssets.xcassets"))
            projectRootDir.set(project.layout.projectDirectory)
        }.apply {
            val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
                include("graphInputHash.txt")
            }
            if (!hashFileTree.isEmpty) {
                configure {
                    inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
                }
            }
        }
    }

    fun getAppleFontsResourceTask(project: Project): TaskProvider<AppleAtlasFontPluginTask> {
        return project.tasks.register(
            appleFontsGenTask,
            AppleAtlasFontPluginTask::class.java
        ) {
            mergedInfoPlist =
                project.layout.buildDirectory.dir("generated/iosMain/resources/fonts/mergedFonts.plist")
                    .get().asFile
            forceRegenerate = FileHelpers.forceRecreateAllFiles(project)
            iOSResourcesFontsDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/fonts/fontFiles"))
            projectRootDir.set(project.layout.projectDirectory)
            outputDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/fonts"))
        }.apply {
            val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
                include("graphInputHash.txt")
            }
            if (!hashFileTree.isEmpty) {
                configure {
                    inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
                }
            }
        }
    }

    fun getAppleFontsPackagingResourceTask(project: Project): TaskProvider<XcFontAssetsPackagingTask> {
        val iosProject = ProjectFinder.findIosClientApp(project)
        return project.tasks.register(
            appleFontsPackagingGenTask,
            XcFontAssetsPackagingTask::class.java
        ) {
            forceRegenerate = FileHelpers.forceRecreateAllFiles(project)
            iOSProjectDirectory = iosProject?.absolutePath ?: ""
            fontDirectory =
                "${project.layout.buildDirectory.asFile.get().path}/generated/iosMain/resources/fonts/fontFiles"
            injectPlistScriptFile.set(project.layout.buildDirectory.file("generated/iosMain/resources/fonts/scripts/updateInfoPlistFonts.sh"))
            copyScriptFile.set(project.layout.buildDirectory.file("generated/iosMain/resources/fonts/scripts/copyAtlasAssets.sh"))
        }.apply {
            val hashFileTree = project.fileTree(project.layout.buildDirectory.dir("atlas")) {
                include("graphInputHash.txt")
            }
            if (!hashFileTree.isEmpty) {
                configure {
                    inputHashFile.set(project.layout.buildDirectory.file("atlas/graphInputHash.txt"))
                }
            }
        }
    }
}