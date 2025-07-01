package com.architect.atlasGraphGenerator

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class AtlasDIProcessorGraphTask : DefaultTask() {

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val iOSOutputDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val externalSourceDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iosSourceDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencyCommonMainSources: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    @get:Input
    abstract val extraViewModelBaseClasses: SetProperty<String>

    init {
        group = "Atlas"
        description = "Generates a dependency graph for the project"
    }

    private fun resolveTransitiveInheritanceChain(
        className: String,
        classHierarchy: Map<String, String?>
    ): List<String> {
        val chain = mutableListOf<String>()
        var current = className

        while (true) {
            val parent = classHierarchy[current] ?: break
            if (chain.contains(parent)) break // avoid circular inheritance
            chain.add(parent)
            current = parent
        }

        return chain
    }

    private fun checkIfExtendsValidViewModel(
        fullyQualifiedClassName: String,
        classHierarchy: Map<String, String?>,
        simpleToFqName: Map<String, String?>
    ): Boolean {
        // here scan for all base viewmodels that inherit from the ViewModel class directly, then add them to the class heirarchy
        val validViewModels = classHierarchy
            .filter { (_, superClass) ->
                superClass == "androidx.lifecycle.ViewModel" ||
                        superClass == "com.architect.atlas.architecture.mvvm.ViewModel"
            }
            .map { it.key }
            .toMutableSet()

        validViewModels.addAll(extraViewModelBaseClasses.get())

        var current = fullyQualifiedClassName
        while (current.isNotBlank()) {
            logger.lifecycle("🔍 Traversing: $current")

            if (validViewModels.contains(current)) {
                logger.lifecycle("✅ Valid ViewModel base found: $current")
                return true
            }

            current = classHierarchy[current]
                ?: simpleToFqName[current]
                        ?: break
        }

        logger.lifecycle("❌ No valid ViewModel found in inheritance chain for: $fullyQualifiedClassName")
        return false
    }

    fun resolveTransitiveSuperclasses(
        className: String,
        classHierarchy: Map<String, String>
    ): List<String> {
        val result = mutableListOf<String>()
        var current = className
        while (classHierarchy.containsKey(current)) {
            val superClass = classHierarchy[current]!!
            result += superClass
            current = superClass
        }
        return result
    }

    @TaskAction
    fun generateGraph() {
        logger.lifecycle("🚀 Running generateDependencyGraph task...")


        val androidContainerDir = androidOutputDir.get().asFile
        val iOSContainerDir = iOSOutputDir.get().asFile

        androidContainerDir.mkdirs()
        iOSContainerDir.mkdirs()

        val outputFile = File(androidContainerDir, "AtlasContainer.kt")
        val iosOutputFile = File(iOSContainerDir, "AtlasContainer.kt")
        val androidOutputFile = File(androidContainerDir, "ViewModelExtensions.kt")

        // 🔹 NEW: Delete previous files to force regeneration
        if (!isAndroidTarget) {
            if (iosOutputFile.exists()) {
                logger.lifecycle("🗑 Deleting old ios AtlasContainer.kt to force regeneration")
                iosOutputFile.delete()
            }
        } else {
            if (outputFile.exists()) {
                logger.lifecycle("🗑 Deleting old AtlasContainer.kt to force regeneration")
                outputFile.delete()
            }
            if (androidOutputFile.exists()) {
                logger.lifecycle("🗑 Deleting old ViewModelExtensions.kt to force regeneration")
                androidOutputFile.delete()
            }
        }

        val classToPackage = mutableMapOf<String, String>()
        val singletons = mutableSetOf<String>()
        val factories = mutableSetOf<String>()
        val scopedInstances = mutableSetOf<String>()
        val viewModels = mutableSetOf<String>()
        val modules = mutableSetOf<String>()
        val provides =
            mutableMapOf<String, ProvidesFunctionInfo>() // Stores Provided dependencies (Class -> Module, Function)

        val annotationMappings = mapOf(
            "com.architect.atlas.container.annotations.Singleton" to singletons,
            "com.architect.atlas.container.annotations.Factory" to factories,
            "com.architect.atlas.container.annotations.Scoped" to scopedInstances,
            "com.architect.atlas.container.annotations.ViewModels" to viewModels,
            "com.architect.atlas.container.annotations.Module" to modules,
        )

        val classHierarchy =
            mutableMapOf<String, String?>() // Fully Qualified Class Name -> Superclass
        val simpleToFqName = mutableMapOf<String, String?>() // Simple Name -> Fully Qualified Name

        // check provides annotations
        val importMappings =
            mutableMapOf<String, String?>() // Stores (SimpleName → Fully Qualified Name)
        val providesReturnTypes =
            mutableMapOf<String, String>() // Stores (Return Type → Fully Qualified Name)

        val allSourceDirs =
            projectRootDir.files
        if (isAndroidTarget) {
            allSourceDirs.addAll(externalSourceDirs.files)
        } else {
            allSourceDirs.addAll(iosSourceDirs.files)
        }

        allSourceDirs.addAll(dependencyCommonMainSources.files)

        allSourceDirs
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
                    val packageName = packageMatch?.groupValues?.get(1)?.trim() ?: ""

                    // ✅ Extract Imports (Simple Name → Fully Qualified Name)
                    val importRegex = Regex("""import\s+([\w.]+)""")
                    importRegex.findAll(content).forEach { match ->
                        val fullyQualifiedName = match.groupValues[1]
                        val simpleName = fullyQualifiedName.substringAfterLast(".")
                        importMappings[simpleName] = fullyQualifiedName
                        simpleToFqName[simpleName] = fullyQualifiedName
                    }

                    // ✅ Scan for @Provides functions **after extracting imports**
                    if (content.contains("@Provides")) {
                        val providesRegex =
                            Regex("""@Provides\s+fun\s+\w+\s*\([^)]*\)\s*:\s*([\w.]+)""")
                        providesRegex.findAll(content).forEach { match ->
                            val returnType = match.groupValues[1]

                            // ✅ Use imports to resolve the fully qualified name instead of assuming the package
                            val fullyQualifiedReturnType =
                                importMappings[returnType] ?: "$packageName.$returnType"

                            providesReturnTypes[returnType] =
                                fullyQualifiedReturnType // ✅ Store correct package mapping
                            logger.lifecycle("🔍 @Provides detected: Function returns $returnType → Fully qualified as $fullyQualifiedReturnType")
                        }
                    }
                }
            }


        logger.lifecycle("🔍 Return Types for Provides ${providesReturnTypes.map { "${it.key} : ${it.value}\n" }}")
        logger.lifecycle("DEP FILES : ${dependencyCommonMainSources.files.count()}")
