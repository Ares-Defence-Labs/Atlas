//package com.architect.atlas_graph_generator
//
//import com.architect.atlas_graph_generator.processors.Singleton
//import com.google.devtools.ksp.processing.*
//import com.google.devtools.ksp.symbol.*
//import java.io.OutputStream
//
//class AtlasDIProcessor(
//    private val codeGenerator: CodeGenerator,
//    private val logger: KSPLogger
//) : SymbolProcessor {
//
//    private val singletons = mutableListOf<String>()
//    private val factories = mutableListOf<String>()
//
//    override fun process(resolver: Resolver): List<KSAnnotated> {
//        logger.warn("🚀 AtlasDIProcessor is running!") // ✅ Debug log
//
//        // Find all classes annotated with @Singleton or @Factory
//        resolver.getSymbolsWithAnnotation(Singleton::class.qualifiedName!!)
//            .filterIsInstance<KSClassDeclaration>()
//            .forEach { singletons.add(it.qualifiedName?.asString() ?: "") }
//
////        resolver.getSymbolsWithAnnotation("com.architect.atlas.container.annotations.Factory")
////            .filterIsInstance<KSClassDeclaration>()
////            .forEach { factories.add(it.qualifiedName?.asString() ?: "") }
//
//        // Generate Dependency Graph
//        generateDependencyGraph()
//        return emptyList()
//    }
//
//    private fun generateDependencyGraph() {
//        val file: OutputStream = codeGenerator.createNewFile(
//            Dependencies(false),
//            "com.architect.atlas.container",
//            "GeneratedDI"
//        )
//
//        file.writer().use {
//            it.write(
//                """
//                package com.architect.atlas.container
//
//                object GeneratedDI {
//                    private val singletons = mutableMapOf<Class<*>, Any>()
//                    private val factories = mutableMapOf<Class<*>, () -> Any>()
//
//                    init {
//                        ${singletons.joinToString("\n") { "singletons[${it}::class.java] = ${it}()" }}
//                        ${factories.joinToString("\n") { "factories[${it}::class.java] = { ${it}() }" }}
//                    }
//
//                    fun <T : Any> resolve(clazz: Class<T>): T {
//                        return (singletons[clazz] ?: factories[clazz]?.invoke()) as? T
//                            ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
//                    }
//                }
//                """.trimIndent()
//            )
//        }
//
//        logger.warn("✅ Generated file: GeneratedDI.kt")
//    }
//}