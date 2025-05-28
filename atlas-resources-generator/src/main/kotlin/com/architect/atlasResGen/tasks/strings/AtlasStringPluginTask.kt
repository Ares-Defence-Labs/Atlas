package com.architect.atlasResGen.tasks.strings

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@CacheableTask
abstract class AtlasStringPluginTask : DefaultTask(){

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    init {
        group = "AtlasStrings"
        description = "Generates a resource class file based on the xml specified"
    }

    @TaskAction
    fun generateStringClass() {
        val inputXmlFile = File(projectRootDir.get().asFile, "src/commonMain/resources/strings/strings.xml")
        if (!inputXmlFile.exists()) {
            logger.warn("❗️No strings.xml file found at: ${inputXmlFile.absolutePath}")
            return
        }

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = docBuilder.parse(inputXmlFile)

        val stringElements = document.getElementsByTagName("string")

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("package com.architect.atlas.resources.strings")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("class AtlasStrings {")
        stringBuilder.appendLine("    companion object {")

        for (i in 0 until stringElements.length) {
            val node = stringElements.item(i)
            val key = node.attributes?.getNamedItem("key")?.nodeValue ?: continue
            val value = node.textContent.replace("\"", "\\\"")

            stringBuilder.appendLine("        const val $key = \"$value\"")
        }

        stringBuilder.appendLine("    }")
        stringBuilder.appendLine("}")

        val outputPath = File(outputDir.get().asFile, "atlas/generated/strings")
        outputPath.mkdirs()

        val outputFile = File(outputPath, "AtlasStrings.kt")
        outputFile.writeText(stringBuilder.toString())

        logger.lifecycle("✅ AtlasStrings.kt generated at: ${outputFile.absolutePath}")
    }
}