//        val classRegex = Regex("""class\s+(\w+)\s*:\s*([\w.<>]+)""")
//        dependencyCommonMainSources.files
//            .filter { it.isDirectory }
//            .flatMap { it.walkTopDown().filter { it.isFile && it.extension == "kt" } }
//            .forEach { file ->
//                val content = file.readText()
//
//                val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
//                val packageName = packageMatch?.groupValues?.get(1)?.trim() ?: return@forEach
//
//                classRegex.findAll(content).forEach { match ->
//                    val className = match.groupValues[1]
//                    val baseClassRaw = match.groupValues[2].split("<").first().trim()
//
//                    val fullyQualifiedName = "$packageName.$className"
//                    val resolvedBaseClass = simpleToFqName[baseClassRaw] ?: baseClassRaw
//
//                    classHierarchy[fullyQualifiedName] = resolvedBaseClass
//                    simpleToFqName[className] = fullyQualifiedName
//
//                    logger.lifecycle("📘 Class from dependency: $fullyQualifiedName → $resolvedBaseClass")
//                }
//            }

        // ✅ First pass: Collect all class declarations across all files
        allSourceDirs
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
                    val packageName = packageMatch?.groupValues?.get(1)?.trim() ?: ""

                    val classRegex = Regex("""class\s+(\w+)\s*(?::\s*([\w.<>]+))?""")
                    classRegex.findAll(content).forEach { match ->
                        val className = match.groupValues[1]
                        val fullyQualifiedClassName = "$packageName.$className"
                        val baseClassRaw = match.groupValues.getOrNull(2)?.split("<")?.firstOrNull()
                            ?.trim() // Ignore generics

                        // Store class mappings
                        classHierarchy[fullyQualifiedClassName] = null
                        simpleToFqName[className] = fullyQualifiedClassName

                        if (baseClassRaw != null) {
                            // scan for class files in dependencies, and append them to the classHeirarchy collection
                            // ✅ If the base class is a simple name (e.g., `ViewModel`), resolve it
                            val resolvedBaseClass = simpleToFqName[baseClassRaw]
                                ?: when (baseClassRaw) {
                                    "ViewModel" -> "androidx.lifecycle.ViewModel"
                                    "BaseViewModel" -> "com.architect.atlas.architecture.mvvm.ViewModel"
                                    else -> baseClassRaw
                                }

                            classHierarchy[fullyQualifiedClassName] = resolvedBaseClass
                            logger.lifecycle("✅ Mapping: $fullyQualifiedClassName extends $resolvedBaseClass")
                        }
                    }
                }
            }

