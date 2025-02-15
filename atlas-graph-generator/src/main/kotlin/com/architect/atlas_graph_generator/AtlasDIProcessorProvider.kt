//package com.architect.atlas_graph_generator
//
//import com.google.devtools.ksp.processing.SymbolProcessor
//import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
//import com.google.devtools.ksp.processing.SymbolProcessorProvider
//
//class AtlasDIProcessorProvider : SymbolProcessorProvider {
//    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
//        environment.logger.warn("🚀 AtlasDIProcessorProvider is being registered!")
//        println("🚀Testing")
//        return AtlasDIProcessor(environment.codeGenerator, environment.logger)
//    }
//}
//
