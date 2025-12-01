package com.architect.atlas.navigationEngine.helpers

import com.architect.atlas.navigationEngine.tasks.models.Quad
import com.architect.atlas.navigationEngine.tasks.models.TabEntrySwift
import java.io.File


fun findViewModelImport(viewModelName: String, outputFiles: List<File>): String? {
    outputFiles.forEach { root ->
        root.walkTopDown().forEach { file ->
            if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
            val lines = file.readLines()

            // Check only if ViewModel is declared here (not just used)
            val declarationRegex = """(class|object)\s+$viewModelName\b""".toRegex()
            if (lines.any { declarationRegex.containsMatchIn(it) }) {
                val packageLine = lines.firstOrNull { it.trim().startsWith("package ") }
                    ?.removePrefix("package ")
                    ?.trim()
                return packageLine?.let { "$it.$viewModelName" }
            }
        }
    }
    return null
}

fun findFunctionImport(screenName: String, outputFiles: List<File>): String? {
    outputFiles.forEach { root ->
        root.walkTopDown().forEach { file ->
            if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
            val lines = file.readLines()
            if (lines.any { it.contains("fun $screenName") }) {
                val pkg = lines.firstOrNull { it.trim().startsWith("package ") }
                    ?.removePrefix("package ")?.trim()
                return pkg?.let { "$it.$screenName" }
            }
        }
    }
    return null
}

fun findScreenImport(screenClassName: String,outputFiles: List<File>): String? {
    outputFiles.forEach { root ->
        root.walkTopDown().forEach { file ->
            if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
            val lines = file.readLines()

            val declarationRegex = """(class|object)\s+$screenClassName\b""".toRegex()
            if (lines.any { declarationRegex.containsMatchIn(it) }) {
                val packageLine = lines.firstOrNull { it.trim().startsWith("package ") }
                    ?.removePrefix("package ")
                    ?.trim()
                return packageLine?.let { "$it.$screenClassName" }
            }
        }
    }
    return null
}

fun scanIosTabAnnotationsFromSwiftFiles(sourceDirs: List<File>): Map<String, List<TabEntrySwift>> {
    val tabEntries = mutableListOf<TabEntrySwift>()
    val swiftAnnotationRegex =
        Regex("""//@AtlasSwiftTab\(\s*(\w+)::class\s*,\s*position\s*=\s*(\d+)\s*,\s*holder\s*=\s*(\w+)::class(?:\s*,\s*initialSelected\s*=\s*(true|false))?\s*\)""")

    sourceDirs.forEach {
        it.walkTopDown().filter { it.isFile && it.extension == "swift" }.forEach { file ->
            val content = file.readText()

            swiftAnnotationRegex.findAll(content).forEach { match ->
                val viewModel = match.groupValues[1]
                val position = match.groupValues[2].toInt()
                val holder = match.groupValues[3]
                val screenName = file.name.removeSuffix(".swift")

                val initialSelected =
                    match.groupValues.getOrNull(4)?.toBooleanStrictOrNull() ?: false
                tabEntries.add(
                    TabEntrySwift(
                        viewModel = viewModel,
                        screen = screenName + ".swift",
                        position = position,
                        holder = holder,
                        initialSelected = initialSelected
                    )
                )
            }
        }
    }

    return tabEntries.groupBy { it.holder }
}


