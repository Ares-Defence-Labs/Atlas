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
            val resolvedParentClass = classHierarchy[parentClass] ?: simpleToFqName[parentClass] ?: parentClass
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
            mutableMapOf<String, Pair<String, String>>() // Stores Provided dependencies (Class -> Module, Function)

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

        val classHierarchy = mutableMapOf<String, String?>() // Fully Qualified Class Name -> Superclass
        val simpleToFqName = mutableMapOf<String, String>() // Simple Name -> Fully Qualified Name

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
                        val baseClassRaw = match.groupValues.getOrNull(2)?.split("<")?.firstOrNull()?.trim() // Ignore generics

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
                                    val providesRegex = "@Provides[\\s\\n]+fun\\s+(\\w+)\\s*\\(.*\\)\\s*:\\s*([\\w.]+)".toRegex()
                                    providesRegex.findAll(content).forEach { match ->
                                        val methodName = match.groupValues[1]
                                        val returnType = match.groupValues[2]
                                        provides[returnType] = Pair(className, methodName)
                                        classToPackage[returnType] = packageName
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
                provides
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
        provides: Map<String, Pair<String, String>>
    ): String {
        val imports = (classToPackage.entries.map { "import ${it.value}.${it.key}" } +
                provides.keys.filter { it.contains(".") }
                    .map { "import ${it}" }) // Filter out invalid imports
            .toSortedSet() // Remove duplicates
            .joinToString("\n")

        return """
            package com.architect.atlas.container

            $imports

            import kotlin.reflect.KClass
            import com.architect.atlas.container.dsl.AtlasContainerContract

            object AtlasContainer : AtlasContainerContract {
                private val singletons: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()
                private val factories: MutableMap<KClass<*>, () -> Any> = mutableMapOf()
                private val scoped: MutableMap<KClass<*>, MutableMap<String, Any>> = mutableMapOf()
                private val viewModels: MutableMap<KClass<*>, Lazy<Any>> = mutableMapOf()
                private val modules: MutableMap<KClass<*>, Any> = mutableMapOf()
                private val provides: MutableMap<KClass<*>, () -> Any> = mutableMapOf()

                init {
                    ${if (singletons.isNotEmpty()) singletons.joinToString("\n        ") { "singletons[${it}::class] = lazy { ${it}() }" } else "// No singletons registered"}
                    ${if (factories.isNotEmpty()) factories.joinToString("\n        ") { "factories[${it}::class] = { lazy { ${it}() }}" } else "// No factories registered"}
                    ${if (scopedInstances.isNotEmpty()) scopedInstances.joinToString("\n        ") { "scoped[${it}::class] = mutableMapOf()" } else "// No scoped instances registered"}
                    ${if (viewModels.isNotEmpty()) viewModels.joinToString("\n        ") { "viewModels[${it}::class] = lazy { ${it}() }" } else "// No ViewModels registered"}
                    ${if (modules.isNotEmpty()) modules.joinToString("\n        ") { "modules[${it}::class] = ${it}()" } else "// No Modules registered"}
                    ${if (provides.isNotEmpty()) provides.entries.joinToString("\n        ") { "provides[${it.key}::class] = { (modules[${it.value.first}::class] as ${it.value.first}).${it.value.second}() }" } else "// No @Provides registered"}
                }

                override fun <T : Any> resolve(clazz: KClass<T>): T {
                    return (singletons[clazz]?.value
                        ?: factories[clazz]?.invoke()
                        ?: viewModels[clazz]?.value
                        ?: provides[clazz]?.invoke()
                        ?: modules[clazz]) as? T
                        ?: throw IllegalArgumentException("No provider found for " + clazz.simpleName)
                }
                
                // dynamic registration (invoked by DSL Api)
                // These are internal APIS ON ATLAS (DO NOT USE IN YOUR APP DIRECTLY)
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
                        factories[clazz] = factory
                    }
                    scopeId != null -> {
                        scoped.getOrPut(clazz) { mutableMapOf() }[scopeId] = factory?.invoke() ?: instance!!
                    }
                    viewModel -> {
                        viewModels[clazz] = lazy { factory?.invoke() ?: instance!! }
                    }
                    else -> {
                        throw IllegalArgumentException("❌ Could not register this type. No valid type specified")
                    }
                }
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