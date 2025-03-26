package com.architect.atlasResGen.tasks.colors

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidResources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputIosDir: DirectoryProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    init {
        group = "AtlasColors"
        description = "Generates a resource class file based on the xml specified"
        outputs.upToDateWhen { false }
        androidResources.from(project.layout.projectDirectory.dir("androidApp/build/intermediates/res/merged/debug"))
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

        // --- 1. Generate expect declarations in commonMain ---
        val atlasColorsExpect = buildString {
            appendLine("package com.architect.atlas.resources.colors")
            appendLine()
            appendLine("expect class AtlasColors {")
            appendLine("companion object {")
            colors.forEach { (key, _) ->
                appendLine("    val $key: PlatformColor")
            }
            appendLine("}")
            appendLine("}")
        }

        val platformColorExpect = """
            package com.architect.atlas.resources.colors

            expect class PlatformColor {
                val raw: String
            }
        """.trimIndent()

        val commonOut = File(outputDir.get().asFile, "atlas/generated/colors")
        commonOut.mkdirs()
        File(commonOut, "AtlasColors.kt").writeText(atlasColorsExpect)
        File(commonOut, "PlatformColor.kt").writeText(platformColorExpect)

        // --- 2. Generate actual object + class for Android ---
        val androidAtlasColors = buildString {
            appendLine("package com.architect.atlas.resources.colors")
            appendLine()
            appendLine("import android.graphics.Color")
            appendLine()
            appendLine("actual class AtlasColors {")
            appendLine("actual companion object {")
            colors.forEach { (key, value) ->
                appendLine("    actual val $key = PlatformColor(\"$value\")")
            }
            appendLine("}")
            appendLine("}")
        }

        val androidPlatformColor = """
            package com.architect.atlas.resources.colors

            actual class PlatformColor (actual val raw: String) {
                val colorInt: Int
                    get() = android.graphics.Color.parseColor(raw)
            }
        """.trimIndent()

        val androidOut = File(androidOutputDir.get().asFile, "atlas/generated/colors")
        androidOut.mkdirs()
        File(androidOut, "AtlasColors.kt").writeText(androidAtlasColors)
        File(androidOut, "PlatformColor.kt").writeText(androidPlatformColor)

        // --- 3. Generate actual object + class for iOS ---
        val iosAtlasColors = buildString {
            appendLine("package com.architect.atlas.resources.colors")
            appendLine()
            appendLine("actual class AtlasColors {")
            appendLine("actual companion object {")
            colors.forEach { (key, value) ->
                appendLine("    actual val $key = PlatformColor(\"$value\")")
            }
            appendLine("}")
            appendLine("}")
        }

        val iosPlatformColor = """
            package com.architect.atlas.resources.colors
            
            import platform.UIKit.UIColor

            actual class PlatformColor(actual val raw: String) {
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

        val iosOut = File(outputIosDir.get().asFile, "atlas/generated/colors")
        iosOut.mkdirs()
        File(iosOut, "AtlasColors.kt").writeText(iosAtlasColors)
        File(iosOut, "PlatformColor.kt").writeText(iosPlatformColor)

        logger.lifecycle("✅ AtlasColors + PlatformColor generated for commonMain, androidMain, and iosMain")
    }
}