//        resolveTransitiveInheritanceChain(
//            classHierarchy,
//            simpleToFqName,
//            importMappings
//        )

        logger.lifecycle("📚 Final class hierarchy map:")
        classHierarchy.forEach { (child, parent) ->
            logger.lifecycle("   $child → $parent")
        }

        // ✅ Second pass: Process annotations and verify hierarchy
        allSourceDirs
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
                    val packageName = packageMatch?.groupValues?.get(1)?.trim() ?: ""

                    annotationMappings.forEach { (annotation, collection) ->
                        val annotationSimpleName = annotation.substringAfterLast(".")
                        val annotationRegex =
                            "@$annotationSimpleName\\s+class\\s+(\\w+)".toRegex()

                        annotationRegex.findAll(content).forEach { match ->
                            val className = match.groupValues[1]
                            val fullyQualifiedClassName = "$packageName.$className"

                            if (content.contains("import $annotation")) {
                                collection.add(fullyQualifiedClassName)
                                classToPackage[className] = packageName

                                if (isAndroidTarget && annotation == "com.architect.atlas.container.annotations.ViewModels") {
                                    val extendsValidViewModel =
                                        checkIfExtendsValidViewModel(
                                            fullyQualifiedClassName,
                                            classHierarchy,
                                            simpleToFqName
                                        )
                                    if (!extendsValidViewModel) {
                                        throw IllegalArgumentException(
                                            "🚨 ERROR: Class `$fullyQualifiedClassName` marked with @ViewModels must extend `com.architect.atlas.architecture.mvvm.ViewModel` or `androidx.lifecycle.ViewModel` directly or indirectly."
                                        )
                                    }
                                }

                                if (annotation == "com.architect.atlas.container.annotations.Module") {
                                    val providesFunctionRegex =
                                        Regex("""@Provides\s+fun\s+(\w+)\s*\((.*?)\)\s*:\s*([\w.]+)""")
                                    providesFunctionRegex.findAll(content)
                                        .forEach { match ->
                                            val functionName = match.groupValues[1]
                                            val parameters =
                                                match.groupValues[2] // Capture parameters
                                            val returnType = match.groupValues[3]

                                            val fullyQualifiedReturnType =
                                                importMappings[returnType]
                                                    ?: "$packageName.$returnType"

                                            val parameterList = parameters.split(",")
                                                .mapNotNull { param ->
                                                    val parts =
                                                        param.trim().split(":")
                                                            .map { it.trim() }
                                                    if (parts.size == 2) {
                                                        val paramName = parts[0]
                                                        val paramType = parts[1]
                                                        val fullyQualifiedParamType =
                                                            importMappings[paramType]
                                                                ?: "$packageName.$paramType"
                                                        paramName to fullyQualifiedParamType
                                                    } else null
                                                }

                                            provides[fullyQualifiedReturnType] =
                                                ProvidesFunctionInfo(
                                                    module = className,
                                                    functionName = functionName,
                                                    parameters = parameterList
                                                )

                                            classToPackage[returnType] = packageName

                                            logger.lifecycle("🔍 @Provides detected: $functionName returning $returnType with params: ${parameterList.map { "${it.first}: ${it.second}" }}")
                                        }
                                }

                                logger.lifecycle("✅ Found $annotation in ${file.name}: $fullyQualifiedClassName (package: $packageName)")
                            }
                        }
                    }
                }
            }

        // prepare annotation file
        if (isAndroidTarget) {
            logger.lifecycle("WRITING DEPENDENCY GRAPH TO ANDROID")
            outputFile.writeText(
                generateAtlasContainer(
                    classToPackage,
                    singletons,
                    factories,
                    scopedInstances,
                    viewModels,
                    modules,
                    provides,
                    providesReturnTypes
                )
            )

            androidOutputFile.writeText(generateAndroidExtensions())

            logger.lifecycle("✅ Generated AtlasContainer.kt at: ${outputFile.absolutePath}")
            logger.lifecycle("✅ Generated ViewModelExtensions.kt at: ${androidOutputFile.absolutePath}")
        } else {
            logger.lifecycle("WRITING DEPENDENCY GRAPH TO IOS")
            iosOutputFile.writeText(
                generateAtlasContainer(
                    classToPackage,
                    singletons,
                    factories,
                    scopedInstances,
                    viewModels,
                    modules,
                    provides,
                    providesReturnTypes
                )
            )
            logger.lifecycle("✅ Generated AtlasContainer.kt at: ${iosOutputFile.absolutePath}")
        }
    }

