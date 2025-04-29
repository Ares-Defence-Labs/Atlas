package com.architect.atlasResGen.tasks.fonts

import com.architect.atlas.common.helpers.FileHelpers
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
abstract class AtlasFontPluginTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    // platform outputs
    @get:OutputDirectory
    abstract val androidResourcesFontsDir: DirectoryProperty

    @get:OutputDirectory
    abstract var androidResourcePackageRef: String

    @get:Input
    abstract var forceRegenerate: Boolean

    init {
        group = "AtlasFonts"
        description = "Generates a resource class file based on the xml specified"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateStringClass() {
        val fontsDir = File(projectRootDir.get().asFile, "src/commonMain/resources/fonts")
        if (!fontsDir.exists()) {
            logger.warn("\u2757\ufe0f No Fonts folder found at: \${fontsDir.absolutePath}")
            return
        }

        val fontFiles = fontsDir.walk()
            .filter {
                it.isFile && it.extension.lowercase() in listOf("ttf", "otf")
            }
            .toList()

        val snakeToPath = fontFiles.associate { file ->
            FileHelpers.getTrimmedFilePath(file).nameWithoutExtension to "fonts/${
                FileHelpers.getTrimmedFilePath(
                    file
                ).name
            }" // safe because file is explicitly named
        }

        logger.lifecycle("PATHS : $snakeToPath")
        generateAndroidActualFontObject(snakeToPath)
        copyFontsToAndroidAssets(fontFiles)

    }

    private fun generateAndroidActualFontObject(entries: Map<String, String>) {
        val builder = StringBuilder()
        builder.appendLine("package com.architect.atlas.resources.fonts")
        builder.appendLine()
        builder.appendLine("import android.content.Context")
        builder.appendLine("import android.graphics.Typeface")
        builder.appendLine("import androidx.core.content.res.ResourcesCompat")
        builder.appendLine("import $androidResourcePackageRef.R")

        builder.appendLine()
        builder.appendLine("object AtlasFonts {")

        for ((name, path) in entries) {
            logger.lifecycle("PATH : $path")
            logger.lifecycle("NAME : $name")
            builder.appendLine("    fun $name(context: Context): Typeface = ResourcesCompat.getFont(context, R.font.$name)")
            builder.appendLine("            ?: error(\"Font Typeface not found: R.font.$name\")")
        }

        builder.appendLine("}")

        val file = File(androidOutputDir.get().asFile, "AtlasFonts.kt")
        file.parentFile.mkdirs()
        file.writeText(builder.toString())
    }

    private fun copyFontsToAndroidAssets(fontFiles: List<File>) {
        val targetDir = androidResourcesFontsDir.asFile.get()
        targetDir.mkdirs()

        fontFiles.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            if (!targetFile.exists() || forceRegenerate) {
                sourceFile.copyTo(FileHelpers.getTrimmedFilePath(targetFile), overwrite = true)
            }
        }

        logger.lifecycle("✅ Copied ${fontFiles.size} images to Android assets: ${targetDir.absolutePath}")
    }
}

