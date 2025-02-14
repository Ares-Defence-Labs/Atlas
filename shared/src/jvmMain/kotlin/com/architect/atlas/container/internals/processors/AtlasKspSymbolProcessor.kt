package com.architect.atlas.container.internals.processors

import com.architect.atlas.container.annotations.Factory
import com.architect.atlas.container.annotations.Module
import com.architect.atlas.container.annotations.Scoped
import com.architect.atlas.container.annotations.Singleton
import com.architect.atlas.container.annotations.ViewModel
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter
import kotlin.reflect.KClass

class AtlasKspSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val processedClasses = mutableMapOf<KClass<*>, Any>()
    fun processAnnotatedClasses(): Map<KClass<*>, Any> {
        return processedClasses
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val singletonClasses =
            resolver.getSymbolsWithAnnotation(Singleton::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()

        val transientClasses =
            resolver.getSymbolsWithAnnotation(Factory::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()

        val scopedClasses =
            resolver.getSymbolsWithAnnotation(Scoped::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()

        val viewModelClasses =
            resolver.getSymbolsWithAnnotation(ViewModel::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()

        val moduleClasses =
            resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()

        if (singletonClasses.any() || transientClasses.any() || scopedClasses.any() ||
            viewModelClasses.any() || moduleClasses.any()
        ) {
            val environment = resolver.getNewFiles()
                .firstOrNull()?.filePath ?: ""
            val isAndroidModule = environment.contains("androidMain")
            generateAtlasContainer(
                moduleClasses,
                singletonClasses,
                transientClasses,
                scopedClasses,
                viewModelClasses,
                isAndroidModule
            )
        }

        return emptyList()
    }

    private fun generateAtlasContainer(
        modules: Sequence<KSClassDeclaration>,
        singletons: Sequence<KSClassDeclaration>,
        transients: Sequence<KSClassDeclaration>,
        scoped: Sequence<KSClassDeclaration>,
        viewModels: Sequence<KSClassDeclaration>,
        isAndroidModule: Boolean
    ) {
        val packageName = "com.architect.atlas.container"
        val className = "AtlasDIContainer"

        val jvmFile = codeGenerator.createNewFile(Dependencies(false), packageName, className)
        val androidFile =
            codeGenerator.createNewFile(Dependencies(false), packageName, "AndroidAtlasDIContainer")

        // **Modify JVM Implementation**

        OutputStreamWriter(jvmFile).use { writer ->
            writer.write("package $packageName\n\n")
            writer.write("import kotlin.reflect.KClass\n\n")

            writer.write("actual object $className {\n")
            writer.write("    private val singletons = mutableMapOf<KClass<*>, Any>()\n")
            writer.write("    private val factories = mutableMapOf<KClass<*>, () -> Any>()\n")
            writer.write("    private val scopedInstances = mutableMapOf<KClass<*>, Any>()\n\n")

            for (classDeclaration in singletons) {
                val className = classDeclaration.simpleName.asString()
                writer.write("    init { singletons[$className::class] = $className() }\n")
            }

            for (classDeclaration in transients) {
                val className = classDeclaration.simpleName.asString()
                writer.write("    init { factories[$className::class] = { $className() } }\n")
            }

            for (classDeclaration in scoped) {
                val className = classDeclaration.simpleName.asString()
                writer.write("    fun getScoped$className(): $className {\n")
                writer.write("        return scopedInstances.getOrPut($className::class) { $className() } as $className\n")
                writer.write("    }\n")
            }

            writer.write("\n    fun <T : Any> resolve(clazz: KClass<T>): T {\n")
            writer.write("        return (singletons[clazz] ?: factories[clazz]?.invoke() ?: scopedInstances[clazz]) as? T \n")
            writer.write("            ?: throw IllegalArgumentException(\"No provider found for \${clazz.simpleName}\")\n")
            writer.write("    }\n")

            writer.write("}\n")
            writer.flush()
        }

        // **Modify Android Implementation (Only Adds ViewModel Logic)**
        if (isAndroidModule) {
            OutputStreamWriter(androidFile).use { writer ->
                writer.write("package $packageName\n\n")
                writer.write("import androidx.lifecycle.ViewModel\n")
                writer.write("import androidx.lifecycle.ViewModelProvider\n")
                writer.write("import androidx.lifecycle.ViewModelStoreOwner\n")
                writer.write("import kotlin.reflect.KClass\n\n")

                writer.write("actual object $className : AndroidAtlasDIContainer() {\n")
                writer.write("    private val viewModelFactories = mutableMapOf<KClass<out ViewModel>, () -> ViewModel>()\n\n")

                for (classDeclaration in viewModels) {
                    val className = classDeclaration.simpleName.asString()
                    writer.write("    init { viewModelFactories[$className::class] = { $className() } }\n")
                }

                writer.write("\n    fun <T : ViewModel> resolveViewModel(owner: ViewModelStoreOwner, clazz: KClass<T>): T {\n")
                writer.write("        val factory = viewModelFactories[clazz]\n")
                writer.write("            ?: throw IllegalArgumentException(\"No ViewModel factory found for \${clazz.simpleName}\")\n")
                writer.write("        return ViewModelProvider(owner, ViewModelFactory(factory))[clazz.java]\n")
                writer.write("    }\n")

                writer.write("}\n")

                writer.write("\nclass ViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {\n")
                writer.write("    override fun <T : ViewModel> create(modelClass: Class<T>): T {\n")
                writer.write("        return creator() as? T ?: throw IllegalArgumentException(\"Unknown ViewModel class: \${modelClass.simpleName}\")\n")
                writer.write("    }\n}\n")

                writer.flush()
            }
        }
    }
}

