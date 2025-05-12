package com.architect.atlasResGen.tasks.fonts

import com.architect.atlas.common.helpers.FileHelpers
import com.architect.atlasResGen.helpers.ResGenFileHelpers
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class AppleAtlasFontPluginTask : DefaultTask() {
    @get:OutputDirectory
    abstract val iOSResourcesFontsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputFile
    abstract var mergedInfoPlist: File

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    abstract var forceRegenerate: Boolean

    init {
        group = "AtlasFonts"
        description = "Generates a resource class file based on the xml specified"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateFontClass() {
        logger.info("Running font files for iOS")
        val fontsDir = File(projectRootDir.get().asFile, "src/commonMain/resources/fonts")
        if (!fontsDir.exists()) {
            logger.warn("\u2757\ufe0f No Fonts folder found at: ${fontsDir.absolutePath}")
            return
        }

        //logger.info("Running font files for iOS")
        val fontFiles = fontsDir.walk()
            .filter {
                it.isFile && it.extension.lowercase() in listOf("ttf", "otf")
            }
            .toList()

        logger.info("Font files detected $fontFiles")
        val snakeToPath = fontFiles.associate { file ->
            FileHelpers.getTrimmedFilePath(file).nameWithoutExtension to
                    FileHelpers.getTrimmedFilePath(
                        file
                    ).name
        }


        prepareInternalFontInfoPlist(snakeToPath.values.toList()) // write the merged manifest info.plist
        generateiOSActualFontObject(fontFiles.associate { file ->
            FileHelpers.getTrimmedFilePath(file).nameWithoutExtension to
                    file
        })
        copyFontsToiOSAssets(fontFiles)
    }

    private fun prepareInternalFontInfoPlist(fontFiles: List<String>) {
        val snippet = buildString {
            appendLine("<array>")
            fontFiles.forEach {
                val path = it
                appendLine("    <string>$path</string>")
            }
            appendLine("</array>")
        }

        mergedInfoPlist.apply {
            writeText(snippet)
        }
    }

    private fun generateiOSActualFontObject(entries: Map<String, File>) {
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.fonts")
        builder.appendLine()
        builder.appendLine("import platform.UIKit.UIFont")
        builder.appendLine()
        builder.appendLine("class AtlasFonts {")
        builder.appendLine("    companion object {")

        for ((name, file) in entries) {
            val fontName = file.name
            val postName = ResGenFileHelpers.extractPostScriptName(file)

            logger.lifecycle("FONT NAME : $postName")
            builder.appendLine("    fun $name(size: Double): UIFont =")
            builder.appendLine("        UIFont.fontWithName(fontName = \"$postName\", size = size) ?:")
            builder.appendLine("            error(\"❌ Font not found: $fontName\")")
            builder.appendLine()
        }

        builder.appendLine("    }")
        builder.appendLine("}")

        val file = File(outputDir.get().asFile, "AtlasFonts.kt")
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }

    private fun copyFontsToiOSAssets(fontFiles: List<File>) {
        val targetDir = iOSResourcesFontsDir.asFile.get()
        targetDir.mkdirs()

        fontFiles.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            if (!targetFile.exists() || forceRegenerate) {
                sourceFile.copyTo(FileHelpers.getTrimmedFilePath(targetFile), overwrite = true)
            }
        }

        logger.lifecycle("✅ Copied ${fontFiles.size} images to iOS assets: ${targetDir.absolutePath}")
    }

}