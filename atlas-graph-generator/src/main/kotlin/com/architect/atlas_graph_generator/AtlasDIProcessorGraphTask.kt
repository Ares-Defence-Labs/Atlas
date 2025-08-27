package com.architect.atlasGraphGenerator

import com.architect.atlas_graph_generator.helpers.buildAllowLists
import com.architect.atlas_graph_generator.helpers.collectClassesFromRoots
import com.architect.atlas_graph_generator.helpers.filterBySimple
import com.architect.atlas_graph_generator.helpers.filterClassToPackage
import com.architect.atlas_graph_generator.helpers.filterProvides
import com.architect.atlas_graph_generator.helpers.filterProvidesByModuleMembership
import com.architect.atlas_graph_generator.helpers.filterProvidesReturnTypes
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

    @get:Optional
    @get:OutputDirectory
    abstract val wearOSOutputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val iOSOutputDir: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRootDir: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val androidMainRootDir: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val externalSourceDirs: ConfigurableFileCollection

    @get:Optional
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val wearOSSourceDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iosSourceDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencyCommonMainSources: ConfigurableFileCollection

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputHashFile: RegularFileProperty

    @get:Optional
    @get:OutputDirectory
    abstract val wearOSModuleDirectory: DirectoryProperty

    @get:Input
    abstract var isAndroidTarget: Boolean

    @get:Input
    abstract val extraViewModelBaseClasses: SetProperty<String>

    @get:Optional
    @get:Input
    abstract var wearOSBasePackageRef: String

    @get:Input
    abstract var androidBasePackageRef: String

    init {
        group = "Atlas"
        description = "Generates a dependency graph for the project"

        outputs.upToDateWhen {
            val file = inputHashFile.orNull?.asFile
            file != null && file.exists()
        }
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

        val wearContainerDir = wearOSOutputDir.orNull?.asFile

        logger.lifecycle("WEARABLE $wearContainerDir")
        val androidContainerDir = androidOutputDir.get().asFile
        val iOSContainerDir = iOSOutputDir.get().asFile

        androidContainerDir.mkdirs()
        iOSContainerDir.mkdirs()
        wearContainerDir?.mkdirs()

        val outputFile = File(androidContainerDir, "AtlasContainer.kt")
        val iosOutputFile = File(iOSContainerDir, "AtlasContainer.kt")
        val androidOutputFile = File(androidContainerDir, "ViewModelExtensions.kt")

        val wearOutputFile =
            if (wearContainerDir != null) File(wearContainerDir, "AtlasContainer.kt") else null
        val wearVMOutputFile =
            if (wearContainerDir != null) File(wearContainerDir, "ViewModelExtensions.kt") else null

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

            if (wearOutputFile?.exists() == true) {
                logger.lifecycle("🗑 Deleting old ViewModelExtensions.kt to force regeneration for WearOS")
                wearOutputFile.delete()
            }

            if (wearVMOutputFile?.exists() == true) {
                logger.lifecycle("🗑 Deleting old ViewModelExtensions.kt to force regeneration for WearOS")
                wearVMOutputFile.delete()
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
            allSourceDirs.addAll(wearOSSourceDirs.files)
            allSourceDirs.addAll(androidMainRootDir.files)
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
            val vmLayout = generateAndroidExtensions()

            // --- ANDROID allow-lists: Android app roots + commonMain (EXCLUDES Wear roots) ---
            val androidRoots = externalSourceDirs.files
            val commonRoots = dependencyCommonMainSources.files + androidMainRootDir.files

            logger.lifecycle("DROID MODULE PATH : ${externalSourceDirs.asPath}")
            logger.lifecycle(
                "ANDROID PRE COLLECTIONS ${
                    androidRoots.map { it.path }.joinToString { "\n" }
                }"
            )
            logger.lifecycle(
                "COMMONMAIN POST COLLECTIONS ${
                    commonRoots.map { it.path }.joinToString { "\n" }
                }"
            )

            val (commonFq, _) = collectClassesFromRoots(commonRoots)
            val (androidFq, androidSimple) = run {
                val (fqA, simpleA) = buildAllowLists(androidRoots)
                val (fqC, simpleC) = buildAllowLists(commonRoots)

                (fqA + fqC) to (simpleA + simpleC)
            }

            val androidClassToPackage = filterClassToPackage(classToPackage, androidSimple)
            val androidSingletons = filterBySimple(singletons, androidSimple) { it }
            val androidFactories = filterBySimple(factories, androidSimple) { it }
            val androidScoped = filterBySimple(scopedInstances, androidSimple) { it }
            val androidViewModels = filterBySimple(viewModels, androidSimple) { it }
            val androidModules = filterBySimple(modules, androidSimple) { it }

            // either android base contains the key, or the file is included inside commonMain/androidMain
            val androidProvides = provides.filter {
                it.key.contains(androidBasePackageRef) || commonFq.any { r ->
                    r.contains(it.key)
                }
            }
            val androidProvidesReturnTypes = providesReturnTypes.filter {
                it.key.contains(androidBasePackageRef) || commonFq.any { r ->
                    r.contains(it.key)
                }
            }

            logger.lifecycle("ANDROID PACKAGE NAMESPACE $androidBasePackageRef")
            logger.lifecycle("ANDROID PROVIDES $androidProvides")
            logger.lifecycle("ANDROID PROVIDES RETURN TYPES $androidProvidesReturnTypes")

            // --- Generate ANDROID (no Wear-only modules leak in) ---
            outputFile.writeText(
                generateAtlasContainer(
                    androidClassToPackage,
                    androidSingletons,
                    androidFactories,
                    androidScoped,
                    androidViewModels,
                    androidModules,
                    androidProvides,
                    androidProvidesReturnTypes
                )
            )
            androidOutputFile.writeText(vmLayout)

            logger.lifecycle("✅ Android container generated (kept Android+commonMain; excluded Wear-only types).")

            // --- Wear branch below (unchanged logic but using the same helpers) ---
            val wearModuleRoot: File? = wearOSModuleDirectory.orNull?.asFile
            if (wearOutputFile != null && wearModuleRoot != null && wearModuleRoot.exists()) {
                logger.lifecycle("Logging WearOS Module Root: ${wearModuleRoot.absolutePath}")

                val wearRoots: Set<File> = wearOSSourceDirs.files
                val (allowedFq, allowedSimple) = run {
                    val (fqA, simpleA) = buildAllowLists(wearRoots)
                    val (fqC, simpleC) = buildAllowLists(commonRoots)

                    (fqA + fqC) to (simpleA + simpleC)
                }

                val wearClassToPackage = filterClassToPackage(classToPackage, allowedSimple)
                val wearSingletons = filterBySimple(singletons, allowedSimple) { it }
                val wearFactories = filterBySimple(factories, allowedSimple) { it }
                val wearScoped = filterBySimple(scopedInstances, allowedSimple) { it }
                val wearViewModels = filterBySimple(viewModels, allowedSimple) { it }
                val wearModules = filterBySimple(modules, allowedSimple) { it }

                val wearProvides = provides.filter {
                    it.key.contains(wearOSBasePackageRef) || commonFq.any { r ->
                        r.contains(it.key)
                    }
                }
                val wearProvidesReturnTypes =
                    providesReturnTypes.filter {
                        it.key.contains(wearOSBasePackageRef) || commonFq.any { r ->
                            r.contains(it.key)
                        }
                    }

                wearOutputFile.writeText(
                    generateAtlasContainer(
                        wearClassToPackage,
                        wearSingletons,
                        wearFactories,
                        wearScoped,
                        wearViewModels,
                        wearModules,
                        wearProvides,
                        wearProvidesReturnTypes
                    )
                )
                wearVMOutputFile?.writeText(vmLayout)
                logger.lifecycle("✅ Wear container generated (kept Wear+commonMain; excluded Android-only).")
            } else {
                logger.lifecycle("ℹ️ Skipping WearOS: no wear module root / output.")
            }
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

    private fun getViewModelStackGen(): String {
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
        val kclass = singletons.toList().firstOrNull {
            it.first.simpleName?.contains(clazz) ?: false || it.first.qualifiedName?.contains(clazz)
                    ?: false
        }
            ?: factories.toList().firstOrNull {
                it.first.simpleName?.contains(clazz) ?: false || it.first.qualifiedName?.contains(
                    clazz
                ) ?: false
            }
                ?: viewModels.toList().firstOrNull {
                    it.first.simpleName?.contains(clazz) ?: false || it.first.qualifiedName?.contains(
                        clazz
                    )
                            ?: false
                }
                    ?: provides.toList().firstOrNull {
                        it.first.simpleName?.contains(clazz) ?: false || it.first.qualifiedName?.contains(
                            clazz
                        )
                                ?: false
                    }
                        ?: modules.toList().firstOrNull {
                            it.first.simpleName?.contains(clazz) ?: false || it.first.qualifiedName?.contains(
                                clazz
                            )
                                    ?: false
                        }

            @Suppress("UNCHECKED_CAST")
            return resolve(kclass?.first as KClass<T>)
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
                        //singletons[clazz] = lazy { clazz.objectInstance?.let { return it } }
                    }
                }
            }
                 
        override fun <T : Any> resolveViewModel(clazz: KClass<T>): T? {
          val entry = viewModels[clazz] ?: return null
          return entry.getOrRegenerate() as? T
       }
       
       override fun <T : Any> resetViewModel(clazz: KClass<T>) {
            @Suppress("UNCHECKED_CAST")
            val entry = viewModels[clazz] as? ViewModelEntry<T>
                ?: error("❌ No ViewModel factory registered for class:")

            viewModels[clazz] = ViewModelEntry(entry.factory)
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