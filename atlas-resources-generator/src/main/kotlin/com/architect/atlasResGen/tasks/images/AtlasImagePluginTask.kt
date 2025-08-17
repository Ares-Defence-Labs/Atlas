package com.architect.atlasResGen.tasks.images

import com.architect.atlas.common.helpers.FileHelpers
import net.coobird.thumbnailator.Thumbnails
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CacheableTask
abstract class AtlasImagePluginTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val wearOutputDir: DirectoryProperty

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

    @get:Optional
    @get:OutputDirectory
    abstract val wearAssetImageDir: DirectoryProperty

    @get:Optional
    @get:OutputDirectory
    abstract val wearResourcesDrawableDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var forceRegenerate: Boolean

    @get:Optional
    @get:Input
    abstract var wearOSResourcePackageRef: String

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasImages"
        description =
            "Generates platform-specific image class files based on images in commonMain/resources/images"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
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
                it.isFile && it.extension.lowercase() in listOf(
                    "svg",
                    "png",
                    "jpg",
                    "jpeg",
                    "webp"
                )
            }
            .toList()

        val snakeToPath = imageFiles.associate { file ->
            val snakeName = FileHelpers.toSnakeCase(file.nameWithoutExtension)
            snakeName to "images/${file.name}" // safe because file is explicitly named
        }

        val nonSvgFiles = imageFiles.filter { it.extension.lowercase() != "svg" }
        val svgFiles = imageFiles.filter { it.extension.lowercase() == "svg" }

        // each of the images, need to be checked if they currently exist
        generateAndroidActualObject(snakeToPath, false)
        prepareSvgFilesForAssetManager(svgFiles)
        generateScaledDrawablesWithThumbnailator(nonSvgFiles)

        val wearOutput = wearOutputDir.orNull?.asFile
        if (wearOutput != null) {
            generateAndroidActualObject(snakeToPath, true)
        }
    }

    private fun prepareSvgFilesForAssetManager(svgImageFiles: List<File>) {
        if (svgImageFiles.isEmpty()) {
            logger.warn("\u26a0\ufe0f No image files found in \${imageDir.absolutePath}")
            return
        }

        copyImagesToAndroidAssets(svgImageFiles)
    }

    private fun generateAndroidActualObject(entries: Map<String, String>, isWearable: Boolean) {
        val wearOutput = wearOutputDir.orNull?.asFile
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.images")
        builder.appendLine()
        builder.appendLine("import android.content.Context")
        builder.appendLine("import android.graphics.Bitmap")
        builder.appendLine("import android.graphics.Canvas")
        builder.appendLine("import android.graphics.drawable.BitmapDrawable")
        builder.appendLine("import android.graphics.drawable.Drawable")
        builder.appendLine("import com.caverock.androidsvg.SVG")
        builder.appendLine("import androidx.appcompat.content.res.AppCompatResources")

        if (isWearable) {
            builder.appendLine("import $wearOSResourcePackageRef.R")
        } else {
            builder.appendLine("import $androidResourcePackageRef.R")
        }

        builder.appendLine()
        builder.appendLine("object AtlasImages {")

        for ((name, path) in entries) {
            if (path.endsWith("svg")) {
                builder.appendLine("    fun $name(context: Context): Drawable {")
                builder.appendLine("        val input = context.assets.open(\"$path\")")
                builder.appendLine("        val svg = SVG.getFromInputStream(input)")
                builder.appendLine("        val widthPx = 512")
                builder.appendLine("        val heightPx = 512")
                builder.appendLine("        svg.setDocumentWidth(widthPx.toFloat())")
                builder.appendLine("        svg.setDocumentHeight(heightPx.toFloat())")
                builder.appendLine("        val picture = svg.renderToPicture()")
                builder.appendLine("        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)")
                builder.appendLine("        val canvas = Canvas(bitmap)")
                builder.appendLine("        canvas.drawPicture(picture)")
                builder.appendLine("        return BitmapDrawable(context.resources, bitmap)")
                builder.appendLine("    }")
            } else {
                builder.appendLine("    fun $name(context: Context): Drawable =")
                builder.appendLine("        AppCompatResources.getDrawable(context, R.drawable.$name)")
                builder.appendLine("            ?: error(\"Image not found: R.drawable.$name\")")
            }
        }

        builder.appendLine("}")

        if (!isWearable) {
            val file = File(androidOutputDir.get().asFile, "AtlasImages.kt")
            file.parentFile.mkdirs()
            file.writeText(builder.toString())
        } else {
            val wearFile = File(wearOutput, "AtlasImages.kt")
            wearFile.parentFile.mkdirs()
            wearFile.writeText(builder.toString())
        }
    }

    private fun copyImagesToAndroidAssets(imageFiles: List<File>) {
        val targetDir = androidAssetImageDir.asFile.get()
        targetDir.mkdirs()

        val filteredImages =
            imageFiles.filter {
                !File(targetDir, it.name).exists() || forceRegenerate
            }

        if (filteredImages.isEmpty()) {
            logger.warn("All images already exist")
            return
        }

        filteredImages.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)

            logger.lifecycle("Moving SVG to AssetManager ${sourceFile.name}")
            sourceFile.copyTo(targetFile, overwrite = true)
        }

        logger.lifecycle("✅ Copied ${imageFiles.size} images to Android assets: ${targetDir.absolutePath}")

        // wear os components
        val wearTargetDir = wearAssetImageDir.orNull?.asFile
        if (wearTargetDir != null) {
            wearTargetDir.mkdirs()
            val wearFilteredImages =
                imageFiles.filter {
                    !File(wearTargetDir, it.name).exists() || forceRegenerate
                }

            if (wearFilteredImages.isEmpty()) {
                logger.warn("All images already exist")
                return
            }

            wearFilteredImages.forEach { sourceFile ->
                val targetFile = File(wearTargetDir, sourceFile.name)

                logger.lifecycle("Moving SVG to AssetManager ${sourceFile.name}")
                sourceFile.copyTo(targetFile, overwrite = true)
            }

            logger.lifecycle("✅ Copied ${imageFiles.size} images to Android assets: ${wearTargetDir.absolutePath}")
        }
    }

    private fun generateScaledDrawablesWithThumbnailator(nonSvgFiles: List<File>) {
        if (nonSvgFiles.isEmpty()) {
            logger.warn("No Images can be found to copy to drawables")
            return
        }

        val baseOutputDir = androidResourcesDrawableDir.asFile.get()
        val wearbaseOutputDir = wearResourcesDrawableDir.orNull?.asFile
        val densities = mapOf(
            "mdpi" to 1.0,
            "hdpi" to 1.5,
            "xhdpi" to 2.0,
            "xxhdpi" to 3.0,
            "xxxhdpi" to 4.0
        )

        nonSvgFiles.forEach { imageFile ->
            densities.forEach { (density, scale) ->
                try {
                    val targetDir = File(baseOutputDir, "drawable-$density")
                    val wearTargetDir = if (wearbaseOutputDir != null) File(
                        wearbaseOutputDir,
                        "drawable-$density"
                    ) else null

                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }

                    if (wearbaseOutputDir != null) {
                        if (wearTargetDir?.exists() == false) {
                            wearTargetDir.mkdirs()
                        }
                    }

                    val outputFile =
                        File(targetDir, "${imageFile.nameWithoutExtension}.${imageFile.extension}")
                    if (outputFile.exists() && !forceRegenerate) {
                        logger.lifecycle("PNG Image already exists ${outputFile.name}")
                        return
                    }

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

                    // wearOS file
                    if (wearbaseOutputDir != null) {
                        val wearOutputFile =
                            File(
                                wearTargetDir,
                                "${imageFile.nameWithoutExtension}.${imageFile.extension}"
                            )
                        if (wearOutputFile.exists() && !forceRegenerate) {
                            logger.lifecycle("PNG Image already exists ${outputFile.name}")
                            return
                        }

                        Thumbnails.of(imageFile)
                            .scale(scale)
                            .outputFormat(imageFile.extension)
                            .allowOverwrite(true)
                            .toFile(wearOutputFile)

                        logger.lifecycle(
                            "✅ [${density.padEnd(6)}] ${wearOutputFile.name} → ${
                                wearTargetDir?.relativeTo(
                                    wearbaseOutputDir
                                )
                            }"
                        )
                    }
                } catch (e: Exception) {
                    logger.error("❌ Failed to generate scaled image for: ${imageFile.name}", e)
                }
            }
        }
    }
}