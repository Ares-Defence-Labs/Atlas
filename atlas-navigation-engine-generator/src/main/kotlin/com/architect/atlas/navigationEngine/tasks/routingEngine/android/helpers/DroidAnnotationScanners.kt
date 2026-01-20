package com.architect.atlas.navigationEngine.tasks.routingEngine.android.helpers

import com.architect.atlas.navigationEngine.tasks.models.Quad
import com.architect.atlas.navigationEngine.tasks.models.TabEntry
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.logging.Logger
import java.io.File

fun scanTabAnnotations(outputFiles: List<File>, logger: Logger): Map<String, List<TabEntry>> {
    val tabGroups = mutableMapOf<String, MutableList<TabEntry>>()

    outputFiles.forEach { root ->
        root.walkTopDown().forEach { file ->
            if (!file.isFile || !file.extension.equals("kt", true)) return@forEach
            val lines = file.readLines()

            for ((index, line) in lines.withIndex()) {
                if (line.contains("@AtlasTab")) {
                    val annotationBlock = lines.drop(index).take(5).joinToString(" ")
                    println("📌 Annotation Block: $annotationBlock")

                    val viewModelRegex =
                        """@AtlasTab\s*\(\s*([\w.]+)::class""".toRegex()  // positional
                    val namedViewModelRegex = """viewModel\s*=\s*([\w.]+)::class""".toRegex()

                    val positionRegex = """position\s*=\s*(\d+)""".toRegex()
                    val holderRegex = """holder\s*=\s*([\w.]+)::class""".toRegex()

                    val viewModel = viewModelRegex.find(annotationBlock)?.groupValues?.get(1)
                        ?: namedViewModelRegex.find(annotationBlock)?.groupValues?.get(1)
                    val position =
                        positionRegex.find(annotationBlock)?.groupValues?.get(1)?.toIntOrNull()
                    val holder = holderRegex.find(annotationBlock)?.groupValues?.get(1)

                    println("🧩 Parsed: viewModel=$viewModel, position=$position, holder=$holder")

                    if (viewModel == null || holder == null || position == null) {
                        logger.warn("⚠️ Could not parse @AtlasTab at ${file.name}:${index + 1}")
                        continue
                    }

                    // Find the next function name
                    val screenName = lines.drop(index + 1)
                        .take(10)
                        .firstOrNull { it.trim().startsWith("fun ") }
                        ?.let { """fun\s+(\w+)""".toRegex().find(it)?.groupValues?.get(1) }

                    if (screenName == null) {
                        logger.warn("⚠️ Could not extract function name for @AtlasTab in ${file.name} near line ${index + 1}")
                        continue
                    }

                    tabGroups.getOrPut(holder) { mutableListOf() }
                        .add(TabEntry(viewModel, screenName, holder, position))
                }
            }
        }
    }

    return tabGroups
}


@Suppress("MemberVisibilityCanBePrivate")
fun scanViewModelAnnotationsClassical(
    outputFiles: List<File>,
    logger: Logger,
    androidSourceFiles: ConfigurableFileCollection
): List<Quad<String, String, String, Boolean>> {
    val results = mutableListOf<Quad<String, String, String, Boolean>>()

    // 1) Build the set of root directories to scan
    val roots = linkedSetOf<File>()

    // Existing roots (whatever you had wired into outputFiles)
    outputFiles.forEach { roots += it }

    // Android source roots (androidApp/src/main/kotlin etc.)
    androidSourceFiles.files.forEach { roots += it }

    if (roots.isEmpty()) {
        logger.warn("AtlasNav: scanViewModelAnnotations – no roots configured, returning empty list")
        return emptyList()
    }

    // Regex to match @AtlasScreen(SomeVm::class, initial = true/false)
    val atlasRegex =
        """@AtlasScreen\(\s*([A-Za-z0-9_.]+)::class(?:\s*,\s*initial\s*=\s*(true|false))?\s*\)"""
            .toRegex()

    // Regex to get the screen class name from 'class FirstTestFragment : Fragment(...)'
    val classRegex = """class\s+([A-Za-z0-9_]+)""".toRegex()

    for (root in roots) {
        if (!root.exists()) continue

        root.walkTopDown()
            .filter { it.isFile && it.extension.equals("kt", ignoreCase = true) }
            .forEach { file ->
                val text = file.readText()

                // Find the first 'class X' in the file as the screen name
                val classMatch = classRegex.find(text) ?: return@forEach
                val screenName = classMatch.groupValues[1]

                // Find all @AtlasScreen(...) annotations in this file
                atlasRegex.findAll(text).forEach { match ->
                    val vmFqnOrSimple =
                        match.groupValues[1] // could be DroidStandard or com.foo.DroidStandard
                    val vmSimpleName = vmFqnOrSimple.substringAfterLast('.')

                    val initialFlag = match.groupValues.getOrNull(2)?.let {
                        it.equals("true", ignoreCase = true)
                    } ?: false

                    results += Quad(
                        vmSimpleName,          // first  -> ViewModel simple name, e.g. "DroidStandard"
                        screenName,            // second -> Screen class name, e.g. "FirstTestFragment"
                        file.absolutePath,     // third  -> file path
                        initialFlag            // fourth -> initial = true / false
                    )
                }
            }
    }

    logger.lifecycle("AtlasNav: scanViewModelAnnotations found ${results.size} screens")
    return results
}

