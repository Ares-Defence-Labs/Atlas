package com.architect.atlasResGen.tasks.platform

import com.architect.atlasResGen.helpers.FileHelpers
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
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

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var xcAssetDirectoryPath: String

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
            .filter { it.isFile && it.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp", "svg") }
            .toList()

        val snakeToPath = imageFiles.associate { file ->
            val snakeName = FileHelpers.toSnakeCase(file.nameWithoutExtension)
            snakeName to "images/${file.name}" // safe because file is explicitly named
        }

        generateIosActualObject(snakeToPath) // package the image generator

        imageFiles.forEach { imageFile ->
            val imageName = imageFile.nameWithoutExtension
            val imageSetName = "${imageName}.imageset"
            val imageSetDir = File(xcAssetDirectoryPath, imageSetName)
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
                "author" : "Atlas"
              }
            }
            """.trimIndent()
            )
        }

        logger.lifecycle("✅ XCAssets generated at: $xcAssetDirectoryPath")
    }

    private fun generateIosActualObject(entries: Map<String, String>) {
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.images")
        builder.appendLine()
        builder.appendLine("import platform.UIKit.UIImage")
        builder.appendLine("import platform.Foundation.NSBundle")
        builder.appendLine("import platform.Foundation.stringByAppendingPathComponent")
        builder.appendLine()
        builder.appendLine("class AtlasImages {")
        builder.appendLine("\t\tcompanion object {")

        for ((name, path) in entries) {
            builder.appendLine("    val $name: UIImage?")
            builder.appendLine("        get() = UIImage.imageNamed(\"$name\")")
        }

        builder.appendLine("}")
        builder.appendLine("}")

        val file = File(outputIosDir.get().asFile, "AtlasImages.kt")
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }
}


