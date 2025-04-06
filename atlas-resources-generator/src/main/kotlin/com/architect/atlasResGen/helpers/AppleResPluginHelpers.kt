package com.architect.atlasResGen.helpers

import com.architect.atlas.common.helpers.FileHelpers
import com.architect.atlas.common.helpers.ProjectFinder
import com.architect.atlasResGen.tasks.fonts.AppleAtlasFontPluginTask
import com.architect.atlasResGen.tasks.platform.XcAssetPackagingTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

object AppleResPluginHelpers {
    private val appleFontsGenTask = "appleFontsGenTask"
    private val applePackageXcodeGenTask = "applePackageXcodeGenTask"

    fun getAtlasXCAssetFilePackagingTask(project: Project): TaskProvider<XcAssetPackagingTask> {
        val iosProject = ProjectFinder.findIosClientApp(project)
        val appName = File(iosProject?.name!!).name
        return project.tasks.register(
            applePackageXcodeGenTask,
            XcAssetPackagingTask::class.java
        ) {
            xcAssetDirectoryPath = "$iosProject/$appName/Assets.xcassets"
            outputIosDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/images"))
            iosAssetsDir.set(project.layout.buildDirectory.dir("generated/iosMain/resources/AtlasAssets.xcassets"))
            projectRootDir.set(project.layout.projectDirectory)
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
}