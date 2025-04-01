package com.architect.atlasResGen.tasks.fonts

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
abstract class AtlasFontPluginTask : DefaultTask(){

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

    @get:Input
    abstract var isAndroidTarget: Boolean

    init {
        group = "AtlasFonts"
        description = "Generates a resource class file based on the xml specified"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun generateStringClass() {
        val inputXmlFile = File(projectRootDir.get().asFile, "src/commonMain/resources/fonts/fonts.xml")
        if (!inputXmlFile.exists()) {
            logger.warn("❗️No fonts.xml file found at: ${inputXmlFile.absolutePath}")
            return
        }

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = docBuilder.parse(inputXmlFile)

        val stringElements = document.getElementsByTagName("fontFamily")

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("package com.architect.atlas.resources.fonts")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("class AtlasFonts {")
        stringBuilder.appendLine("    companion object {")

        for (i in 0 until stringElements.length) {
            val node = stringElements.item(i)
            val key = node.attributes?.getNamedItem("key")?.nodeValue ?: continue
            val value = node.textContent.trim().replace("\"", "\\\"")

            stringBuilder.appendLine("        const val $key = \"$value\"")
        }

        stringBuilder.appendLine("    }")
        stringBuilder.appendLine("}")

        val outputPath = File(outputDir.get().asFile, "atlas/generated/fonts")
        outputPath.mkdirs()

        val outputFile = File(outputPath, "AtlasFonts.kt")
        outputFile.writeText(stringBuilder.toString())

        logger.lifecycle("✅ AtlasFonts.kt generated at: ${outputFile.absolutePath}")
    }
}