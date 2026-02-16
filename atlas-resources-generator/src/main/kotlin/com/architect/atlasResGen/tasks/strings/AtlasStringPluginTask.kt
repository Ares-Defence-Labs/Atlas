package com.architect.atlasResGen.tasks.strings

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@CacheableTask
abstract class AtlasStringPluginTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    init {
        group = "AtlasStrings"
        description = "Generates a resource class file based on the xml specified"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
    }

    private fun unescapeXmlBackslashSequences(raw: String): String {
        return raw
            .replace("\\'", "'")
            .replace("\\\"", "\"")
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    private fun kotlinSingleLineLiteral(raw: String): String {
        // Normal "..." string
        return buildString {
            append('"')
            for (ch in raw) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '$' -> append("\\$")
                    else -> append(ch)
                }
            }
            append('"')
        }
    }

    private fun kotlinTripleQuotedLiteral(raw: String): String {
        val normalised = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            // prevent string interpolation inside """ """
            .replace("$", "\\$")
            // avoid ending the triple quote accidentally
            .replace("\"\"\"", "\"\"\\\"\"")

        return "\"\"\"${normalised.trimIndent()}\"\"\""
    }

    private fun kotlinStringKeyLiteral(raw: String): String {
        // Used for map keys, must be valid Kotlin string literal
        return buildString {
            append('"')
            for (ch in raw) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '$' -> append("\\$")
                    else -> append(ch)
                }
            }
            append('"')
        }
    }

    private fun toValidKotlinIdentifier(key: String): String {
        var s = key.replace(Regex("[^A-Za-z0-9_]"), "_")
        if (s.isEmpty()) s = "_"
        if (s.first().isDigit()) s = "_$s"
        val keywords = setOf(
            "package","as","typealias","class","this","super","val","var","fun","for","null","true","false",
            "is","in","throw","return","break","continue","object","if","try","else","while","do","when",
            "interface","typeof"
        )
        if (s in keywords) s = "${s}_"

        return s
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

        data class Entry(val originalKey: String, val kotlinName: String, val valueLiteral: String)

        val entries = mutableListOf<Entry>()

        for (i in 0 until stringElements.length) {
            val node = stringElements.item(i)
            val originalKey = node.attributes?.getNamedItem("key")?.nodeValue ?: continue
            val rawValue = node.textContent ?: ""

            val cookedValue = unescapeXmlBackslashSequences(rawValue)

            val valueLiteral = if (cookedValue.contains('\n') || cookedValue.contains('\r')) {
                kotlinTripleQuotedLiteral(cookedValue)
            } else {
                kotlinSingleLineLiteral(cookedValue)
            }

            val kotlinName = toValidKotlinIdentifier(originalKey)
            entries += Entry(originalKey = originalKey, kotlinName = kotlinName, valueLiteral = valueLiteral)
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("package com.architect.atlas.resources.strings")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("import kotlin.jvm.JvmStatic")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("class AtlasStrings {")
        stringBuilder.appendLine("    companion object {")

        // 1) const vals
        for (e in entries) {
            stringBuilder.appendLine("        const val ${e.kotlinName} = ${e.valueLiteral}")
        }

        stringBuilder.appendLine("")
        // 2) key lookup map
        stringBuilder.appendLine("        private val _byKey: Map<String, String> by lazy(LazyThreadSafetyMode.NONE) {")
        stringBuilder.appendLine("            mapOf(")
        for (e in entries) {
            val keyLit = kotlinStringKeyLiteral(e.originalKey)
            stringBuilder.appendLine("                $keyLit to ${e.kotlinName},")
        }
        stringBuilder.appendLine("            )")
        stringBuilder.appendLine("        }")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("        @JvmStatic")
        stringBuilder.appendLine("        fun get(key: String): String? = _byKey[key]")

        stringBuilder.appendLine("    }")
        stringBuilder.appendLine("}")
        stringBuilder.appendLine("")

        val outputPath = File(outputDir.get().asFile, "atlas/generated/strings")
        outputPath.mkdirs()

        val outputFile = File(outputPath, "AtlasStrings.kt")
        outputFile.writeText(stringBuilder.toString())

        logger.lifecycle("✅ AtlasStrings.kt generated at: ${outputFile.absolutePath}")
    }
}