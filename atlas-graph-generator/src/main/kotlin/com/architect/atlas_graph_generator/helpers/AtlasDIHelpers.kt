package com.architect.atlas_graph_generator.helpers

import com.architect.atlasGraphGenerator.ProvidesFunctionInfo
import java.io.File

// Keep only elements whose key() simple name is in allowedSimple
fun <T> filterBySimple(
    input: Set<T>,
    allowedSimple: Set<String>,
    key: (T) -> String
): Set<T> =
    input.filterTo(mutableSetOf()) { t ->
        val candidate = key(t).substringAfterLast('.') // reduce to simple name
        candidate in allowedSimple
    }

// Keep only elements whose key() is in allowedFq (full package + class)
fun <T> filterByFullyQualified(
    input: Set<T>,
    allowedFq: Set<String>,
    key: (T) -> String
): Set<T> =
    input.filterTo(mutableSetOf()) { t ->
        val candidate = key(t) // expected to be an FQN already
        candidate in allowedFq
    }

// Rebuild classToPackage with only allowed simples
fun filterClassToPackage(
    classToPackage: Map<String, String>,
    allowedSimple: Set<String>
): Map<String, String> = classToPackage.filterKeys { it in allowedSimple }

fun buildAllowLists(roots: Set<File>): Pair<Set<String>, Set<String>> {
    val (fq, simpleToFq) = collectClassesFromRoots(roots)
    return fq to simpleToFq.keys
}

fun collectClassesFromRoots(roots: Set<File>): Pair<Set<String>, Map<String, String>> {
    val fqNames = mutableSetOf<String>()
    val simpleToFq = mutableMapOf<String, String>()

    roots.filter { it.isDirectory }
        .forEach { root ->
            root.walkTopDown()
                .filter { it.extension == "kt" }
                .forEach { file ->
                    val content = file.readText()
                    val pkg = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
                        .find(content)?.groupValues?.get(1)?.trim().orEmpty()

                    val classRegex = Regex("""\b(class|object)\s+([A-Za-z_]\w*)""")
                    classRegex.findAll(content).forEach { m ->
                        val simple = m.groupValues[2]
                        val fq = if (pkg.isNotBlank()) "$pkg.$simple" else simple
                        fqNames += fq
                        simpleToFq[simple] = fq
                    }
                }
        }

    return fqNames to simpleToFq
}

fun filterProvides(
    provides: Map<String, ProvidesFunctionInfo>,
    allowedFq: Set<String>,
    allowedSimple: Set<String>,
    allowedModulesSimple: Set<String>
): Map<String, ProvidesFunctionInfo> = provides.filter { (returnTypeFqcn, info) ->
    val returnOk =
        returnTypeFqcn in allowedFq || returnTypeFqcn.substringAfterLast('.') in allowedSimple
    val paramsOk = info.parameters.all { (_, paramFqcn) ->
        paramFqcn in allowedFq || paramFqcn.substringAfterLast('.') in allowedSimple
    }
    val moduleOk = info.module.substringAfterLast('.') in allowedModulesSimple

    returnOk && paramsOk && moduleOk
}

// Filter @Provides by FQN (with safe simple fallback)
fun filterProvidesByModuleMembership(
    provides: Map<String, ProvidesFunctionInfo>,
    inModuleFq: Set<String>,
    classToPackage: Map<String, String>,
    requireModuleOwner: Boolean = true
): Map<String, ProvidesFunctionInfo> = provides.filter { (returnTypeFqcn, info) ->
    val returnOk = returnTypeFqcn in inModuleFq ||
            inModuleFq.any { it.endsWith(".${returnTypeFqcn.substringAfterLast('.')}") }

    val moduleFq = classToPackage[info.module]?.let { "$it.${info.module}" }
    val moduleOk = if (!requireModuleOwner) {
        true
    } else {
        moduleFq != null && moduleFq in inModuleFq
    }

    returnOk && moduleOk
}

fun filterProvidesReturnTypes(
    providesReturnTypes: Map<String, String>,
    allowedFq: Set<String>,
    allowedSimple: Set<String>
): Map<String, String> = providesReturnTypes.filter { (retSimple, retFq) ->
    retFq in allowedFq || retSimple in allowedSimple
}

