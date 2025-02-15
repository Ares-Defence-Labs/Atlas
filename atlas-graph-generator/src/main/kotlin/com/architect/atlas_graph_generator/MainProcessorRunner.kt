//package com.architect.atlas_graph_generator
//
//import com.google.devtools.ksp.processing.SymbolProcessorProvider
//
//object MainProcessorRunner {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        println("🚀 Running AtlasDIProcessorProvider manually...")
//
//        // ✅ Manually instantiate the processor
//        val processorProvider: SymbolProcessorProvider = AtlasDIProcessorProvider()
//        println("✅ Symbol Processor Provider Loaded: ${processorProvider::class.qualifiedName}")
//    }
//}

package com.architect.atlas_graph_generator.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Singleton

@Singleton
class Testing{

}