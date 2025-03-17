package com.architect.atlas.container.plugins

import kotlin.reflect.KClass

object DependencyGraphGenerator {
    private val singletons = mutableSetOf<KClass<*>>()

    fun registerSingleton(clazz: KClass<*>) {
        singletons.add(clazz)
    }

    /**
     * Generates the final AtlasContainer.kt at compile time
     */
    fun generateAtlasContainer(): String {
        return buildString {
            appendLine("package com.architect.atlas.container")
            appendLine("import kotlin.reflect.KClass")
            appendLine()
            appendLine("object AtlasContainer {")
            appendLine("    private val singletons: MutableMap<KClass<*>, Any> = mutableMapOf()")
            appendLine()
            appendLine("    init {")
            for (cls in singletons) {
                appendLine("        singletons[${cls.simpleName}::class] = ${cls.simpleName}()")
            }
            appendLine("    }")
            appendLine()
            appendLine("    fun <T : Any> resolve(clazz: KClass<T>): T {")
            appendLine("        return singletons[clazz] as? T ?: throw IllegalArgumentException(\"No provider found for \${clazz.simpleName}\")")
            appendLine("    }")
            appendLine("}")
        }
    }
}


// This is automatically generated at compile-time
val generatedAtlasContainer = DependencyGraphGenerator.generateAtlasContainer()

// Writes the generated container to the project (or IDE memory)
@OptIn(ExperimentalStdlibApi::class)
fun compileTimeRegister() {
    println("🔧 Registering AtlasContainer at compile time...")
    println(generatedAtlasContainer)
}