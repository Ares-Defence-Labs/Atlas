package com.architect.atlas.container.plugins

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * Kotlin Multiplatform Compiler Plugin to generate AtlasContainer at compile time
 */
class AtlasCompilerPlugin : IrGenerationExtension {
    override fun generate(moduleFragment: org.jetbrains.kotlin.ir.declarations.IrModuleFragment, pluginContext: org.jetbrains.kotlin.backend.common.IrPluginContext) {
        val classes = moduleFragment.files.flatMap { it.declarations.filterIsInstance<IrClass>() }

        val annotatedClasses = classes.filter { cls ->
            cls.annotations.any { it.type.classFqName?.asString() == "com.architect.atlas.container.annotations.Singleton" }
        }

        val generatedCode = buildString {
            appendLine("package com.architect.atlas.container")
            appendLine("import kotlin.reflect.KClass")
            appendLine()
            appendLine("object AtlasContainer {")
            appendLine("    private val singletons: MutableMap<KClass<*>, Any> = mutableMapOf()")
            appendLine()
            appendLine("    init {")
            for (cls in annotatedClasses) {
                appendLine("        singletons[${cls.kotlinFqName}::class] = ${cls.kotlinFqName}()")
            }
            appendLine("    }")
            appendLine()
            appendLine("    fun <T : Any> resolve(clazz: KClass<T>): T {")
            appendLine("        return singletons[clazz] as? T ?: throw IllegalArgumentException(\"No provider found for \${clazz.simpleName}\")")
            appendLine("    }")
            appendLine("}")
        }

        // Writes `AtlasContainer.kt` to the build directory
        pluginContext.irFactory.createFile(
            moduleFragment.descriptor, // Module
            "AtlasContainer.kt",       // File Name
            generatedCode              // Contents
        )
    }
}