//    private fun resolveTransitiveInheritanceChain(
//        classHierarchy: MutableMap<String, String?>,
//        simpleToFqName: MutableMap<String, String?>,
//        importMappings: MutableMap<String, String?>
//    ) {
//        val visited = mutableSetOf<String>()
//
//        for ((child, directParent) in classHierarchy) {
//            var current = directParent
//            while (current != null && current !in visited) {
//                visited += current
//
//                val nextParent = classHierarchy[current]
//                    ?: simpleToFqName[current]
//                    ?: importMappings[current]
//                    ?: break
//
//                classHierarchy[current] = nextParent
//                current = nextParent
//            }
//        }
//    }

    private fun getViewModelStackGen(): String {
        if (isAndroidTarget) {
            return """
                 private class ViewModelEntry<T : Any>(val factory: () -> T) {
            private var instance: T? = null

            fun getOrRegenerate(): T {
                return instance ?: factory().also { instance = it }
            }

            fun forceRegenerate() {
                instance = factory()
            }
            
            fun resetVm(){
               instance = null         
            }
        }
            """.trimIndent()
        } else {
            return """
            private class ViewModelEntry<T : Any>(val factory: () -> T) {
           private var weakRef: Lazy<WeakReference<T>> = lazy { WeakReference(factory())}

            fun getOrRegenerate(): T {
                return weakRef.value.get() ?: factory().also { newInstance ->
                    weakRef = lazy {WeakReference(newInstance)} // ✅ Regenerate and store new instance
                }
            }
            
            fun forceRegenerate() {
                weakRef = lazy { WeakReference(factory())} 
            }
            
            fun resetVm(){
               weakRef = lazy { WeakReference(factory())}      
            }
        }
        """.trimIndent()
        }
    }

    private fun generateAtlasContainer(
        classToPackage: Map<String, String>,
        singletons: Set<String>,
        factories: Set<String>,
        scopedInstances: Set<String>,
        viewModels: Set<String>,
        modules: Set<String>,
        provides: Map<String, ProvidesFunctionInfo>,
        providesReturnTypes: Map<String, String>
    ): String {
        val allClassesToImport = (
                singletons +
                        factories +
                        scopedInstances +
                        viewModels +
                        provides.keys + // ✅ Ensure return types of @Provides functions are imported
                        provides.values.flatMap { it.parameters.map { (_, paramType) -> paramType } } // ✅ Ensure all parameters in @Provides are imported
                )
            .mapNotNull { className -> classToPackage[className]?.let { "$it.$className" } }
            .toSortedSet()
            .toMutableSet()

        logger.lifecycle("📦 Imported Classes: ${allClassesToImport.joinToString()}")
        logger.lifecycle("📦 Modules Found: ${modules.joinToString()}")

        // 🔹 Generate imports for all relevant classes
        val imports = allClassesToImport.joinToString("\n") { "import $it" }
        val moduleImports = modules.joinToString("\n") { "import $it" }
        val providesImports = providesReturnTypes.values.joinToString("\n") { "import $it" }

        val providesLazyDeclarations =
            provides.entries.joinToString("\n") { (returnType, providesInfo) ->
                val moduleSimpleName = providesInfo.module
                val fullyQualifiedModule =
                    classToPackage[providesInfo.module]?.let { "$it.${providesInfo.module}" }
                        ?: providesInfo.module

                val safeReturnTypeName = returnType.replace(".", "").replace("/", "")

                """
    private val lazyInstanceFor$safeReturnTypeName = lazy {
        val moduleInstance = modules[$moduleSimpleName::class] as? $moduleSimpleName
            ?: throw IllegalArgumentException("🚨 ERROR: Module instance for $fullyQualifiedModule not found!")

        moduleInstance.${providesInfo.functionName}(
            ${providesInfo.parameters.joinToString(", ") { "resolve(${it.second}::class)" }}
        )
    }
    """.trimIndent()
            }

        val providesRegistrations = provides.entries.joinToString("\n") { (returnType, _) ->
            val safeReturnTypeName = returnType.replace(".", "").replace("/", "")

            """
    provides[$returnType::class] = { lazyInstanceFor$safeReturnTypeName.value }
    """.trimIndent()
        }

        return """
        package com.architect.atlas.container

        $imports
        $providesImports
        $moduleImports
       
        import com.architect.atlas.memory.WeakReference
        import kotlin.reflect.KClass
        import com.architect.atlas.container.dsl.AtlasContainerContract
        
        ${getViewModelStackGen()}
        
        object AtlasContainer : AtlasContainerContract {
            $providesLazyDeclarations
        
            private var allRegisteredClassDefinitions = mutableMapOf<String, KClass<*>>()
    
            private val singletons: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()
            private val factories: MutableMap<KClass<*>, () -> Lazy<Any>> = mutableMapOf()
            private val scoped: MutableMap<KClass<*>, MutableMap<String, Any>> = mutableMapOf()
            private val viewModels: MutableMap<KClass<*>, ViewModelEntry<Any>> = mutableMapOf()
            private val modules: MutableMap<KClass<*>, Any> = mutableMapOf()
            private val provides: MutableMap<KClass<*>, () -> Any> = mutableMapOf()

            init {   
                ${if (singletons.isNotEmpty()) singletons.joinToString("\n") { "singletons[${it}::class] = lazy { ${it}() }" } else "// No singletons registered"}
                ${if (factories.isNotEmpty()) factories.joinToString("\n") { "factories[${it}::class] = { lazy { ${it}() }}" } else "// No factories registered"}
                ${if (scopedInstances.isNotEmpty()) scopedInstances.joinToString("\n") { "scoped[${it}::class] = mutableMapOf()" } else "// No scoped instances registered"}
                ${if (viewModels.isNotEmpty()) viewModels.joinToString("\n") { "viewModels[${it}::class] = ViewModelEntry { ${it}() }" } else "// No ViewModels registered"}
                ${if (modules.isNotEmpty()) modules.joinToString("\n") { "modules[${it}::class] = ${it}()" } else "// No Modules registered"}
                
                $providesRegistrations
        
            forceRefreshNamedDependencies()
            }
            
            private fun forceRefreshNamedDependencies() {
                val allKClasses = buildSet {
                    addAll(singletons.keys)
                    addAll(factories.keys)
                    addAll(viewModels.keys)
                    addAll(modules.keys)
                    addAll(provides.keys)
                    addAll(scoped.keys)
                }

               for (kclass in allKClasses) {
                  val key = kclass.qualifiedName ?: kclass.simpleName ?: "UNKNOWN"
                  if (key !in allRegisteredClassDefinitions) {
                     allRegisteredClassDefinitions[key] = kclass
                  }
               }  
            }
            
            override fun <T : Any> resolve(clazz: KClass<T>): T {
                return (singletons[clazz]?.value
                            ?: factories[clazz]?.invoke()?.value
                            ?: resolveViewModel(clazz)
                            ?: provides[clazz]?.invoke()
                            ?: modules[clazz]) as? T
                            ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
            }
            
            override fun <T : Any> resolveByName(clazz: String): T {
                val kclass = allRegisteredClassDefinitions[clazz]
                    ?: throw IllegalArgumentException("No registered class found for: " + clazz)

                @Suppress("UNCHECKED_CAST")
                return resolve(kclass as KClass<T>)
            }
            
            // dynamic registration (invoked by DSL API)
            // These are internal APIs ON ATLAS (DO NOT USE IN YOUR APP DIRECTLY)
            override fun <T : Any> register(
                clazz: KClass<T>,
                instance: T?,
                factory: (() -> T)?,
                scopeId: String?,
                viewModel: Boolean
            ) {
                when {
                    instance != null -> {
                        singletons[clazz] = lazy { instance }
                    }
                    factory != null -> {
                        if(viewModel) {
                            viewModels[clazz] =
                                ViewModelEntry(factory) // ✅ Store factory with WeakReference
                        }
                        else {
                            factories[clazz] = { lazy { factory() } }
                        }
                    }
                    scopeId != null -> {
                        scoped.getOrPut(clazz) { mutableMapOf() }[scopeId] = factory?.invoke() ?: instance!!
                    }
                    else -> {
                        throw IllegalArgumentException("❌ Could not register this type. No valid type specified")
                    }
                }
                
                forceRefreshNamedDependencies()
            }
                 
        override fun <T : Any> resolveViewModel(clazz: KClass<T>): T? {
          val entry = viewModels[clazz] ?: return null
          return entry.getOrRegenerate() as? T
       }
       
       override fun <T : Any> resetViewModel(clazz: KClass<T>) {
        @Suppress("UNCHECKED_CAST")
        val entry = viewModels[clazz] as? ViewModelEntry<T>
            ?: error("No ViewModel registered for class:")

        viewModels[clazz] = ViewModelEntry { entry.factory() }
        }
        
        override fun resetViewModelByName(clazz: String) {
       
        }
    }
    
        
        
    """.trimIndent()
    }

    private fun generateAndroidExtensions(): String {
        return """
            package com.architect.atlas.container.android

            import androidx.lifecycle.ViewModel
            import com.architect.atlas.container.AtlasContainer
            import androidx.activity.ComponentActivity
            import androidx.fragment.app.Fragment
            import androidx.appcompat.app.AppCompatActivity

            inline fun <reified T : ViewModel> Fragment.viewModels(): Lazy<T> {
                return lazy { AtlasContainer.resolve(T::class) }
            }

            inline fun <reified T : ViewModel> AppCompatActivity.viewModels(): Lazy<T> {
                return lazy { AtlasContainer.resolve(T::class) }
            }

            inline fun <reified T : ViewModel> ComponentActivity.viewModels(): Lazy<T> {
                return lazy { AtlasContainer.resolve(T::class) }
            }
        """.trimIndent()
    }


}

data class ProvidesFunctionInfo(
    val module: String,
    val functionName: String,
    val parameters: List<Pair<String, String>>
)