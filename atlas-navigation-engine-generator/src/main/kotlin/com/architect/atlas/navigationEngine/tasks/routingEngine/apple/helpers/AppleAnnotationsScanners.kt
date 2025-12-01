package com.architect.atlas.navigationEngine.tasks.routingEngine.apple.helpers

import com.architect.atlas.navigationEngine.tasks.models.Quad
import java.io.File

fun scanViewModelSwiftAnnotations(outputFiles: List<File>): List<Quad<String, String, String, Boolean>> {
    val results = mutableListOf<Quad<String, String, String, Boolean>>()
    val swiftRegex =
        """^\s*//\s*@?AtlasScreen\s*\(\s*viewModel\s*:\s*([A-Za-z0-9_]+)\.self\s*(?:,\s*initial\s*:\s*(true|false))?\s*\)""".toRegex()

    outputFiles.forEach { subProject ->
        subProject.walkTopDown().forEach { file ->
            if (!file.isFile || !file.extension.equals("swift", true)) return@forEach

            val lines = file.readLines()
            var pendingAnnotation: MatchResult? = null

            for ((index, line) in lines.withIndex()) {
                println("📄 [${file.name}:${index + 1}] $line")

                val rawLine = line // keep it untrimmed for context

                // Step 1: Look for the annotation
                if (rawLine.contains("@AtlasScreen")) {
                    val match = swiftRegex.find(rawLine)

                    println("🔍 Attempting regex match on line: '$rawLine'")
                    println(
                        "🔣 Unicode points: ${
                            rawLine.map { it.code }.joinToString(", ") { "U+%04X".format(it) }
                        }"
                    )
                    println("✅ Matched: ${match != null}")
                    println("📦 Groups: ${match?.groupValues}")

                    if (match != null) {
                        println("🟨 Found @AtlasScreen annotation in ${file.name} line ${index + 1}")
                        pendingAnnotation = match
                    } else {
                        println("❌ No match found for: '$rawLine'")
                    }

                    continue
                }

                // Step 2: If we have a pending annotation, check for class/struct declaration
                if (pendingAnnotation != null &&
                    (rawLine.trim().startsWith("struct ") || rawLine.trim()
                        .startsWith("class "))
                ) {
                    val currentClass =
                        rawLine.trim().split("\\s+".toRegex()).getOrNull(1)?.trim()
                    if (currentClass != null) {
                        val viewModelName = pendingAnnotation.groupValues[1]
                        val isInitial =
                            pendingAnnotation.groupValues.getOrNull(2)?.toBooleanStrictOrNull()
                                ?: false

                        println("✅ Matched @AtlasScreen to class $currentClass (vm=$viewModelName, initial=$isInitial)")

                        results.add(
                            Quad(
                                viewModelName,
                                currentClass,
                                file.absolutePath,
                                isInitial
                            )
                        )
                    } else {
                        error("Could not resolve class/struct for @AtlasScreen in ${file.name} line ${index + 1}")
                    }

                    pendingAnnotation = null
                }
            }
        }
    }

    return results
}
