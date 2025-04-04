package com.architect.atlasResGen.helpers

import com.architect.atlasResGen.tasks.fonts.AppleAtlasFontPluginTask
import com.architect.atlasResGen.tasks.platform.XcAssetIDEMigrationTask
import com.architect.atlasResGen.tasks.platform.XcAssetPackagingTask
import com.architect.atlasResGen.tasks.platform.XcFontAssetsPackagingTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

object AppleResPluginHelpers {

    private val appleFontsGenTask = "appleFontsGenTask"
    private val appleFontsPackagingGenTask = "appleFontsPackagingGenTask"
    private val applePackageXcodeGenTask = "applePackageXcodeGenTask"
    private val appleMigratePackagedXcodeGenTask = "appleMigratePackagedXcodeGenTask"

    fun getAtlasXCAssetFilePackagingTask(project: Project): TaskProvider<XcAssetPackagingTask> {
        return project.tasks.register(
            applePackageXcodeGenTask,
            XcAssetPackagingTask::class.java
        ) {
            iosAssetsDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/AtlasAssets.xcassets"))
            projectRootDir.set(project.layout.projectDirectory)
        }
    }

    fun getAtlasXCAssetPostPackageMigrationTask(project: Project): TaskProvider<XcAssetIDEMigrationTask> {
        return project.tasks.register(
            appleMigratePackagedXcodeGenTask,
            XcAssetIDEMigrationTask::class.java
        ) {
            xcassetsSourceDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/AtlasAssets.xcassets"))
            outputScriptFile.set(project.layout.buildDirectory.file("scripts/copyAtlasAssets.sh"))
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
        }
    }

    fun getAppleFontsPackagingResourceTask(project: Project): TaskProvider<XcFontAssetsPackagingTask> {
        val iosProject = ProjectFinder.findIosClientApp(project)
        return project.tasks.register(
            appleFontsPackagingGenTask,
            XcFontAssetsPackagingTask::class.java
        ) {
            iOSProjectDirectory = iosProject?.absolutePath ?: ""
            fontDirectory =
                "${project.layout.buildDirectory.asFile.get().path}/generated/iosMain/resources/fonts/fontFiles"
            injectPlistScriptFile.set(project.layout.buildDirectory.file("generated/iosMain/resources/fonts/scripts/updateInfoPlistFonts.sh"))
            copyScriptFile.set(project.layout.buildDirectory.file("generated/iosMain/resources/fonts/scripts/copyAtlasAssets.sh"))
        }
    }
}