@Suppress("MemberVisibilityCanBePrivate")
fun scanViewModelAnnotationsCompose(
    outputFiles: List<File>,
    logger: Logger,
    androidSourceFiles: ConfigurableFileCollection
): List<Quad<String, String, String, Boolean>> {
    val results = mutableListOf<Quad<String, String, String, Boolean>>()

    // 1) Build the set of root directories to scan
    val roots = linkedSetOf<File>()

    // Existing roots (whatever you had wired into outputFiles)
    outputFiles.forEach { roots += it }

    // Android source roots (androidApp/src/main/kotlin etc.)
    androidSourceFiles.files.forEach { roots += it }

    if (roots.isEmpty()) {
        logger.warn("AtlasNav: scanViewModelAnnotationsCompose – no roots configured, returning empty list")
        return emptyList()
    }

    // Regex to match:
    //   @AtlasScreen(DroidStandard::class, initial = true)
    //   @AtlasScreen(viewModel = DroidStandard::class, initial = true)
    val atlasRegex =
        """@AtlasScreen\(\s*(?:viewModel\s*=\s*)?([A-Za-z0-9_.]+)::class(?:\s*,\s*initial\s*=\s*(true|false))?(?:\s*,\s*isTabHolder\s*=\s*(true|false))?\s*\)"""
            .toRegex()

    // Regex to get the *function* name from 'fun GreetingView(...'
    val funRegex = """fun\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(""".toRegex()

    for (root in roots) {
        if (!root.exists()) continue

        root.walkTopDown()
            .filter { it.isFile && it.extension.equals("kt", ignoreCase = true) }
            .forEach { file ->
                val text = file.readText()

                // Find all @AtlasScreen(...) annotations in this file
                atlasRegex.findAll(text).forEach { match ->
                    val vmFqnOrSimple =
                        match.groupValues[1] // could be DroidStandard or com.foo.DroidStandard
                    val vmSimpleName = vmFqnOrSimple.substringAfterLast('.')

                    val initialFlag = match.groupValues.getOrNull(2)?.let {
                        it.equals("true", ignoreCase = true)
                    } ?: false

                    // Look for the FIRST `fun` *after* this annotation as the screen function
                    val afterAnnotation = text.substring(match.range.last + 1)
                    val funMatch = funRegex.find(afterAnnotation)

                    if (funMatch == null) {
                        logger.warn(
                            "AtlasNav: @AtlasScreen for $vmSimpleName in ${file.name} " +
                                    "but no following function declaration found"
                        )
                        return@forEach
                    }

                    val functionName = funMatch.groupValues[1] // e.g. GreetingView

                    results += Quad(
                        vmSimpleName,          // first  -> ViewModel simple name, e.g. "DroidStandard"
                        functionName,          // second -> Composable function name, e.g. "GreetingView"
                        file.absolutePath,     // third  -> file path
                        initialFlag            // fourth -> initial = true / false
                    )
                }
            }
    }

    logger.lifecycle("AtlasNav: scanViewModelAnnotationsCompose found ${results.size} screens")
    return results
}