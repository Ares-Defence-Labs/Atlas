package com.architect.atlasResGen.tasks.images

import com.architect.atlasResGen.helpers.ResPluginHelpers
import net.coobird.thumbnailator.Thumbnails
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class AtlasImagePluginTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectBuildDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidAssetImageDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidResourcesDrawableDir: DirectoryProperty

    @get:OutputDirectory
    abstract var androidResourcePackageRef: String

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    @get:Input
    abstract var forceRegenerate: Boolean

    init {
        group = "AtlasImages"
        description =
            "Generates platform-specific image class files based on images in commonMain/resources/images"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateImagesClass() {
        val imageDir = File(projectRootDir.get().asFile, "src/commonMain/resources/images")
        if (!imageDir.exists()) {
            logger.warn("\u2757\ufe0f No images folder found at: \${imageDir.absolutePath}")
            return
        }

        val imageFiles = imageDir.walk()
            .filter {
                it.isFile && it.extension.lowercase() in listOf("svg", "png", "jpg", "jpeg", "webp")
            }
            .toList()

        val snakeToPath = imageFiles.associate { file ->
            val snakeName = ResPluginHelpers.toSnakeCase(file.nameWithoutExtension)
            snakeName to "images/${file.name}" // safe because file is explicitly named
        }

        val nonSvgFiles = imageFiles.filter { it.extension.lowercase() != "svg" }
        val svgFiles = imageFiles.filter { it.extension.lowercase() == "svg" }

        if (isAndroidTarget) {
            generateAndroidActualObject(snakeToPath)
            prepareSvgFilesForAssetManager(svgFiles)
            generateScaledDrawablesWithThumbnailator(nonSvgFiles)
        }
    }

    private fun prepareSvgFilesForAssetManager(svgImageFiles: List<File>) {
        if (svgImageFiles.isEmpty()) {
            logger.warn("\u26a0\ufe0f No image files found in \${imageDir.absolutePath}")
            return
        }

        copyImagesToAndroidAssets(svgImageFiles)
    }

    private fun generateAndroidActualObject(entries: Map<String, String>) {
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.images")
        builder.appendLine()
        builder.appendLine("import android.content.Context")
        builder.appendLine("import android.graphics.drawable.Drawable")
        builder.appendLine("import androidx.appcompat.content.res.AppCompatResources")
        builder.appendLine("import $androidResourcePackageRef.R")
        builder.appendLine()
        builder.appendLine("object AtlasImages {")

        for ((name, path) in entries) {
            if (path.endsWith("svg")) {
                builder.appendLine("    fun $name(context: Context): Drawable =")
                builder.appendLine("        context.assets.open(\"$path\").use { input ->")
                builder.appendLine("            Drawable.createFromStream(input, null) ?: error(\"Image not found: $path\")")
                builder.appendLine("        }")
            } else {
                builder.appendLine("    fun $name(context: Context): Drawable =")
                builder.appendLine("        AppCompatResources.getDrawable(context, R.drawable.$name)")
                builder.appendLine("            ?: error(\"Image not found: R.drawable.image_name\")")
            }
        }

        builder.appendLine("}")

        val file = File(androidOutputDir.get().asFile, "AtlasImages.kt")
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }

    private fun generateIosActualObject(entries: Map<String, String>) {
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.images")
        builder.appendLine()
        builder.appendLine("import platform.UIKit.UIImage")
        builder.appendLine("import platform.Foundation.NSBundle")
        builder.appendLine("import platform.Foundation.stringByAppendingPathComponent")
        builder.appendLine()
        builder.appendLine("object AtlasImages {")

        for ((name, path) in entries) {
            builder.appendLine("    val $name: UIImage?")
            builder.appendLine("        get() = NSBundle.mainBundle.resourcePath")
            builder.appendLine("            ?.stringByAppendingPathComponent(\"$path\")")
            builder.appendLine("            ?.let { UIImage.imageWithContentsOfFile(it) }")
        }

        builder.appendLine("}")

        val file = File(outputIosDir.get().asFile, "AtlasImages.kt")
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }

    private fun copyImagesToAndroidAssets(imageFiles: List<File>) {
        val targetDir = androidAssetImageDir.asFile.get()
        logger.lifecycle("TARGET PATH $targetDir")
        targetDir.mkdirs()

        val filteredImages =
            imageFiles.filter { !it.exists() || forceRegenerate }
        filteredImages.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            sourceFile.copyTo(targetFile, overwrite = true)
        }

        logger.lifecycle("✅ Copied ${imageFiles.size} images to Android assets: ${targetDir.absolutePath}")
    }

    private fun generateScaledDrawablesWithThumbnailator(nonSvgFiles: List<File>) {
        val baseOutputDir = androidResourcesDrawableDir.asFile.get()
        val densities = mapOf(
            "mdpi" to 1.0,
            "hdpi" to 1.5,
            "xhdpi" to 2.0,
            "xxhdpi" to 3.0,
            "xxxhdpi" to 4.0
        )

        val filteredImages =
            nonSvgFiles.filter { !it.exists() || forceRegenerate }
        filteredImages.forEach { imageFile ->
            densities.forEach { (density, scale) ->
                try {
                    val targetDir = File(baseOutputDir, "drawable-$density")
                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }

                    val outputFile =
                        File(targetDir, "${imageFile.nameWithoutExtension}.${imageFile.extension}")
                    Thumbnails.of(imageFile)
                        .scale(scale)
                        .outputFormat(imageFile.extension)
                        .allowOverwrite(true)
                        .toFile(outputFile)

                    logger.lifecycle(
                        "✅ [${density.padEnd(6)}] ${outputFile.name} → ${
                            targetDir.relativeTo(
                                baseOutputDir
                            )
                        }"
                    )
                } catch (e: Exception) {
                    logger.error("❌ Failed to generate scaled image for: ${imageFile.name}", e)
                }
            }
        }
    }
}