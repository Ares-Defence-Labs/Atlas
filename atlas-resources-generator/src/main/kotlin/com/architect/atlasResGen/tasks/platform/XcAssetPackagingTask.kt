package com.architect.atlasResGen.tasks.platform

import com.architect.atlas.common.helpers.FileHelpers
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import javax.xml.parsers.DocumentBuilderFactory

@CacheableTask
abstract class XcAssetPackagingTask : DefaultTask() {
    @get:OutputDirectory
    abstract val iosAssetsDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var xcAssetDirectoryPath: String

    @get:Input
    abstract var forceSVGs: Boolean

    @get:Input
    abstract var forceRegenerate: Boolean

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasXCPackaging"
        description =
            "Generates XCAsset File, to be used with Apple Platforms (iOS/AppleWatch)"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
    }

    @TaskAction
    fun packageXCAssetFile() {
        val imageDir = File(projectRootDir.get().asFile, "src/commonMain/resources/images")
        if (!imageDir.exists()) {
            logger.warn("⚠️ No images folder found at: ${imageDir.absolutePath}")
            return
        }

        val imageFiles = imageDir.walkTopDown()
            .filter {
                it.isFile && it.extension.lowercase() in listOf(
                    "png",
                    "jpg",
                    "jpeg",
                    "webp",
                    "svg"
                )
            }
            .toList()

        val snakeToPath = imageFiles.associate { file ->
            val snakeName = FileHelpers.toSnakeCase(file.nameWithoutExtension)
            snakeName to "images/${file.name}" // safe because file is explicitly named
        }

        generateIosActualObject(snakeToPath) // package the image generator

        val contentAssetFiles = imageFiles.filter {
            val imageSetDir = File(xcAssetDirectoryPath, it.name)
            val contentJson = File(imageSetDir, "Contents.json")

            if(contentJson.exists()) !contentJson.readText().contains(it.name) || forceRegenerate else forceRegenerate
        }
        if (contentAssetFiles.isEmpty()) {
            logger.lifecycle("All image files already exist. Skipping asset catalog assignment")
            return
        }
        contentAssetFiles.forEach { imageFile ->
            logger.lifecycle("IMAGE FILE : $imageFiles")
            val isSvg = imageFile.extension.lowercase() == "svg"
            val imageName = imageFile.nameWithoutExtension
            val imageSetName = "${imageName}.imageset"
            val imageSetDir = File(xcAssetDirectoryPath, imageSetName)
            imageSetDir.mkdirs()

            val outputImages = mutableListOf<Map<String, String>>()

            if (isSvg && !forceSVGs) {
                val safeSvg = sanitizeSvgFile(imageFile)
                val scales = listOf(
                    Triple("@1x", 1f, "$imageName.png"),
                    Triple("@2x", 2f, "$imageName@2x.png"),
                    Triple("@3x", 3f, "$imageName@3x.png")
                )

                for ((suffix, scale, filename) in scales) {
                    val outputFile = File(imageSetDir, filename)
                    convertSvgToPng(
                        svgFile = safeSvg,
                        outputFile = outputFile,
                        scale
                    )

                    logger.lifecycle("RUNNING FILE NAME OUTPUT: $filename")

                    outputImages.add(
                        mapOf(
                            "idiom" to "universal",
                            "filename" to filename,
                            "scale" to "${scale.toInt()}x"
                        )
                    )
                }
            } else {
                val targetFile = File(imageSetDir, imageFile.name)
                imageFile.copyTo(targetFile, overwrite = true)
                outputImages.add(
                    mapOf(
                        "idiom" to "universal",
                        "filename" to imageFile.name,
                        "scale" to "1x"
                    )
                )
            }

            // Write Contents.json
            val contentsJson = """
{
  "images" : [
    ${
                outputImages.joinToString(",\n    ") { img ->
                    """{
      "idiom" : "${img["idiom"]}",
      "filename" : "${img["filename"]}",
      "scale" : "${img["scale"]}"
    }"""
                }
            }
  ],
  "info" : {
    "version" : 1,
    "author" : "Atlas"
  }
}
""".trimIndent()

            File(imageSetDir, "Contents.json").writeText(contentsJson)
        }

        logger.lifecycle("✅ XCAssets generated at: $xcAssetDirectoryPath")

        // generate color assets (writes to the asset catalog)
        val colorsXmlFile =
            File(projectRootDir.get().asFile, "src/commonMain/resources/colors/colors.xml")
        val colors = mutableListOf<Pair<String, String>>()

        if (colorsXmlFile.exists()) {
            logger.lifecycle("FOUND COLORS $colorsXmlFile")
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = docBuilder.parse(colorsXmlFile)
            val colorElements = document.getElementsByTagName("color")

            for (i in 0 until colorElements.length) {
                val node = colorElements.item(i)
                val key = node.attributes?.getNamedItem("key")?.nodeValue ?: continue
                val value = node.textContent.trim()
                colors.add(key to value)
            }

            generateColorAssets(colors)
        }
    }

    private fun sanitizeSvgFile(svgFile: File): File {
        val content = svgFile.readText()
        val patched = content.replace(
            Regex("""<stop(?![^>]*\boffset=)"""),
            """<stop offset="0%""""
        )

        svgFile.writeText(patched)

        return svgFile
    }

    private fun compressPng(inputFile: File, outputFile: File) {
        val image = ImageIO.read(inputFile)

        if (image == null) {
            logger.warn("❌ Failed to read image for compression: ${inputFile.name}")
            return
        }

        // Force new BufferedImage with same width/height and TYPE_INT_ARGB
        val compressedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = compressedImage.createGraphics()
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()

        // Delete file explicitly before rewriting
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val success = ImageIO.write(compressedImage, "png", outputFile)

        if (!success) {
            logger.warn("❌ PNG compression failed: ${outputFile.name}")
        }
    }

    private fun convertSvgToPng(
        svgFile: File,
        outputFile: File,
        scaleFactor: Float = 1f
    ) {
        // Parse SVG and extract width/height
        val documentBuilderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val builder = documentBuilderFactory.newDocumentBuilder()
        val document = builder.parse(svgFile)

        val svgRoot = document.documentElement

        // Attempt to get width/height from attributes (e.g., width="100", height="80")
        val rawWidth = svgRoot.getAttribute("width")
        val rawHeight = svgRoot.getAttribute("height")

        val width = rawWidth.removeSuffix("px").toFloatOrNull() ?: 100f
        val height = rawHeight.removeSuffix("px").toFloatOrNull() ?: 100f

        val transcoder = PNGTranscoder().apply {
            addTranscodingHint(PNGTranscoder.KEY_WIDTH, width * scaleFactor)
            addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height * scaleFactor)
        }

        FileInputStream(svgFile).use { svgInput ->
            FileOutputStream(outputFile).use { pngOutput ->
                val input = TranscoderInput(svgInput)
                val output = TranscoderOutput(pngOutput)
                transcoder.transcode(input, output)
            }
        }
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


    private fun generateColorAssets(colors: List<Pair<String, String>>) {
        val assetDir = File(xcAssetDirectoryPath)

        val filteredColors = colors.filter { (name, hex) -> !File(assetDir, "$name.colorset").exists() || forceRegenerate }
        if (filteredColors.isEmpty()) {
            logger.lifecycle("All Color Code files already exist. Skipping asset catalog assignment")
            return
        }

        filteredColors.forEach { (name, hex) ->
            val colorSetDir = File(assetDir, "$name.colorset")
            colorSetDir.mkdirs()

            val colorJson = """
        {
          "colors" : [
            {
              "idiom" : "universal",
              "color" : {
                "color-space" : "srgb",
                "components" : {
                  "red" : "${hexComponent(hex, 0)}",
                  "green" : "${hexComponent(hex, 1)}",
                  "blue" : "${hexComponent(hex, 2)}",
                  "alpha" : "1.0"
                }
              }
            }
          ],
          "info" : {
            "version" : 1,
            "author" : "Atlas"
          }
        }
        """.trimIndent()

            File(colorSetDir, "Contents.json").writeText(colorJson)
        }

        logger.lifecycle("🎨 iOS Color assets written to $xcAssetDirectoryPath")
    }

    private fun hexComponent(hex: String, index: Int): String {
        val cleaned = hex.removePrefix("#")
        val color = if (cleaned.length == 8) cleaned.substring(2) else cleaned
        return try {
            val component = color.substring(index * 2, index * 2 + 2).toInt(16)
            "%.3f".format(component / 255.0)
        } catch (e: Exception) {
            "0.0"
        }
    }
}


