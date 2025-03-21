package com.architect.atlasGraphGenerator

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class AtlasDIProcessorGraphTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val androidOutputDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidResources: ConfigurableFileCollection

    @get:Input
    abstract var isAndroidTarget: Boolean

    init {
        group = "Atlas"
        description = "Generates a dependency graph for the project"
        outputs.upToDateWhen { false }
        androidResources.from(project.layout.projectDirectory.dir("androidApp/build/intermediates/res/merged/debug"))
    }

    private fun checkIfExtendsValidViewModel(
        fullyQualifiedClassName: String,
        classHierarchy: Map<String, String?>,
        simpleToFqName: Map<String, String>
    ): Boolean {
        val validBaseViewModels = setOf(
            "com.architect.atlas.architecture.mvvm.ViewModel",
            "androidx.lifecycle.ViewModel"
        )

        var currentClass: String? = fullyQualifiedClassName

        while (currentClass != null) {
            logger.lifecycle("✅ Checking class: $currentClass")

            // ✅ If the current class is a valid ViewModel, return true
            if (validBaseViewModels.contains(currentClass)) {
                logger.lifecycle("✅ Found valid ViewModel: $currentClass")
                return true
            }

            // Resolve parent class properly
            val parentClass = classHierarchy[currentClass]
            if (parentClass == null || parentClass == currentClass) {
                logger.lifecycle("✅ Reached top of hierarchy: $parentClass (no further parent)")
                break
            }

            // ✅ Ensure parent class is fully resolved
            val resolvedParentClass =
                classHierarchy[parentClass] ?: simpleToFqName[parentClass] ?: parentClass
            logger.lifecycle("✅ Parent resolved to: $resolvedParentClass")

            currentClass = resolvedParentClass
        }

        logger.lifecycle("❌ No valid ViewModel found for: $fullyQualifiedClassName")
        return false
    }

    @TaskAction
    fun generateGraph() {
        logger.lifecycle("🚀 Running generateDependencyGraph task...")

        val atlasContainerDir = File(outputDir.get().asFile, "com/architect/atlas/container/")
        val androidContainerDir =
            File(androidOutputDir.get().asFile, "com/architect/atlas/container/android/")

        atlasContainerDir.mkdirs()
        androidContainerDir.mkdirs()

        val outputFile = File(atlasContainerDir, "AtlasContainer.kt")
        val androidOutputFile = File(androidContainerDir, "ViewModelExtensions.kt")

        // 🔹 NEW: Delete previous files to force regeneration
        if (outputFile.exists()) {
            logger.lifecycle("🗑 Deleting old AtlasContainer.kt to force regeneration")
            outputFile.delete()
        }
        if (androidOutputFile.exists()) {
            logger.lifecycle("🗑 Deleting old ViewModelExtensions.kt to force regeneration")
            androidOutputFile.delete()
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

        logger.lifecycle("📂 Ensured directories exist: ${atlasContainerDir.absolutePath}, ${androidContainerDir.absolutePath}")

        // ✅ Check if Android resources are present
        if (androidResources.files.isNotEmpty()) {
            logger.lifecycle("📦 Detected merged Android resources at ${androidResources.asPath}")
        } else {
            logger.lifecycle("⚠️ No Android merged resources found.")
        }

        val classHierarchy =
            mutableMapOf<String, String?>() // Fully Qualified Class Name -> Superclass
        val simpleToFqName = mutableMapOf<String, String>() // Simple Name -> Fully Qualified Name

        // check provides annotations
        val importMappings =
            mutableMapOf<String, String>() // Stores (SimpleName → Fully Qualified Name)
        val providesReturnTypes =
            mutableMapOf<String, String>() // Stores (Return Type → Fully Qualified Name)

        projectRootDir.get().asFile.walkTopDown()
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


        // ✅ First pass: Collect all class declarations across all files
        projectRootDir.get().asFile.walkTopDown()
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

        // ✅ Second pass: Process annotations and verify hierarchy
        projectRootDir.get().asFile.walkTopDown()
            .filter { it.isDirectory && it.path.contains("src") && it.path.contains("kotlin") }
            .forEach { sourceDir ->
                sourceDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                    val content = file.readText()

                    val packageMatch = Regex("""^package\s+([\w.]+)""").find(content)
                    val packageName = packageMatch?.groupValues?.get(1)?.trim() ?: ""

                    annotationMappings.forEach { (annotation, collection) ->
                        val annotationSimpleName = annotation.substringAfterLast(".")
                        val annotationRegex = "@$annotationSimpleName\\s+class\\s+(\\w+)".toRegex()

                        annotationRegex.findAll(content).forEach { match ->
                            val className = match.groupValues[1]
                            val fullyQualifiedClassName = "$packageName.$className"

                            if (content.contains("import $annotation")) {
                                collection.add(fullyQualifiedClassName)
                                classToPackage[className] = packageName

                                if (isAndroidTarget && annotation == "com.architect.atlas.container.annotations.ViewModels") {
                                    val extendsValidViewModel = checkIfExtendsValidViewModel(
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
                                    providesFunctionRegex.findAll(content).forEach { match ->
                                        val functionName = match.groupValues[1]
                                        val parameters = match.groupValues[2] // Capture parameters
                                        val returnType = match.groupValues[3]

                                        val fullyQualifiedReturnType =
                                            importMappings[returnType] ?: "$packageName.$returnType"

                                        val parameterList = parameters.split(",")
                                            .mapNotNull { param ->
                                                val parts =
                                                    param.trim().split(":").map { it.trim() }
                                                if (parts.size == 2) {
                                                    val paramName = parts[0]
                                                    val paramType = parts[1]
                                                    val fullyQualifiedParamType =
                                                        importMappings[paramType]
                                                            ?: "$packageName.$paramType"
                                                    paramName to fullyQualifiedParamType
                                                } else null
                                            }

                                        provides[fullyQualifiedReturnType] = ProvidesFunctionInfo(
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

        return """
        package com.architect.atlas.container

        $imports
        $providesImports
        $moduleImports

        import com.architect.atlas.memory.WeakReference
        import kotlin.reflect.KClass
        import com.architect.atlas.container.dsl.AtlasContainerContract
        
        private class ViewModelEntry<T : Any>(private val factory: () -> T) {
           private var weakRef: Lazy<WeakReference<T>> = lazy { WeakReference(factory())}

            fun getOrRegenerate(): T {
                return weakRef.value.get() ?: factory().also { newInstance ->
                    weakRef = lazy {WeakReference(newInstance)} // ✅ Regenerate and store new instance
                }
            }
            
            fun forceRegenerate() {
                weakRef = lazy { WeakReference(factory())} 
            }
        }

        object AtlasContainer : AtlasContainerContract {
            private val allRegisteredClassDefinitions: Map<String, KClass<*>> by lazy {
                formatRegisteredKClasses()
            }
    
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
                
                ${if (provides.isNotEmpty()) provides.entries.joinToString("\n") { (returnType, providesInfo) ->
                    val moduleSimpleName = providesInfo.module
                    val fullyQualifiedModule =
                    classToPackage[providesInfo.module]?.let { "$it.${providesInfo.module}" } ?: providesInfo.module
                """
            provides[$returnType::class] = {
                val moduleInstance = modules[$moduleSimpleName::class] as? $moduleSimpleName
                    ?: throw IllegalArgumentException("🚨 ERROR: Module instance for $fullyQualifiedModule not found!")

                moduleInstance.${providesInfo.functionName}(
                    ${providesInfo.parameters.joinToString(", ") { "resolve(${it.second}::class)" }}
                )
            }
            """.trimIndent()
            } else "// No @Provides registered"
        }
            }
            
            private fun formatRegisteredKClasses(): Map<String, KClass<*>> {
                 val allKClasses = mutableSetOf<KClass<*>>()

                allKClasses += singletons.keys
                allKClasses += factories.keys
                allKClasses += viewModels.keys
                allKClasses += modules.keys
                allKClasses += provides.keys
                allKClasses += scoped.keys

                return allKClasses.associateBy { it.qualifiedName ?: it.simpleName ?: "UNKNOWN" }
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
            }
                 
        override fun <T : Any> resolveViewModel(clazz: KClass<T>): T? {
          val entry = viewModels[clazz] ?: return null
          return entry.getOrRegenerate() as? T
       }
       
       override fun <T : Any> resetViewModel(clazz: KClass<T>) {
          viewModels[clazz]?.forceRegenerate()
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