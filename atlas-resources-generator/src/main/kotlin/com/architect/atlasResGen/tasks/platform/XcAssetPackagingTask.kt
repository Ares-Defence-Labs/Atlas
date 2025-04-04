package com.architect.atlasResGen.tasks.platform

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class XcAssetPackagingTask : DefaultTask(){
    @get:OutputDirectory
    abstract val iosAssetsDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    init {
        group = "AtlasXCPackaging"
        description =
            "Generates XCAsset File, to be used with Apple Platforms (iOS/AppleWatch)"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun packageXCAssetFile() {
        val imageDir = File(projectRootDir.get().asFile, "src/commonMain/resources/images")
        if (!imageDir.exists()) {
            logger.warn("⚠️ No images folder found at: ${imageDir.absolutePath}")
            return
        }

        val imageFiles = imageDir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp") }
            .toList()

        val xcAssetsDir = iosAssetsDir.get().asFile
        xcAssetsDir.mkdirs()

        imageFiles.forEach { imageFile ->
            val imageName = imageFile.nameWithoutExtension
            val imageSetName = "${imageName}.imageset"
            val imageSetDir = File(xcAssetsDir, imageSetName)
            imageSetDir.mkdirs()

            // Copy the image
            val targetFile = File(imageSetDir, imageFile.name)
            imageFile.copyTo(targetFile, overwrite = true)

            // Write Contents.json
            File(imageSetDir, "Contents.json").writeText(
                """
            {
              "images" : [
                {
                  "idiom" : "universal",
                  "filename" : "${imageFile.name}",
                  "scale" : "1x"
                }
              ],
              "info" : {
                "version" : 1,
                "author" : "xcode"
              }
            }
            """.trimIndent()
            )
        }

        logger.lifecycle("✅ XCAssets generated at: ${xcAssetsDir.absolutePath}")
    }
}


