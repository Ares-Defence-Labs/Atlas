package com.architect.atlasResGen.tasks.colors

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@CacheableTask
abstract class AtlasColorsPluginTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasColors"
        description = "Generates a resource class file based on the xml specified"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
    }

    @TaskAction
    fun generateColorClass() {
        val xmlFile = File(projectRootDir.get().asFile, "src/commonMain/resources/colors/colors.xml")
        if (!xmlFile.exists()) {
            logger.warn("❗️No colors.xml file found at ${xmlFile.absolutePath}")
            return
        }

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = docBuilder.parse(xmlFile)
        val colorElements = document.getElementsByTagName("color")

        val colors = mutableListOf<Pair<String, String>>()
        for (i in 0 until colorElements.length) {
            val node = colorElements.item(i)
            val key = node.attributes?.getNamedItem("key")?.nodeValue ?: continue
            val value = node.textContent.trim()
            colors.add(key to value)
        }

        // --- 2. Generate actual object + class for Android ---
        val androidAtlasColors = buildString {
            appendLine("package com.architect.atlas.resources.colors")
            appendLine()
            appendLine("import android.graphics.Color")
            appendLine()
            appendLine("class AtlasColors {")
            appendLine("    companion object {")
            colors.forEach { (key, value) ->
                appendLine("  val $key = PlatformColor(\"$value\")")
            }
            appendLine("}")
            appendLine("}")
        }

        val androidPlatformColor = """
            package com.architect.atlas.resources.colors

            class PlatformColor (val raw: String) {
                val colorInt: Int
                    get() = android.graphics.Color.parseColor(raw)
            }
        """.trimIndent()

        val androidOut = File(androidOutputDir.get().asFile, "colors")
        androidOut.mkdirs()
        File(androidOut, "AtlasColors.kt").writeText(androidAtlasColors)
        File(androidOut, "PlatformColor.kt").writeText(androidPlatformColor)

        // --- 3. Generate actual object + class for iOS ---
        val iosAtlasColors = buildString {
            appendLine("package com.architect.atlas.resources.colors")
            appendLine()
            appendLine("class AtlasColors {")
            appendLine("companion object {")
            colors.forEach { (key, value) ->
                appendLine("    val $key = PlatformColor(\"$value\")")
            }
            appendLine("}")
            appendLine("}")
        }

        val iosPlatformColor = """
            package com.architect.atlas.resources.colors
            
            import platform.UIKit.UIColor

            class PlatformColor(val raw: String) {
                fun uiColor(): UIColor = hexToUIColor(raw)
                fun swiftUIColor(): String = raw
            }

            private fun hexToUIColor(hex: String): UIColor {
                val cleaned = hex.removePrefix("#")
                val color = when (cleaned.length) {
                    6 -> cleaned
                    8 -> cleaned.substring(2) // ignore alpha
                    else -> return UIColor.blackColor
                }

                val r = color.substring(0, 2).toInt(16).toDouble() / 255.0
                val g = color.substring(2, 4).toInt(16).toDouble() / 255.0
                val b = color.substring(4, 6).toInt(16).toDouble() / 255.0

                return UIColor(red = r, green = g, blue = b, alpha = 1.0)
            }
        """.trimIndent()

        val iosOut = File(outputIosDir.get().asFile, "colors")
        iosOut.mkdirs()
        File(iosOut, "AtlasColors.kt").writeText(iosAtlasColors)
        File(iosOut, "PlatformColor.kt").writeText(iosPlatformColor)

        logger.lifecycle("✅ AtlasColors + PlatformColor generated for commonMain, androidMain, and iosMain")
    }
}